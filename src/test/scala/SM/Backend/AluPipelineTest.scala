package SM.Backend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AluPipelineTest extends AnyFlatSpec with ChiselScalatestTester {
  "AluPipeline" should "perform correct subtraction" in {
    test(new AluPipeline(4, 1, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.of.warp.poke(0.U)
      dut.io.of.opcode.poke(0.U)
      dut.io.of.dest.poke(0.U)
      dut.io.of.rs1.poke(0.U)
      dut.io.of.rs2.poke(0.U)
      dut.io.of.rs3.poke(0.U)
      dut.io.of.imm.poke(0.S)

      dut.clock.step(1)

      // Perform a simple subtraction
      dut.io.of.rs1.poke("h0000000500000008".U)
      dut.io.of.rs2.poke("h0000000300000002".U)
      dut.io.of.opcode.poke("b00111".U)

      dut.io.alu.out.expect("h0000000200000006".U)

      dut.clock.step(1)

      // Perform a subtraction where the result is negative
      dut.io.of.rs1.poke("h0000000500000001".U)
      dut.io.of.rs2.poke("h0000001000000040".U)
      dut.io.of.opcode.poke("b00111".U)

      dut.io.alu.out.expect("hfffffff5ffffffc1".U)

      dut.clock.step(1)
    }
  }
}
