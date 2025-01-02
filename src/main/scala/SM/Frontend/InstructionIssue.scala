package SM.Frontend

import SM.Opcodes
import chisel3._

class InstructionIssue(warpCount: Int, warpAddrLen: Int) extends Module {
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
      val imm = Input(SInt(32.W))
    }

    val scheduler = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val stall = Input(Bool())
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
      val imm = Output(SInt(32.W))
    }

    val issIf = new Bundle {
      val jump = Output(Bool())
      val jumpAddr = Output(UInt(32.W))
    }

    val nzpUpdate = new Bundle {
      val en = Input(Bool())
      val warp = Input(UInt(warpAddrLen.W))
      val nzp = Input(UInt(3.W))
    }

    val setPending = Output(Bool())
    val headInstrType = Output(UInt(warpCount.W))
  })

  private def generateQueues[T <: Data](gen: T, dataIn: T, inQueueSel: UInt, outQueueSel: UInt, warp: UInt): T = {
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

  // TODO: Add output if each warp's head instruction is a mem-instr
  val nzpRegFile = RegInit(VecInit(Seq.fill(warpCount)(0.U(3.W))))
  val inQueueSel = WireDefault(0.U(warpCount.W))
  val outQueueSel = WireDefault(0.U(warpCount.W))
  val headInstrType = VecInit(Seq.fill(warpCount)(0.U(1.W)))
  val setPending = WireDefault(false.B)
  val jump = WireDefault(false.B)
  val jumpAddr = WireDefault(0.U(32.W))
  val nzpRegOut = WireDefault(0.U(3.W))

  // TODO: Think if 32 bits is not too much for a PC, this ends up using a lot of LUTs for a queue
  val pcCurr = WireDefault(0.U(32.W))
  val opcodeCurr = WireDefault(0.U(5.W))
  val destCurr = WireDefault(0.U(5.W))
  val nzpCurr = WireDefault(0.U(3.W))
  val rs1Curr = WireDefault(0.U(5.W))
  val rs2Curr = WireDefault(0.U(5.W))
  val rs3Curr = WireDefault(0.U(5.W))
  val immCurr = WireDefault(0.S(32.W))
  // Since there is a need for getting all warp head instruction opcodes, a queue is generated outside the function
  val opcodeQueues = Module(new DataQueues(UInt(5.W), warpCount, 3))

  // Logic for selecting queues and thus popping entries from them
  when(io.id.valid) {
    inQueueSel := 1.U << io.id.warp
  }

  when(!io.scheduler.stall) {
    outQueueSel := 1.U << io.scheduler.warp
  }

  // Update the correct nzp register
  when(io.nzpUpdate.en) {
    nzpRegFile(io.nzpUpdate.warp) := io.nzpUpdate.nzp
  }

  // Generate buffers for decoded instructions
  pcCurr := generateQueues(UInt(32.W), io.id.pc, inQueueSel, outQueueSel, io.scheduler.warp)
  destCurr := generateQueues(UInt(5.W), io.id.dest, inQueueSel, outQueueSel, io.scheduler.warp)
  nzpCurr := generateQueues(UInt(3.W), io.id.nzp, inQueueSel, outQueueSel, io.scheduler.warp)
  rs1Curr := generateQueues(UInt(5.W), io.id.rs1, inQueueSel, outQueueSel, io.scheduler.warp)
  rs2Curr := generateQueues(UInt(5.W), io.id.rs2, inQueueSel, outQueueSel, io.scheduler.warp)
  rs3Curr := generateQueues(UInt(5.W), io.id.rs3, inQueueSel, outQueueSel, io.scheduler.warp)
  immCurr := generateQueues(SInt(32.W), io.id.imm, inQueueSel, outQueueSel, io.scheduler.warp)

  opcodeQueues.io.dataIn := io.id.opcode
  opcodeQueues.io.inQueueSel := inQueueSel
  opcodeQueues.io.outQueueSel := outQueueSel

  when(opcodeQueues.io.notEmpty(io.scheduler.warp)) {
    opcodeCurr := opcodeQueues.io.data(io.scheduler.warp)
  }

  // Forward data if all head instruction types are mem-instr
  for (i <- 0 until warpCount) {
    headInstrType(i) := opcodeQueues.io.data(i) === Opcodes.LD || opcodeQueues.io.data(i) === Opcodes.ST
  }

  // Forward the nzp value if the warp is the same
  when(io.nzpUpdate.warp === io.scheduler.warp && io.nzpUpdate.en) {
    nzpRegOut := io.nzpUpdate.nzp
  }.otherwise {
    nzpRegOut := nzpRegFile(io.scheduler.warp)
  }

  // If variable latency instruction set the warp as pending, or the last instruction of the wrap has been issued
  when(opcodeCurr === Opcodes.LD || opcodeCurr === Opcodes.ST || opcodeCurr === Opcodes.RET) {
    setPending := true.B
  }

  // If cmp instruction, then perform the jump based on nzp register contents
  when(opcodeCurr === Opcodes.BRNZP) {
    jump := (nzpCurr & nzpRegOut).orR
  }

  // TODO: Think if the 22 bits of immediate are enough for calculating the jump address
  jumpAddr := (pcCurr.asSInt + immCurr).asUInt

  io.iss.pending := setPending
  io.iss.warp := io.scheduler.warp
  io.iss.opcode := opcodeCurr
  io.iss.dest := destCurr
  io.iss.rs1 := rs1Curr
  io.iss.rs2 := rs2Curr
  io.iss.rs3 := rs3Curr
  io.iss.imm := immCurr

  io.setPending := setPending
  io.headInstrType := headInstrType.asUInt

  io.issIf.jump := jump
  io.issIf.jumpAddr := jumpAddr
}