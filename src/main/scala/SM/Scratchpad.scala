package SM

import chisel3._
import chisel3.util._

class Scratchpad extends Module {
  val io = IO(new Bundle {
    // TODO: Add port declarations
  })
  // TODO: Implement hardware
}

object ScratchpadMain extends App {
  println("Generating the scratchpad hardware")
  emitVerilog(new Scratchpad(), Array("--target-dir", "generated"))
}