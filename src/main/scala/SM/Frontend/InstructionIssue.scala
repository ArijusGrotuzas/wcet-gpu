package SM.Frontend

import chisel3._

class InstructionIssue extends Module {
  val io = IO(new Bundle {
    val pcIss = Input(UInt(32.W))
    val warpIss = Input(UInt(32.W))
    val warpFetch = Input(UInt(2.W))

    val opcode = Input(UInt(5.W))
    val dest = Input(UInt(5.W))
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rs3 = Input(UInt(5.W))
    val imm = Input(UInt(22.W))

    val issSetPending = Output(Bool())
    val issSetInactive = Output(Bool())
  })

  // TODO: Use the fifo queues in here

  // TODO: Implement instruction issue stage
  //  Elements:
  //    - Instruction queues
  //    - Logic for setting warp as pending
}
