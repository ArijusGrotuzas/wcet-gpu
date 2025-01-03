import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// TODO: Add a way to assert correct output
class TopSimTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadInstrMem(dut: TopSim, program: Array[String]): Unit = {
    dut.io.loadInstrEn.poke(true.B)

    for (i <- program.indices) {
      dut.io.loadInstrAddr.poke(i.U)
      dut.io.loadInstr.poke(program(i).U(32.W))
      dut.clock.step()
    }

    dut.io.loadInstrEn.poke(false.B)
  }

  "Sm" should "execute program 1" in {
    test(new TopSim(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.loadInstr.poke(0.U)
      dut.io.loadInstrEn.poke(false.B)
      dut.io.loadInstrAddr.poke(0.U)

      dut.clock.step(1)

      // Load first test kernel
      loadInstrMem(dut, TestKernels.kernel1)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b1111".U)
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(100)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute program 2" in {
    test(new TopSim(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.loadInstr.poke(0.U)
      dut.io.loadInstrEn.poke(false.B)
      dut.io.loadInstrAddr.poke(0.U)

      dut.clock.step(1)

      // Load second test kernel
      loadInstrMem(dut, TestKernels.kernel2)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke(1.U)
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(200)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }
}
