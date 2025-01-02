package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

private object TestKernels {
  val kernel1 = Array(
    "11000000000010111101010110001101", // (LUI, 12, 21, 23, X)
    "00000000000111011001100110101001", // (ADDI, 13, 6, 27, 1)
    "00000000001000011001010111011111", // (RET, 14, 5, 3, 2)
  )

  val kernel2 = Array(
    "00000000000001010000000001001001", // (ADDI, x2, x0, 10): j = 10
    "00000000000000101000000001101001", // (ADDI, x3, x0, 5): a = 5
    "00000000000000001001000010001001", // (ADDI, x4, x4, 1): i += 1
    "00000000000000000000000000000000", // (NOP)
    "00000000000000011001010010100011", // (ADD, x5, x5, x3): b += a
    "00000000000000010001000000000110", // (CMP, x4, x2) i < j
    "00000000000000000000000000000000", // (NOP)
    "11111111111111111110100000100010", // (BRNZP, NZP=100, -6)
    "00000000000000000000000000000000", // (NOP)
    "00000000000000000000000000000000", // (NOP)
    "00000000000000000000000000000000", // (NOP)
    "00000000000000000000000000000000", // (NOP)
    "00000000000000000000000000011111", // (RET)
  )
}

class FrontendTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadInstrMem(dut: Frontend, program: Array[String]): Unit = {
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

    // TODO: Three different conditions for imm
//    if (longImm) {
//      dut.io.front.imm.expect(Integer.parseUnsignedInt(program(instr).slice(0, 22) + "0000000000", 2).S)
//    } else {
//      dut.io.front.imm.expect(Integer.parseInt(program(instr).slice(0, 17), 2).S)
//    }
  }

  def expectKernel(dut: Frontend, warp: Int, start: Int, end: Int, program: Array[String], longImm: Array[Boolean]): Unit = {
    for(i <- start until end) {
      expectInstr(dut, warp, i, program, longImm(i - start) === true)
      dut.clock.step(1)
    }
  }

  "Frontend" should "correctly schedule instructions for warps" in {
    test(new Frontend(4, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val kernel = TestKernels.kernel1

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
      dut.io.nzpUpdate.en.poke(false.B)
      dut.io.nzpUpdate.nzp.poke(0.U)
      dut.io.nzpUpdate.warp.poke(0.U)

      dut.clock.step(1)

      // Load the instructions
      loadInstrMem(dut, kernel)

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

      // Expect correct instructions for the first warp
      expectKernel(dut, 0, 0, kernel.length, kernel, Array(true, false, false))

      // Expect an NOP after issuing the RET instruction
      dut.io.front.warp.expect(1.U)
      dut.io.front.opcode.expect(0.U)

      // Set the warp as inactive
      setWarpInactive(dut, 0)

      dut.clock.step(3)

      // Expect correct instructions for the second warp
      expectKernel(dut, 1, 0, kernel.length, kernel, Array(true, false, false))

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

  "Frontend" should "correctly perform a jump instruction" in {
    test(new Frontend(4, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val kernel = TestKernels.kernel2

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
      dut.io.nzpUpdate.en.poke(false.B)
      dut.io.nzpUpdate.nzp.poke(0.U)
      dut.io.nzpUpdate.warp.poke(0.U)

      dut.clock.step(1)

      // Load the instructions
      loadInstrMem(dut, kernel)

      dut.clock.step(1)

      // Start computation
      dut.io.start.ready.expect(true.B)
      dut.io.start.valid.poke(true.B)
      dut.io.start.data.poke("b0001".U)

      dut.clock.step(1)

      // Expect ready to be de-asserted
      dut.io.start.ready.expect(false.B)
      dut.io.start.valid.poke(false.B)
      dut.io.start.data.poke(0.U)

      dut.clock.step(4)

      expectKernel(dut, 0, 0, 7, kernel, Array.fill(7)(false))

      // Expect the branch instruction as well as update the NZP register to take the branch
      dut.io.nzpUpdate.en.poke(true.B)
      dut.io.nzpUpdate.nzp.poke("b001".U)
      dut.io.nzpUpdate.warp.poke(0.U)

      expectInstr(dut, 0, 7, kernel)

      dut.clock.step(1)

      dut.io.nzpUpdate.en.poke(false.B)
      dut.io.nzpUpdate.nzp.poke("b000".U)
      dut.io.nzpUpdate.warp.poke(0.U)

      // Step for four clock cycles since there is 4 delay slots after the jump instruction
      dut.clock.step(4)

      expectKernel(dut, 0, 2, 7, kernel, Array.fill(7)(false))

      // Expect the branch instruction as well as update the NZP register to not take the branch
      dut.io.nzpUpdate.en.poke(true.B)
      dut.io.nzpUpdate.nzp.poke(0.U)
      dut.io.nzpUpdate.warp.poke(0.U)

      expectInstr(dut, 0, 7, kernel)

      dut.clock.step(1)

      expectKernel(dut, 0, 8, kernel.length, kernel, Array.fill(10)(false))

      // Set the warp as inactive
      setWarpInactive(dut, 0)

      // Step for two clock cycles as the scheduler will need one clock cycle to reset the warp table before being ready
      dut.clock.step(2)

      // Expect the scheduler to be ready again
      dut.io.start.ready.expect(true.B)

      dut.clock.step(5)
    }
  }
}
