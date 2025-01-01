package SM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SmTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadInstrMem(dut: Sm, program: Array[String]): Unit = {
    dut.io.loadInstr.en.poke(true.B)

    for (i <- program.indices) {
      dut.io.loadInstr.addr.poke(i)
      dut.io.loadInstr.instr.poke(program(i).U(32.W))
      dut.clock.step()
    }

    dut.io.loadInstr.en.poke(false.B)
  }

  // TODO: Extend the test by adding more test programs
  // TODO: Add a way to assert correct output

  "Sm" should "execute program 1" in {
    test(new Sm(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//      val kernel = Array(
//        "00000000000000000010100000101101", // (LUI, x1, 10)
//        "00000000000000000101000001001101", // (LUI, x2, 20)
//        "00000000000000101000010001101001", // (ADDI, x3, x1, 5)
//        "00000000000001000000100010001001", // (ADDI, x4, x2, 8)
//        "00000000000000000000000000000000", // (NOP)
//        "00000000000000000000000000000000", // (NOP)
//        "00000000000000011001000010100011", // (ADD, x5, x3, x4)
//        "00000000000000000000000000000000", // (NOP)
//        "00000000000000000000000000000000", // (NOP)
//        "00000000000000101001000011000111", // (SUB, x6, x4, x5)
//        "00000000000000100000110011101011", // (AND, x7, x3, x4)
//        "00000000000000101001000100001111", // (OR, x8, x4, x5)
//        "00000000000000000000000000000000", // (NOP)
//        "00000000000000000000000000011111"  // (RET)
//      )

      dut.io.loadInstr.en.poke(false.B)
      dut.io.loadInstr.instr.poke(0.U)
      dut.io.loadInstr.addr.poke(0.U)
      dut.io.start.valid.poke(false.B)
      dut.io.start.data.poke(0.U)

      dut.clock.step(1)

      loadInstrMem(dut, TestKernels.kernel1)

      dut.clock.step(1)

      dut.io.start.valid.poke(true.B)
      dut.io.start.data.poke("b1111".U)

      dut.clock.step(1)

      dut.io.start.valid.poke(false.B)
      dut.io.start.data.poke(0.U)
      dut.io.start.ready.expect(false.B)

      dut.clock.step(100)

      dut.io.start.ready.expect(true.B)
    }
  }
}
