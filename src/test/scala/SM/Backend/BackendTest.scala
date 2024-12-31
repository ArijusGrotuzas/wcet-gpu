package SM.Backend

import SM.Opcodes
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BackendTest extends AnyFlatSpec with ChiselScalatestTester {
  def pushInstruction(
                       dut: Backend,
                       warp: Int,
                       opcode: UInt,
                       dest: Int = 0,
                       source1: Int = 0,
                       source2: Int = 0,
                       source3: Int = 0,
                       immediate: Int = 0
                     ): Unit = {
    dut.io.front.warp.poke(warp.U)
    dut.io.front.opcode.poke(opcode)
    dut.io.front.dest.poke(dest.U)
    dut.io.front.rs1.poke(source1.U)
    dut.io.front.rs2.poke(source2.U)
    dut.io.front.rs3.poke(source3.U)
    dut.io.front.imm.poke(immediate.U)
  }

  def generateBinaryString(count: Int, value: Int): String = {
    val binaryValue = String.format("%32s", Integer.toBinaryString(value)).replace(' ', '0').takeRight(32)

    "b" + (binaryValue * count)
  }

  "Backend" should "process arithmetic instructions" in {
    val warpCount = 4
    val warpSize = 8
    val warpAddrLen = 2

    test(new Backend(warpCount, warpSize, warpAddrLen)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.front.warp.poke(0.U)
      dut.io.front.opcode.poke(0.U)
      dut.io.front.dest.poke(0.U)
      dut.io.front.rs1.poke(0.U)
      dut.io.front.rs2.poke(0.U)
      dut.io.front.rs3.poke(0.U)
      dut.io.front.imm.poke(0.U)

      dut.clock.step(1)

      // Send an ADDI instruction
      pushInstruction(dut, 0, Opcodes.ADDI, 2, 0, 0, 0, 256)

      dut.clock.step(1)

      // Send an ADDI instruction
      pushInstruction(dut, 0, Opcodes.ADDI, 11, 0, 0, 0, 119)

      dut.clock.step(1)

      // Send an ADDI instruction
      pushInstruction(dut, 1, Opcodes.ADDI, 20, 0, 0, 0, 240)

      dut.clock.step(1)

      // Send an ADDI instruction
      pushInstruction(dut, 1, Opcodes.ADDI, 25, 0, 0, 0, 103)

      // Expect the first Immediate instruction to return the results
      dut.io.wbOutTest.expect(generateBinaryString(warpSize, 256).U)

      dut.clock.step(1)

      // Send ADD instruction
      pushInstruction(dut, 0, Opcodes.ADD, 3, 11, 2)

      // Expect the second Immediate instruction to return the results
      dut.io.wbOutTest.expect(generateBinaryString(warpSize, 119).U)

      dut.clock.step(1)

      // Send RET instruction
      pushInstruction(dut, 0, Opcodes.RET)

      // Expect the third Immediate instruction to return the results
      dut.io.wbOutTest.expect(generateBinaryString(warpSize, 240).U)

      dut.clock.step(1)

      // Send AND instruction
      pushInstruction(dut, 1, Opcodes.AND, 19, 20, 25)

      // Expect the fourth Immediate instruction to return the results
      dut.io.wbOutTest.expect(generateBinaryString(warpSize, 103).U)

      dut.clock.step(1)

      // Send an NOP instruction
      pushInstruction(dut, 0, Opcodes.NOP)

      // Expect the ADD instruction to return the results
      dut.io.wbOutTest.expect(generateBinaryString(warpSize, 375).U)

      dut.clock.step(1)

      // Send an NOP instruction
      pushInstruction(dut, 0, Opcodes.NOP)

      // Expect the RET instruction to return the results
      dut.io.wb.setInactive.expect(true.B)

      dut.clock.step(1)

      // Send AND instruction
      pushInstruction(dut, 1, Opcodes.AND, 19, 20, 25)

      // Expect the AND instruction to return the results
      dut.io.wbOutTest.expect(generateBinaryString(warpSize, 96).U)

      dut.clock.step(5)
    }
  }
}
