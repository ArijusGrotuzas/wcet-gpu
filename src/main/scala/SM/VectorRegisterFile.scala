package SM

import chisel3._
import chisel3.util._

class VectorRegisterFile extends Module {
  val io = IO(new Bundle {
    // TODO: Add port declarations
  })
  // TODO: Implement hardware
}

object VectorRegisterFileMain extends App {
  println("Generating the vector register file hardware")
  emitVerilog(new VectorRegisterFile(), Array("--target-dir", "generated"))
}