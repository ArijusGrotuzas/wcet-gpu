package SM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// TODO: Add a way to assert correct output
// TODO: Create a higher level module which includes the SM and the instruction memory
class SmTest extends AnyFlatSpec with ChiselScalatestTester {
//  def loadInstrMem(dut: Sm, program: Array[String]): Unit = {
//    dut.io.loadInstr.en.poke(true.B)
//
//    for (i <- program.indices) {
//      dut.io.loadInstr.addr.poke(i)
//      dut.io.loadInstr.instr.poke(program(i).U(32.W))
//      dut.clock.step()
//    }
//
//    dut.io.loadInstr.en.poke(false.B)
//  }

  "Sm" should "execute program 1" in {
    test(new Sm(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//
//      dut.io.loadInstr.en.poke(false.B)
//      dut.io.loadInstr.instr.poke(0.U)
//      dut.io.loadInstr.addr.poke(0.U)
//      dut.io.start.valid.poke(false.B)
//      dut.io.start.data.poke(0.U)
//
//      dut.clock.step(1)
//
//      loadInstrMem(dut, TestKernels.kernel1)
//
//      dut.clock.step(1)
//
//      dut.io.start.valid.poke(true.B)
//      dut.io.start.data.poke("b1111".U)
//
//      dut.clock.step(1)
//
//      dut.io.start.valid.poke(false.B)
//      dut.io.start.data.poke(0.U)
//      dut.io.start.ready.expect(false.B)
//
//      dut.clock.step(100)
//
//      // Expect the SM to be done
//      dut.io.start.ready.expect(true.B)
    }
  }

  "Sm" should "execute program 2" in {
    test(new Sm(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//      dut.io.loadInstr.en.poke(false.B)
//      dut.io.loadInstr.instr.poke(0.U)
//      dut.io.loadInstr.addr.poke(0.U)
//      dut.io.start.valid.poke(false.B)
//      dut.io.start.data.poke(0.U)
//
//      dut.clock.step(1)
//
//      loadInstrMem(dut, TestKernels.kernel2)
//
//      dut.clock.step(1)
//
//      dut.io.start.valid.poke(true.B)
//      dut.io.start.data.poke("b0001".U)
//
//      dut.clock.step(1)
//
//      dut.io.start.valid.poke(false.B)
//      dut.io.start.data.poke(0.U)
//      dut.io.start.ready.expect(false.B)
//
//      dut.clock.step(200)
//
//      // Expect the SM to be done
//      dut.io.start.ready.expect(true.B)
    }
  }
}
