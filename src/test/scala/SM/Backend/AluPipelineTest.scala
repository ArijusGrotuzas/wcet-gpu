package SM.Backend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AluPipelineTest extends AnyFlatSpec with ChiselScalatestTester {
  "AluPipeline" should "perform correct operations" in {
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

      // Perform addition
      dut.io.of.rs1.poke("h0000000500000008".U)
      dut.io.of.rs2.poke("h0000000300000002".U)
      dut.io.of.opcode.poke("b00011".U)

      dut.io.alu.out.expect("h000000080000000a".U)

      dut.clock.step(1)

      // Perform subtraction
      dut.io.of.rs1.poke("h0000000500000008".U)
      dut.io.of.rs2.poke("h0000000300000002".U)
      dut.io.of.opcode.poke("b00111".U)

      dut.io.alu.out.expect("h0000000200000006".U)

      dut.clock.step(1)

      // Perform subtraction where subtrahend is larger than minuend
      dut.io.of.rs1.poke("h0000000500000001".U)
      dut.io.of.rs2.poke("h0000001000000040".U)
      dut.io.of.opcode.poke("b00111".U)

      dut.io.alu.out.expect("hfffffff5ffffffc1".U)

      dut.clock.step(1)

      // Perform a MAD operation
      dut.io.of.rs1.poke("h0000000500000001".U)
      dut.io.of.rs2.poke("h0000001000000040".U)
      dut.io.of.rs3.poke("h0000000100000001".U)
      dut.io.of.opcode.poke("b10111".U)

      dut.io.alu.out.expect("h0000005100000041".U)

      dut.clock.step(1)

      // Perform a MUL operation
      dut.io.of.rs1.poke("h0000000500000001".U)
      dut.io.of.rs2.poke("h0000001000000040".U)
      dut.io.of.opcode.poke("b10011".U)

      dut.io.alu.out.expect("h0000005000000040".U)

      dut.clock.step(1)

      // Perform a LUI operation
      dut.io.of.rs1.poke("h0000000500000001".U)
      dut.io.of.rs2.poke("h0000001000000040".U)
      dut.io.of.imm.poke(-32.S)
      dut.io.of.opcode.poke("b10001".U)

      dut.io.alu.out.expect("hFFFFFFE0FFFFFFE0".U)

      dut.clock.step(1)

      // Perform a LDS operation
      dut.io.of.rs1.poke("h0000000500000001".U)
      dut.io.of.rs2.poke("h0000001000000040".U)
      dut.io.of.srs.poke(4.U)
      dut.io.of.opcode.poke("b01001".U)

      dut.io.alu.out.expect("h0000000200000002".U)

      // Perform a SRLI operation
      dut.io.of.rs1.poke("h0000000500000001".U)
      dut.io.of.imm.poke(1.S)
      dut.io.of.opcode.poke("b10101".U)

      dut.io.alu.out.expect("h0000000200000000".U)
    }
  }
}
