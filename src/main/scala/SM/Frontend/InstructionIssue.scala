package SM.Frontend

import Constants.Opcodes
import SM.Backend.PredicateRegister
import chisel3._
import chisel3.util._

class InstructionIssue(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val id = new Bundle {
      val valid = Input(Bool())
      val pc = Input(UInt(32.W))
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val nzp = Input(UInt(3.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val srs = Input(UInt(3.W))
      val imm = Input(SInt(32.W))
    }

    val scheduler = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val stall = Input(Bool())
    }

    val nzpUpdateCtrl = new Bundle {
      val en = Input(Bool())
      val nzp = Input(UInt((3 * warpSize).W))
      val warp = Input(UInt(warpAddrLen.W))
    }

    val iss = new Bundle {
      //      val pc = Output(UInt(32.W))
      val pending = Output(Bool())
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val srs = Output(UInt(3.W))
      val imm = Output(SInt(32.W))
    }

    val issIfCtrl = new Bundle {
      val jump = Output(Bool())
      val jumpAddr = Output(UInt(32.W))
    }

    val setPending = Output(Bool())
    val headInstrType = Output(UInt(warpCount.W))
  })

  private def genNzpRegFile(idx: UInt, addrR: UInt, addrW: UInt, dataW: UInt = 0.U, we: Bool = false.B): UInt = {
    // Size = (3 * warpSize) * warpCount
    val nzpRegFile = RegInit(VecInit(Seq.fill(warpCount)(0.U((3 * warpSize).W))))
    val out = WireDefault(0.U((3 * warpSize).W))

    // Update the correct nzp register
    when(we) {
      nzpRegFile(addrW) := dataW
    }

    // Forward the nzp value if the warp is the same
    when(addrW === addrR && we) {
      out := dataW
    }.otherwise {
      out := nzpRegFile(addrR)
    }

    out
  }

  private def genDataQueues[T <: Data](gen: T, dataIn: T, inQueueSel: UInt, outQueueSel: UInt, warp: UInt): T = {
    val queues = Module(new DataQueues(gen, warpCount, 3))
    val dataOut = WireDefault(0.U.asTypeOf(gen))

    queues.io.dataIn := dataIn
    queues.io.inQueueSel := inQueueSel
    queues.io.outQueueSel := outQueueSel

    when(queues.io.notEmpty(warp)) {
      dataOut := queues.io.data(warp)
    }

    dataOut
  }

  val nzpRegFile = RegInit(VecInit(Seq.fill(warpCount)(0.U((3 * warpSize).W))))
  val inQueueSel = WireDefault(0.U(warpCount.W))
  val outQueueSel = WireDefault(0.U(warpCount.W))
  val headInstrType = VecInit(Seq.fill(warpCount)(0.U(1.W)))
  val setPending = WireDefault(false.B)
  val bnzpRegOut = WireDefault(0.U(3.W))
  val jumpAddr = WireDefault(0.U(32.W))
  val jump = WireDefault(false.B)

  // TODO: Think if 32 bits is not too much for a PC, this ends up using a lot of LUTs for a queue
  val pcCurr = WireDefault(0.U(32.W))
  val opcodeCurr = WireDefault(0.U(5.W))
  val destCurr = WireDefault(0.U(5.W))
  val nzpCurr = WireDefault(0.U(3.W))
  val rs1Curr = WireDefault(0.U(5.W))
  val rs2Curr = WireDefault(0.U(5.W))
  val rs3Curr = WireDefault(0.U(5.W))
  val srsCurr = WireDefault(0.U(3.W))
  val immCurr = WireDefault(0.S(32.W))

  // Logic for selecting queues and thus popping entries from them
  when(io.id.valid) {
    inQueueSel := 1.U << io.id.warp
  }

  when(!io.scheduler.stall) {
    outQueueSel := 1.U << io.scheduler.warp
  }

  // TODO: Think if all of these queues are necessary maybe one queue for an instruction of each warp is enough
  // Generate buffers for decoded instructions
  pcCurr := genDataQueues(UInt(32.W), io.id.pc, inQueueSel, outQueueSel, io.scheduler.warp)
  destCurr := genDataQueues(UInt(5.W), io.id.dest, inQueueSel, outQueueSel, io.scheduler.warp)
  nzpCurr := genDataQueues(UInt(3.W), io.id.nzp, inQueueSel, outQueueSel, io.scheduler.warp)
  rs1Curr := genDataQueues(UInt(5.W), io.id.rs1, inQueueSel, outQueueSel, io.scheduler.warp)
  rs2Curr := genDataQueues(UInt(5.W), io.id.rs2, inQueueSel, outQueueSel, io.scheduler.warp)
  rs3Curr := genDataQueues(UInt(5.W), io.id.rs3, inQueueSel, outQueueSel, io.scheduler.warp)
  srsCurr := genDataQueues(UInt(3.W), io.id.srs, inQueueSel, outQueueSel, io.scheduler.warp)
  immCurr := genDataQueues(SInt(32.W), io.id.imm, inQueueSel, outQueueSel, io.scheduler.warp)

  // Since there is a need for getting all warp head instruction opcodes, a queue is generated outside the function
  val opcodeQueues = Module(new DataQueues(UInt(5.W), warpCount, 3))

  opcodeQueues.io.dataIn := io.id.opcode
  opcodeQueues.io.inQueueSel := inQueueSel
  opcodeQueues.io.outQueueSel := outQueueSel

  when(opcodeQueues.io.notEmpty(io.scheduler.warp)) {
    opcodeCurr := opcodeQueues.io.data(io.scheduler.warp)
  }

  // Forward instruction type if all head instruction types are mem-instr
  for (i <- 0 until warpCount) {
    headInstrType(i) := opcodeQueues.io.data(i) === Opcodes.LD.asUInt(5.W) || opcodeQueues.io.data(i) === Opcodes.ST.asUInt(5.W)
  }

  // TODO: Move the nzp register file to instruction fetch same as branch jump logic
  // Update the correct nzp register
  when(io.nzpUpdateCtrl.en) {
    nzpRegFile(io.nzpUpdateCtrl.warp) := io.nzpUpdateCtrl.nzp
  }

  // Forward the nzp value if the warp is the same
  when(io.nzpUpdateCtrl.warp === io.scheduler.warp && io.nzpUpdateCtrl.en) {
    bnzpRegOut := io.nzpUpdateCtrl.nzp(2, 0)
  }.otherwise {
    bnzpRegOut := nzpRegFile(io.scheduler.warp)(2, 0)
  }

  // If variable latency instruction set the warp as pending, or the last instruction of the wrap has been issued
  when(opcodeCurr === Opcodes.LD.asUInt(5.W) || opcodeCurr === Opcodes.ST.asUInt(5.W) || opcodeCurr === Opcodes.RET.asUInt(5.W)) {
    setPending := true.B
  }

  // If cmp instruction, then perform the jump based on nzp register contents
  when(opcodeCurr === Opcodes.BNZP.asUInt(5.W)) {
    when(nzpCurr =/= 0.U) {
      // When the nzp value is not zero, then it is a conditional jump
      jump := (nzpCurr & bnzpRegOut).orR
    }.otherwise {
      // When the nzp value is zero, then it is an unconditional jump
      jump := true.B
    }
  }

  jumpAddr := ((0.U ## pcCurr).asSInt + immCurr).asUInt

  // To operand fetch stage
  io.iss.pending := setPending
  io.iss.warp := io.scheduler.warp
  io.iss.opcode := opcodeCurr
  io.iss.dest := destCurr
  io.iss.rs1 := rs1Curr
  io.iss.rs2 := rs2Curr
  io.iss.rs3 := rs3Curr
  io.iss.srs := srsCurr
  io.iss.imm := immCurr

  // Control signals
  io.setPending := setPending
  io.headInstrType := headInstrType.asUInt
  io.issIfCtrl.jump := jump
  io.issIfCtrl.jumpAddr := jumpAddr
}