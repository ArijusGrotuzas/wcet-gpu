package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionIssueTest extends AnyFlatSpec with ChiselScalatestTester {
  "InstructionIssue" should "work" in {
    test(new InstructionIssue(4, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
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

      dut.clock.step(4)

      // Push the first instruction for the first warp to the queue
      dut.io.id.valid.poke(true.B)
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(1.U)
      dut.io.id.dest.poke(5.U)
      dut.io.id.rs1.poke(7.U)
      dut.io.id.rs2.poke(8.U)
      dut.io.id.rs3.poke(9.U)
      dut.io.id.imm.poke(32.U)
      dut.io.warpIf.poke(3.U)

      dut.clock.step(1)

      // Push the second instruction for the first warp to the queue
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(2.U)
      dut.io.id.dest.poke(8.U)
      dut.io.id.rs1.poke(12.U)
      dut.io.id.rs2.poke(13.U)
      dut.io.id.rs3.poke(14.U)
      dut.io.id.imm.poke(255.U)
      dut.io.warpIf.poke(3.U)

      dut.clock.step(1)

      // Push the third instruction for the first warp to the queue
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(3.U)
      dut.io.id.dest.poke(19.U)
      dut.io.id.rs1.poke(20.U)
      dut.io.id.rs2.poke(21.U)
      dut.io.id.rs3.poke(22.U)
      dut.io.id.imm.poke(112.U)
      dut.io.warpIf.poke(3.U)

      dut.clock.step(1)

      // Push the fourth instruction for the first warp to the queue
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(4.U)
      dut.io.id.dest.poke(23.U)
      dut.io.id.rs1.poke(24.U)
      dut.io.id.rs2.poke(25.U)
      dut.io.id.rs3.poke(25.U)
      dut.io.id.imm.poke(16.U)
      dut.io.warpIf.poke(3.U)

      dut.clock.step(1)

      // Push an invalid instruction
      // Pop the first instruction from the first queue
      dut.io.id.valid.poke(false.B)
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(0.U)
      dut.io.warpIf.poke(0.U)
      dut.io.iss.opcode.expect(1.U)

      dut.clock.step(1)

      dut.io.warpIf.poke(0.U)
      dut.io.iss.opcode.expect(2.U)

      dut.clock.step(1)

      dut.io.warpIf.poke(0.U)
      dut.io.iss.opcode.expect(3.U)

      dut.clock.step(10)
    }
  }
}

