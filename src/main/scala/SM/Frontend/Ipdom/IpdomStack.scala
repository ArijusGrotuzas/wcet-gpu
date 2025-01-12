package SM.Frontend.Ipdom

import chisel3._

class IpdomStack extends Module {
  val io = IO(new Bundle{
    val push = Input(Bool())
    val pop = Input(Bool())
    val full = Output(Bool())
    val empty = Output(Bool())
  })
}
