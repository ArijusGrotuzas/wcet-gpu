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
      val nzp = Input(UInt(3.W))
      val en = Input(Bool())
      val warp = Input(UInt(warpAddrLen.W))
    }

    val setPending = Output(Bool())
    val headInstrType = Output(UInt(warpCount.W))
  })

  // TODO: Add output if each warp's head instruction is a mem-instr
  // TODO: Add logic for deciding if to make a jump when a BRNZP instruction is issued
  val nzpRegFile = RegInit(VecInit(Seq.fill(warpCount)(0.U(3.W))))

  when(io.nzpUpdate.en) {
    nzpRegFile(io.nzpUpdate.warp) := io.nzpUpdate.nzp
  }

  val inQueueSel = WireDefault(0.U(warpCount.W))
  val outQueueSel = WireDefault(0.U(warpCount.W))
  val headInstrType = WireDefault(0.U(warpCount.W))
  val setPending = WireDefault(false.B)
  val jump = WireDefault(false.B)
  val jumpAddr = WireDefault(0.U(32.W))

  when(io.id.valid) {
    inQueueSel := 1.U << io.id.warp
  }

  when(!io.scheduler.stall) {
    outQueueSel := 1.U << io.scheduler.warp
  }

  // TODO: Think if 32 bits is not too much for a PC, this ends up using a lot of LUTs
  val pcQueues = Module(new DataQueues(UInt(32.W), warpCount, 3))
  val pc = WireDefault(0.U(5.W))

  pcQueues.io.dataIn := io.id.pc
  pcQueues.io.inQueueSel := inQueueSel
  pcQueues.io.outQueueSel := outQueueSel
  pcQueues.io.outDataSel := io.scheduler.warp
  pc := pcQueues.io.dataOut

  val opcodeQueues = Module(new DataQueues(UInt(5.W), warpCount, 3))
  val opcode = WireDefault(0.U(5.W))

  opcodeQueues.io.dataIn := io.id.opcode
  opcodeQueues.io.inQueueSel := inQueueSel
  opcodeQueues.io.outQueueSel := outQueueSel
  opcodeQueues.io.outDataSel := io.scheduler.warp
  opcode := opcodeQueues.io.dataOut

  val destQueues = Module(new DataQueues(UInt(5.W), warpCount, 3))
  val dest = WireDefault(0.U(5.W))

  destQueues.io.dataIn := io.id.dest
  destQueues.io.inQueueSel := inQueueSel
  destQueues.io.outQueueSel := outQueueSel
  destQueues.io.outDataSel := io.scheduler.warp
  dest := destQueues.io.dataOut

  val nzpQueues = Module(new DataQueues(UInt(3.W), warpCount, 3))
  val nzp = WireDefault(0.U(5.W))

  nzpQueues.io.dataIn := io.id.nzp
  nzpQueues.io.inQueueSel := inQueueSel
  nzpQueues.io.outQueueSel := outQueueSel
  nzpQueues.io.outDataSel := io.scheduler.warp
  nzp := nzpQueues.io.dataOut

  val rs1Queues = Module(new DataQueues(UInt(5.W), warpCount, 3))
  val rs1 = WireDefault(0.U(5.W))

  rs1Queues.io.dataIn := io.id.rs1
  rs1Queues.io.inQueueSel := inQueueSel
  rs1Queues.io.outQueueSel := outQueueSel
  rs1Queues.io.outDataSel := io.scheduler.warp
  rs1 := rs1Queues.io.dataOut

  val rs2Queues = Module(new DataQueues(UInt(5.W), warpCount, 3))
  val rs2 = WireDefault(0.U(5.W))

  rs2Queues.io.dataIn := io.id.rs2
  rs2Queues.io.inQueueSel := inQueueSel
  rs2Queues.io.outQueueSel := outQueueSel
  rs2Queues.io.outDataSel := io.scheduler.warp
  rs2 := rs2Queues.io.dataOut

  val rs3Queues = Module(new DataQueues(UInt(5.W), warpCount, 3))
  val rs3 = WireDefault(0.U(5.W))

  rs3Queues.io.dataIn := io.id.rs3
  rs3Queues.io.inQueueSel := inQueueSel
  rs3Queues.io.outQueueSel := outQueueSel
  rs3Queues.io.outDataSel := io.scheduler.warp
  rs3 := rs3Queues.io.dataOut

  val immQueues = Module(new DataQueues(SInt(32.W), warpCount, 3))
  val imm = WireDefault(0.S(32.W))

  immQueues.io.dataIn := io.id.imm
  immQueues.io.inQueueSel := inQueueSel
  immQueues.io.outQueueSel := outQueueSel
  immQueues.io.outDataSel := io.scheduler.warp
  imm := immQueues.io.dataOut

  // If variable latency instruction set the warp as pending, or the last instruction of the wrap has been issued
  when(opcode === Opcodes.LD || opcode === Opcodes.ST || opcode === Opcodes.RET) {
    setPending := true.B
  }

  when(opcode === Opcodes.CMP) {
    jump := nzp & nzpRegFile(io.scheduler.warp).orR
  }

  // TODO: Think if the 22 bits of immediate are enough for calculating the jump address
  jumpAddr := pc + imm.asUInt

  io.iss.pending := setPending
  io.iss.warp := io.scheduler.warp
  io.iss.opcode := opcode
  io.iss.dest := dest
  io.iss.rs1 := rs1
  io.iss.rs2 := rs2
  io.iss.rs3 := rs3
  io.iss.imm := imm

  io.setPending := setPending
  io.headInstrType := headInstrType

  io.issIf.jump := jump
  io.issIf.jumpAddr := 0.U
}