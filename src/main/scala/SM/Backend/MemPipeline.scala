package SM.Backend

import chisel3._

class MemPipeline extends Module {
  val io = IO(new Bundle {
    // TODO: Add port declarations
  })
  // TODO: Implement hardware
}

object MemPipelineMain extends App {
  println("Generating the memory pipeline hardware")
  emitVerilog(new MemPipeline(), Array("--target-dir", "generated"))
}