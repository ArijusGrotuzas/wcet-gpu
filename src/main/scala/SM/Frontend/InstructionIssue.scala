package SM.Frontend

import Constants.Opcodes
import chisel3._
import chisel3.util._

class InstructionIssue(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val id = new Bundle {
      val valid = Input(Bool())
      val threadMask = Input(UInt(warpSize.W))
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val srs = Input(UInt(3.W))
      val imm = Input(SInt(32.W))
      val pred = Input(UInt(2.W))
    }

    val scheduler = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val stall = Input(Bool())
    }

    val iss = new Bundle {
      val threadMask = Output(UInt(warpSize.W))
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val srs = Output(UInt(3.W))
      val imm = Output(SInt(32.W))
      val pred = Output(UInt(2.W))
    }

    val setPending = Output(Bool())
    val headInstrType = Output(UInt(warpCount.W))
  })

  private def genDataQueues[T <: Data](gen: T, dataIn: T, inQueueSel: UInt, outQueueSel: UInt, warp: UInt): T = {
    val queues = Module(new DataQueues(gen, warpCount, 3))
    val dataOut = WireDefault(0.U.asTypeOf(gen))

    queues.io.dataIn := dataIn
    queues.io.inQueueSel := inQueueSel
    queues.io.outQueueSel := outQueueSel

    when(queues.io.notEmpty(warp)) {
      dataOut := queues.io.data(warp)
    }

    Mux(outQueueSel.orR, dataOut, 0.U.asTypeOf(gen))
  }

  val headInstrType = VecInit(Seq.fill(warpCount)(0.U(1.W)))
  val inQueueSel = Mux(io.id.valid, (1.U << io.id.warp).asUInt, 0.U)
  val outQueueSel = Mux(!io.scheduler.stall, (1.U << io.scheduler.warp).asUInt, 0.U)

  // TODO: Think if all of these queues are necessary maybe one queue for an instruction of each warp is enough
  // TODO: Think if 32 bits is not too much for a PC, this ends up using a lot of LUTs for a queue
  // Generate buffers for decoded instructions
  val threadMaskCurr = genDataQueues(UInt(warpSize.W), io.id.threadMask, inQueueSel, outQueueSel, io.scheduler.warp)
  val destCurr = genDataQueues(UInt(5.W), io.id.dest, inQueueSel, outQueueSel, io.scheduler.warp)
  val rs1Curr = genDataQueues(UInt(5.W), io.id.rs1, inQueueSel, outQueueSel, io.scheduler.warp)
  val rs2Curr = genDataQueues(UInt(5.W), io.id.rs2, inQueueSel, outQueueSel, io.scheduler.warp)
  val rs3Curr = genDataQueues(UInt(5.W), io.id.rs3, inQueueSel, outQueueSel, io.scheduler.warp)
  val srsCurr = genDataQueues(UInt(3.W), io.id.srs, inQueueSel, outQueueSel, io.scheduler.warp)
  val immCurr = genDataQueues(SInt(32.W), io.id.imm, inQueueSel, outQueueSel, io.scheduler.warp)
  val predCurr = genDataQueues(UInt(2.W), io.id.pred, inQueueSel, outQueueSel, io.scheduler.warp)

  // Since there is a need for getting all warp head instruction opcodes, a queue is generated outside the function
  val opcodeQueues = Module(new DataQueues(UInt(5.W), warpCount, 3))
  val opcodeCurr = WireDefault(0.U(5.W))

  opcodeQueues.io.dataIn := io.id.opcode
  opcodeQueues.io.inQueueSel := inQueueSel
  opcodeQueues.io.outQueueSel := outQueueSel

  when(opcodeQueues.io.notEmpty(io.scheduler.warp)) {
    opcodeCurr := Mux(outQueueSel.orR, opcodeQueues.io.data(io.scheduler.warp), 0.U)
  }

  // Send information to warp scheduler if all head instruction types are mem-instr
  for (i <- 0 until warpCount) {
    headInstrType(i) := opcodeQueues.io.data(i) === Opcodes.LD.asUInt(5.W) || opcodeQueues.io.data(i) === Opcodes.ST.asUInt(5.W)
  }

  // To operand fetch stage
  io.iss.threadMask := threadMaskCurr
  io.iss.warp := io.scheduler.warp
  io.iss.opcode := opcodeCurr
  io.iss.dest := destCurr
  io.iss.rs1 := rs1Curr
  io.iss.rs2 := rs2Curr
  io.iss.rs3 := rs3Curr
  io.iss.srs := srsCurr
  io.iss.imm := immCurr
  io.iss.pred := predCurr

  // If variable latency instruction set the warp as pending, or the last instruction of the wrap has been issued
  io.setPending := (opcodeCurr === Opcodes.LD.asUInt(5.W) || opcodeCurr === Opcodes.ST.asUInt(5.W) || opcodeCurr === Opcodes.RET.asUInt(5.W))
  io.headInstrType := headInstrType.asUInt
}