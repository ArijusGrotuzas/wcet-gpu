package SM.Frontend

import chisel3._

class InstructionFetch extends Module {
  // TODO: Implement Instruction Fetch stage
  //  Elements:
  //    - Scheduler
  //    - Instruction Memory/Cache
  //    - Warp table

  val warpTable = Module(new WarpTable(4, 2))
}
