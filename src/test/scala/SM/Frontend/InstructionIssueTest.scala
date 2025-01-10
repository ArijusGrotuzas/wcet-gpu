package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionIssueTest extends AnyFlatSpec with ChiselScalatestTester {
  def pushInstruction(dut: InstructionIssue, bitFields: (Int, Int, Int, Int, Int, Int)): Unit = {
    dut.io.id.opcode.poke(bitFields._1.U)
    dut.io.id.dest.poke(bitFields._2.U)
    dut.io.id.rs1.poke(bitFields._3.U)
    dut.io.id.rs2.poke(bitFields._4.U)
    dut.io.id.rs3.poke(bitFields._5.U)
    dut.io.id.imm.poke(bitFields._6.S)
  }

  def expectBitFields(dut: InstructionIssue, bitFields: (Int, Int, Int, Int, Int, Int)): Unit = {
    dut.io.iss.opcode.expect(bitFields._1.U)
    dut.io.iss.dest.expect(bitFields._2.U)
    dut.io.iss.rs1.expect(bitFields._3.U)
    dut.io.iss.rs2.expect(bitFields._4.U)
    dut.io.iss.rs3.expect(bitFields._5.U)
    dut.io.iss.imm.expect(bitFields._6.S)
  }

  "InstructionIssue" should "push and pop correct instructions from queues" in {
    test(new InstructionIssue(4, 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val inputBitFields = List(
        (1, 5, 7, 8, 9, 32),
        (2, 8, 12, 13, 14, 255),
        (3, 19, 20, 21, 22, 112)
      )

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
      dut.io.nzpUpdateCtrl.en.poke(false.B)
      dut.io.nzpUpdateCtrl.warp.poke(0.U)
      dut.io.nzpUpdateCtrl.nzp.poke(0.U)

      dut.clock.step(4)

      // Set the push instruction warp and set the scheduler warp
      dut.io.id.valid.poke(true.B)
      dut.io.id.warp.poke(0.U)
      dut.io.scheduler.warp.poke(3.U)

      // Push the first instruction for the first warp to the queue
      pushInstruction(dut, inputBitFields.head)

      dut.clock.step(1)

      // Push the second instruction for the first warp to the queue
      pushInstruction(dut, inputBitFields(1))

      dut.clock.step(1)

      // Push the third instruction for the first warp to the queue
      pushInstruction(dut, inputBitFields(2))

      dut.clock.step(1)

      dut.io.id.valid.poke(false.B)
      dut.io.id.warp.poke(0.U)
      dut.io.scheduler.warp.poke(0.U)

      // Expect the first instruction to be popped from the first warp queue
      expectBitFields(dut, inputBitFields.head)

      dut.clock.step(1)

      // Expect the second instruction to be popped from the first warp queue
      expectBitFields(dut, inputBitFields(1))

      dut.clock.step(1)

      // Expect the third instruction to be popped from the first warp queue
      expectBitFields(dut, inputBitFields(2))

      dut.clock.step(1)
    }
  }
}

