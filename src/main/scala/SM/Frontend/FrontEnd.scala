package SM.Frontend

import SM.VectorRegisterFile
import chisel3._

class FrontEnd extends Module {
  val io = IO(new Bundle {
    // TODO: Add port declarations
  })
  // TODO: Implement hardware
}

object FrontEndMain extends App {
  println("Generating the front end hardware")
  emitVerilog(new VectorRegisterFile(), Array("--target-dir", "generated"))
}