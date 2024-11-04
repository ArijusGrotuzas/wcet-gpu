package SM.Backend

import SM.Scratchpad
import chisel3._

class AluPipeline extends Module {
  val io = IO(new Bundle {
    // TODO: Add port declarations
  })
  // TODO: Implement hardware
}

object AluPipelineMain extends App {
  println("Generating the ALU pipeline hardware")
  emitVerilog(new Scratchpad(), Array("--target-dir", "generated"))
}