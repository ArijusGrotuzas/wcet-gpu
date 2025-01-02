package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// TODO: Finish this test
class InstructionIssueTest extends AnyFlatSpec with ChiselScalatestTester {
  def pushInstruction() = {

  }

  def expectInstruction(dut: InstructionIssue, opcode: Int, dest: Int, rs1: Int, rs2: Int, rs3: Int, imm: Int) = {
    dut.io.iss.opcode.expect(2.U)
    dut.io.iss.dest.expect(8.U)
    dut.io.iss.rs1.expect(12.U)
    dut.io.iss.rs2.expect(13.U)
    dut.io.iss.rs3.expect(14.U)
    dut.io.iss.imm.expect(255.S)
  }

  "InstructionIssue" should "push and pop correct instructions from queues" in {
    test(new InstructionIssue(4, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.id.valid.poke(false.B)
      dut.io.id.pc.poke(0.U)
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(0.U)
      dut.io.id.dest.poke(0.U)
      dut.io.id.rs1.poke(0.U)
      dut.io.id.rs2.poke(0.U)
      dut.io.id.rs3.poke(0.U)
      dut.io.id.imm.poke(0.S)
      dut.io.scheduler.warp.poke(0.U)
      dut.io.scheduler.stall.poke(false.B)
      dut.io.nzpUpdate.en.poke(false.B)
      dut.io.nzpUpdate.warp.poke(0.U)
      dut.io.nzpUpdate.nzp.poke(0.U)

      dut.clock.step(4)

      // Push the first instruction for the first warp to the queue
      dut.io.scheduler.warp.poke(3.U)
      dut.io.id.valid.poke(true.B)
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(1.U)
      dut.io.id.dest.poke(5.U)
      dut.io.id.rs1.poke(7.U)
      dut.io.id.rs2.poke(8.U)
      dut.io.id.rs3.poke(9.U)
      dut.io.id.imm.poke(32.S)

      dut.clock.step(1)

      dut.io.scheduler.warp.poke(3.U)

      // Push the second instruction for the first warp to the queue
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(2.U)
      dut.io.id.dest.poke(8.U)
      dut.io.id.rs1.poke(12.U)
      dut.io.id.rs2.poke(13.U)
      dut.io.id.rs3.poke(14.U)
      dut.io.id.imm.poke(255.S)

      dut.clock.step(1)

      // Push the third instruction for the first warp to the queue
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(3.U)
      dut.io.id.dest.poke(19.U)
      dut.io.id.rs1.poke(20.U)
      dut.io.id.rs2.poke(21.U)
      dut.io.id.rs3.poke(22.U)
      dut.io.id.imm.poke(112.S)

      dut.clock.step(1)

      // Push an invalid instruction
      // Pop the first instruction from the first queue
      dut.io.id.valid.poke(false.B)
      dut.io.id.warp.poke(0.U)
      dut.io.id.opcode.poke(0.U)
      dut.io.scheduler.warp.poke(0.U)

      dut.io.iss.opcode.expect(1.U)
      dut.io.iss.dest.expect(5.U)
      dut.io.iss.rs1.expect(7.U)
      dut.io.iss.rs2.expect(8.U)
      dut.io.iss.rs3.expect(9.U)
      dut.io.iss.imm.expect(32.S)

      dut.clock.step(1)

      dut.io.iss.opcode.expect(2.U)
      dut.io.iss.dest.expect(8.U)
      dut.io.iss.rs1.expect(12.U)
      dut.io.iss.rs2.expect(13.U)
      dut.io.iss.rs3.expect(14.U)
      dut.io.iss.imm.expect(255.S)

      dut.clock.step(1)

      dut.io.iss.opcode.expect(3.U)
      dut.io.iss.dest.expect(19.U)
      dut.io.iss.rs1.expect(20.U)
      dut.io.iss.rs2.expect(21.U)
      dut.io.iss.rs3.expect(22.U)
      dut.io.iss.imm.expect(112.S)

      dut.clock.step(1)
    }
  }
}

