import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SmTopTest extends AnyFlatSpec with ChiselScalatestTester {
  def dump(dut: SmTop): Unit = {
    dut.io.dump.poke(true.B)
    dut.clock.step(200)
  }

  "Sm" should "execute program 1" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/kernel1.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.sw.poke("b00001111".U)
      dut.io.ready.expect(true.B)

      // Step for a few clock cycles to allow the debounced signals to propagate
      dut.clock.step(20)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(100)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      dump(dut)
    }
  }

  "Sm" should "execute program 2" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/kernel2.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.sw.poke(1.U)
      dut.io.ready.expect(true.B)

      // Step for a few clock cycles to allow the debounced signals to propagate
      dut.clock.step(20)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(150)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute program 3" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/kernel3.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.sw.poke("b100110".U)
      dut.io.ready.expect(true.B)

      // Step for a few clock cycles to allow the debounced signals to propagate
      dut.clock.step(20)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(150)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute program 4" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/kernel4.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.sw.poke("b100110".U)
      dut.io.ready.expect(true.B)

      // Step for a few clock cycles to allow the debounced signals to propagate
      dut.clock.step(20)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(150)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute program 5" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/kernel5.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.sw.poke(1.U) // Single active warp and zero as block idx
      dut.io.ready.expect(true.B)

      // Step for a few clock cycles to allow the debounced signals to propagate
      dut.clock.step(20)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.sw.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(150)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }
}
