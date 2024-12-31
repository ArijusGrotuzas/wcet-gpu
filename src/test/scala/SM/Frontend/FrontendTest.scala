package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FrontendTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadMem(dut: Frontend, program: Array[String]): Unit = {
    dut.io.loadInstr.en.poke(true.B)

    for (i <- program.indices) {
      dut.io.loadInstr.addr.poke(i)
      dut.io.loadInstr.instr.poke(("b" + program(i)).U)
      dut.clock.step()
    }

    dut.io.loadInstr.en.poke(false.B)
  }

  def setWarpInactive(dut: Frontend, warp: Int): Unit = {
    dut.io.wb.setInactive.poke(true.B)
    dut.io.wb.warp.poke(warp.U)

    dut.clock.step(1)

    dut.io.wb.setInactive.poke(false.B)
  }

  def expectInstr(dut: Frontend, warp: Int, instr: Int, program: Array[String], longImm: Boolean = false): Unit = {
    dut.io.front.warp.expect(warp.U)
    dut.io.front.opcode.expect(("b" + program(instr).slice(27, 32)).U)
    dut.io.front.dest.expect(("b" + program(instr).slice(22, 27)).U)
    dut.io.front.rs1.expect(("b" + program(instr).slice(17, 22)).U)
    dut.io.front.rs2.expect(("b" + program(instr).slice(12, 17)).U)
    dut.io.front.rs3.expect(("b" + program(instr).slice(7, 12)).U)

    if (longImm) {
      dut.io.front.imm.expect(Integer.parseUnsignedInt(program(instr).slice(0, 22) + "0000000000", 2).S)
    } else {
      dut.io.front.imm.expect(Integer.parseInt(program(instr).slice(0, 17), 2).S)
    }
  }

  "Frontend" should "correctly schedule instructions for warps" in {
    test(new Frontend(4, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val kernel = Array(
        "11000000000010111101010110001101", // 1110000 00000 10111 10101 01100 01101 (LUI, 12, 21, 23, X)
        "00000000000111011001100110101001", // 0100000 00001 11011 00110 01101 11110 (ADDI, 13, 6, 27, 1)
        "00000000001000011001010111011111"  // 0000000 00010 00011 00101 01110 11111 (RET, 14, 5, 3, 2)
      )

      // Default assignments
      dut.io.loadInstr.en.poke(false.B)
      dut.io.loadInstr.instr.poke(0.U)
      dut.io.loadInstr.addr.poke(0.U)
      dut.io.wb.setNotPending.poke(false.B)
      dut.io.wb.warp.poke(0.U)
      dut.io.start.valid.poke(false.B)
      dut.io.start.data.poke(0.U)
      dut.io.funcUnits.memStall.poke(false.B)
      dut.io.funcUnits.aluStall.poke(false.B)

      dut.clock.step(1)

      // Load the instructions
      loadMem(dut, kernel)

      dut.clock.step(1)

      // Start computation
      dut.io.start.ready.expect(true.B)
      dut.io.start.valid.poke(true.B)
      dut.io.start.data.poke("b0011".U)

      dut.clock.step(1)

      // Expect ready to be de-asserted
      dut.io.start.ready.expect(false.B)
      dut.io.start.valid.poke(false.B)
      dut.io.start.data.poke(0.U)

      dut.clock.step(4)

      expectInstr(dut, 0, 0, kernel, longImm = true)

      dut.clock.step(1)

      expectInstr(dut, 0, 1, kernel)

      dut.clock.step(1)

      // Expect a RET instruction
      expectInstr(dut, 0, 2, kernel)

      dut.clock.step(1)

      // Expect an NOP after issuing the RET instruction
      dut.io.front.warp.expect(1.U)
      dut.io.front.opcode.expect(0.U)

      // Set the warp as inactive
      setWarpInactive(dut, 0)

      dut.clock.step(3)

      expectInstr(dut, 1, 0, kernel, longImm = true)

      dut.clock.step(1)

      expectInstr(dut, 1, 1, kernel)

      dut.clock.step(1)

      // Expect a RET instruction
      expectInstr(dut, 1, 2, kernel)

      dut.clock.step(1)

      // Expect an NOP after issuing the RET instruction
      dut.io.front.warp.expect(0.U)
      dut.io.front.opcode.expect(0.U)

      // Set the warp as inactive
      setWarpInactive(dut, 1)

      // Step for two clock cycles as the scheduler will need one clock cycle to reset the warp table before being ready
      dut.clock.step(2)

      dut.io.start.ready.expect(true.B)

      dut.clock.step(5)
    }
  }
}
