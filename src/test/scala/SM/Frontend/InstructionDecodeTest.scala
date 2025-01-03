package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

private object testKernels {
  val testKernel1 = Array(
    "00000000001001000110100000101101", // (LUI, x1, 2330)
    "00000000000001010000000001001001", // (ADDI, x2, x0, 10): j = 10
    "00000000000000011001010010100011", // (ADD, x5, x5, x3): b += a
    "00000000000000101001000011000111", // (SUB, x6, x4, x5)
    "00000000000000100000110011101011", // (AND, x7, x3, x4)
    "00000000000000001001000100001111", // (OR, x8, x4, x5)
    "00000000000000010001000000000110", // (CMP, x4, x2) i < j
    "01111111111111111110110000100010", // (BRNZP, NZP=100, 2097147)
    "00000000000000000000000000000000", // (NOP)
    "00000000000000000000000000011111" // (RET)
  )
}

class InstructionDecodeTest extends AnyFlatSpec with ChiselScalatestTester {
  def testKernel(dut: InstructionDecode, kernel: Array[String]): Unit = {
    for (i <- kernel.indices) {
      testInstruction(dut, kernel(i), i)

      dut.clock.step(1)
    }
  }

  def testInstruction(dut: InstructionDecode, instruction: String, pc: Int, warp: Int = 0, valid: Boolean = true): Unit = {
    val opcode = "b" + instruction.slice(27, 32)
    val dest = "b" + instruction.slice(22, 27)
    val nzp = "b" + instruction.slice(24, 27)
    val rs1 = "b" + instruction.slice(17, 22)
    val rs2 = "b" + instruction.slice(12, 17)
    val rs3 = "b" + instruction.slice(7, 12)

    // NOTE: parseInt cannot correctly parse signed binary strings
    val immReg = Integer.parseInt(instruction.slice(0, 17), 2)
    val immBrn = Integer.parseInt(instruction.slice(0, 22), 2)
    val immUpper = Integer.parseUnsignedInt(instruction.slice(8, 22) + "00000000000000000", 2)

    // Push instruction
    dut.io.instrF.valid.poke(valid.B)
    dut.io.instrF.warp.poke(warp.U)
    dut.io.instrF.pc.poke(pc.U)
    dut.io.instrF.instr.poke(("b" + instruction).U)

    // Expect correct fields to be decoded
    dut.io.id.opcode.expect(opcode.U)
    dut.io.id.dest.expect(dest.U)
    dut.io.id.nzp.expect(nzp.U)
    dut.io.id.rs1.expect(rs1.U)
    dut.io.id.rs2.expect(rs2.U)
    dut.io.id.rs3.expect(rs3.U)

    // Expect a different type of immediate to be decoded based on opcode
    opcode match {
      case "b01101" => dut.io.id.imm.expect(immUpper.S)
      case "b00010" => dut.io.id.imm.expect(immBrn.S)
      case _ => dut.io.id.imm.expect(immReg.S)
    }

    // Expect correct control values
    dut.io.id.warp.expect(warp.U)
    dut.io.id.pc.expect(pc.U)
    dut.io.id.valid.expect(valid.B)
  }

  "InstructionDecode" should "work" in {
    test(new InstructionDecode(2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.instrF.valid.poke(false.B)
      dut.io.instrF.warp.poke(0.U)
      dut.io.instrF.pc.poke(0.U)
      dut.io.instrF.instr.poke(0.U)

      dut.clock.step(1)

      testKernel(dut, testKernels.testKernel1)

      dut.clock.step(1)
    }
  }
}

