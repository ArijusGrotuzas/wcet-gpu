package SM.Backend

import chisel3._

class AluPipeline(operandWidth: Int, lanesCount: Int) extends Module {
  val io = IO(new Bundle {
    // TODO: Add port declarations
  })

  val alu = Module(new Alu(operandWidth))

  alu.io.a := 0.S
  alu.io.b := 0.S
  alu.io.op := AluOps.ADD
}