package SM.Backend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BackendTest extends AnyFlatSpec with ChiselScalatestTester{
  "Backend" should "work" in {
    test(new Backend(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.front.warp.poke(0.U)
      dut.io.front.opcode.poke(0.U)
      dut.io.front.dest.poke(0.U)
      dut.io.front.rs1.poke(0.U)
      dut.io.front.rs2.poke(0.U)
      dut.io.front.rs3.poke(0.U)
      dut.io.front.imm.poke(0.U)

      dut.clock.step(1)

      // Send an ADD instruction
      dut.io.front.warp.poke(1.U)
      dut.io.front.opcode.poke(1.U)
      dut.io.front.dest.poke(1.U)
      dut.io.front.rs1.poke(1.U)
      dut.io.front.rs2.poke(1.U)
    }
  }
}
