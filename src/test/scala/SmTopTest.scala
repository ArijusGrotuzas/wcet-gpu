import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SmTopTest extends AnyFlatSpec with ChiselScalatestTester {
  "Sm" should "execute program 1" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/instructions/kernel1.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b00001111".U)
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
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/instructions/kernel2.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

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
      instructionFile = "hex/instructions/kernel3.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b100110".U)
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
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
      instructionFile = "hex/instructions/kernel4.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b000011".U)
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
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
      instructionFile = "hex/instructions/kernel5.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke(1.U) // Single active warp and zero as block idx
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(150)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute saxpy" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/instructions/saxpy.hex",
      dataFile = "hex/data/saxpy.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b001111".U)
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(500)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute hamming" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/instructions/hamming.hex",
      dataFile = "hex/data/hamming.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b001111".U)
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(500)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute fibonacci" in {
    test(new SmTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 1024,
      dataMemDepth = 1024,
      freq = 100,
      baud = 50,
      instructionFile = "hex/instructions/fibonacci.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b001111".U)
      dut.io.ready.expect(true.B)

      dut.clock.step(1)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(600)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }
}
