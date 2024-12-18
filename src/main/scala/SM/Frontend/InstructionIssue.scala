package SM.Frontend

import chisel3._

class InstructionIssue extends Module {
  val io = IO(new Bundle {
    val warpId = Input(UInt(2.W))
  })

  // TODO: Implement instruction issue stage
  //  Elements:
  //    - Instruction queues
  //    - Logic for setting warp as pending
}
