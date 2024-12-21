package SM.Frontend

import chisel3._
import chisel3.util._

class InstructionIssue(warpCount: Int) extends Module {
  val io = IO(new Bundle {
    val id = new Bundle {
      val valid = Input(Bool())
      val pc = Input(UInt(32.W))
      val warp = Input(UInt(warpCount.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val imm = Input(UInt(22.W))
    }

    val warpIf = Input(UInt(warpCount.W))

    val iss = new Bundle {
//      val pc = Output(UInt(32.W))
      val warp = Output(UInt(warpCount.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val imm = Output(UInt(22.W))
    }

    val issCtrl = new Bundle {
      val setPending = Output(Bool())
      val setInactive = Output(Bool())
    }

    val headInstrType = Output(UInt(warpCount.W))
  })

  // TODO: Add logic for setting warp as pending
  // TODO: Add logic for accepting stall signal from scheduler

  val setInactive = WireDefault(false.B)
  val inQueueSel = WireDefault(0.U(warpCount.W))
  val outQueueSel = WireDefault(0.U(warpCount.W))
  val headInstrType = WireDefault(0.U(warpCount.W))

  when(io.id.valid) {
    inQueueSel := 1.U << io.id.warp
  }

  outQueueSel := 1.U << io.warpIf

  val opcodeQueues = Module(new DataQueues(warpCount, 4, 5))
  val opcode = WireDefault(0.U(5.W))

  opcodeQueues.io.dataIn := io.id.opcode
  opcodeQueues.io.inQueueSel := inQueueSel
  opcodeQueues.io.outQueueSel := outQueueSel
  opcodeQueues.io.outDataSel := io.warpIf
  opcode := opcodeQueues.io.dataOut

  val destQueues = Module(new DataQueues(warpCount, 4, 5))
  val dest = WireDefault(0.U(5.W))

  destQueues.io.dataIn := io.id.dest
  destQueues.io.inQueueSel := inQueueSel
  destQueues.io.outQueueSel := outQueueSel
  destQueues.io.outDataSel := io.warpIf
  dest := destQueues.io.dataOut

  val rs1Queues = Module(new DataQueues(warpCount, 4, 5))
  val rs1 = WireDefault(0.U(5.W))

  rs1Queues.io.dataIn := io.id.rs1
  rs1Queues.io.inQueueSel := inQueueSel
  rs1Queues.io.outQueueSel := outQueueSel
  rs1Queues.io.outDataSel := io.warpIf
  rs1 := rs1Queues.io.dataOut

  val rs2Queues = Module(new DataQueues(warpCount, 4, 5))
  val rs2 = WireDefault(0.U(5.W))

  rs2Queues.io.dataIn := io.id.rs2
  rs2Queues.io.inQueueSel := inQueueSel
  rs2Queues.io.outQueueSel := outQueueSel
  rs2Queues.io.outDataSel := io.warpIf
  rs2 := rs2Queues.io.dataOut

  val rs3Queues = Module(new DataQueues(warpCount, 4, 5))
  val rs3 = WireDefault(0.U(5.W))

  rs3Queues.io.dataIn := io.id.rs3
  rs3Queues.io.inQueueSel := inQueueSel
  rs3Queues.io.outQueueSel := outQueueSel
  rs3Queues.io.outDataSel := io.warpIf
  rs3 := rs3Queues.io.dataOut

  val immQueues = Module(new DataQueues(warpCount, 4, 22))
  val imm = WireDefault(0.U(22.W))

  immQueues.io.dataIn := io.id.imm
  immQueues.io.inQueueSel := inQueueSel
  immQueues.io.outQueueSel := outQueueSel
  immQueues.io.outDataSel := io.warpIf
  imm := immQueues.io.dataOut

  when(opcode === "b11111".U) {
    setInactive := true.B
  }

  io.iss.warp := io.warpIf
  io.iss.opcode := opcode
  io.iss.dest := dest
  io.iss.rs1 := rs1
  io.iss.rs2 := rs2
  io.iss.rs3 := rs3
  io.iss.imm := imm

  io.issCtrl.setInactive := setInactive
  io.issCtrl.setPending := false.B

  io.headInstrType := headInstrType
}