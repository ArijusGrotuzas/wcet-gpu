package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionIssueTest extends AnyFlatSpec with ChiselScalatestTester {
  "InstructionIssue" should "work" in {
    test(new InstructionIssue(2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.id.pc.poke(0.U)
      dut.io.id.valid.poke(false.B)
      dut.io.id.warp.poke(0.U)
      dut.io.warpIf.poke(0.U)
      dut.io.id.opcode.poke(0.U)
      dut.io.id.dest.poke(0.U)
      dut.io.id.rs1.poke(0.U)
      dut.io.id.rs2.poke(0.U)
      dut.io.id.rs3.poke(0.U)
      dut.io.id.imm.poke(0.U)

      dut.clock.step(1)

      dut.io.id.warp.poke(1.U)
      dut.io.id.valid.poke(true.B)
      dut.io.warpIf.poke(0.U)
      dut.io.id.opcode.poke(1.U)

      dut.clock.step(1)

      dut.io.id.warp.poke(1.U)
      dut.io.id.valid.poke(true.B)
      dut.io.warpIf.poke(0.U)
      dut.io.id.opcode.poke(2.U)

      dut.clock.step(1)

      dut.io.id.warp.poke(1.U)
      dut.io.id.valid.poke(true.B)
      dut.io.warpIf.poke(0.U)
      dut.io.id.opcode.poke(3.U)

      dut.clock.step(1)

      dut.io.id.warp.poke(0.U)
      dut.io.warpIf.poke(1.U)
      dut.io.id.opcode.poke(0.U)

      dut.clock.step(10)
    }
  }
}

