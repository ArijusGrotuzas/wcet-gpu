import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SmTopTest extends AnyFlatSpec with ChiselScalatestTester {
  def testProgram(testName: String, dut: SmTestTop, blockConfig: String, memDepth: Int, dumpMem: Boolean = false): Unit = {
    // Default DUT assignments
    dut.io.valid.poke(false.B)
    dut.io.data.poke(0.U)
    dut.clock.setTimeout(1500)

    dut.clock.step(1)

    // Start the SM
    dut.io.valid.poke(true.B)
    dut.io.data.poke(blockConfig.U)
    dut.io.ready.expect(true.B, "The SM is not ready upon initialization.\n")

    dut.clock.step(1)

    // Unset the start signals
    dut.io.valid.poke(false.B)
    dut.io.data.poke(0.U)
    dut.io.ready.expect(false.B, "The SM did not start processing.\n")

    var run = true
    val maxCycles = 2000
    var executionCycles = 0

    while (run) {
      // val pc = dut.io.dbg.pc.peekInt()
      // val warp = dut.io.dbg.warp.peekInt()
      // val instr = dut.io.dbg.instr.peekInt()
      // Predef.printf("warp: %d, pc: 0x%08x, instr: 0x%08x, valid: %b\n", warp, pc, instr, valid)

      dut.clock.step(1)

      executionCycles += 1
      run = dut.io.ready.peekInt() == 0

      assert(executionCycles < maxCycles, "Ran out of execution cycles")
    }

    if (dumpMem) {
      Predef.printf("Dumping data memory contents for %s:\n", testName)

      for (i <- 0 until memDepth) {
        dut.io.memDump.dumpAddr.poke(i.U)
        dut.clock.step(1)

        Predef.printf("%d: 0x%08x\n", i, dut.io.memDump.dumpData.peekInt())
      }
    }

    Predef.printf("Execution cycles for %s: %d\n", testName, executionCycles - 1)

    // Expect the SM to be done
    dut.io.ready.expect(true.B, "SM is not ready for new execution.\n")
  }

  "Sm" should "execute program 1" in {
    val dataMemDepth = 32
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/kernel1.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }

  "Sm" should "execute program 2" in {
    val dataMemDepth = 32
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/kernel2.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }

  "Sm" should "execute program 3" in {
    val dataMemDepth = 32
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/kernel3.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b100110", dataMemDepth)
    }
  }

  "Sm" should "execute program 4" in {
    val dataMemDepth = 32
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 6,
      warpSize = 8,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/kernel4.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b111111", dataMemDepth)
    }
  }

  "Sm" should "execute program 5" in {
    val dataMemDepth = 32
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/kernel5.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }

  "Sm" should "execute program 6" in {
    val dataMemDepth = 32
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 8,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/kernel6.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }

  "Sm" should "execute program 7" in {
    val dataMemDepth = 64
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 16,
      instrMemDepth = 64,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/kernel7.hex",
      dataFile = "hex/data/sequential.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }

  "Sm" should "execute axpy" in {
    val dataMemDepth = 256
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 16,
      instrMemDepth = 64,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/axpy.hex",
      dataFile = "hex/data/sequential.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }

  "Sm" should "execute hamming" in {
    val dataMemDepth = 256
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 16,
      instrMemDepth = 64,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/hamming.hex",
      dataFile = "hex/data/sequential.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }

  "Sm" should "execute fibonacci" in {
    val dataMemDepth = 256
    test(new SmTestTop(
      blockCount = 4,
      warpCount = 4,
      warpSize = 16,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/fibonacci.hex"
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(getTestName, dut, "b001111", dataMemDepth)
    }
  }
}
