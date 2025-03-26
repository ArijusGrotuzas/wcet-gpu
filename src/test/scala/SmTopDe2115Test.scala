import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SmTopDe2115Test extends AnyFlatSpec with ChiselScalatestTester {
  def testProgram(dut: SmTopDe2115, blockConfig: String, memDepth: Int, dumpMem: Boolean = false): Unit = {
    val maxCycles = 1500
    dut.clock.setTimeout(maxCycles)

    // Default DUT assignments
    dut.io.valid.poke(true.B)
    dut.io.dump.poke(true.B)
    dut.io.sw.poke(0.U)

    dut.clock.step(1)

    // Let the switches debounce
    dut.io.sw.poke(blockConfig.U)

    dut.clock.step(5)

    // Start the SM
    dut.io.valid.poke(false.B)
    dut.io.ready.expect(true.B, "The SM is not ready upon initialization.\n")

    dut.clock.step(1)

    // Unset the start signals
    dut.io.valid.poke(true.B)
    dut.io.sw.poke(0.U)
    dut.io.ready.expect(false.B, "The SM did not start processing.\n")

    var run = true
    var executionCycles = 0

    while (run && executionCycles < maxCycles) {
      dut.clock.step(1)

      executionCycles += 1
      run = dut.io.ready.peekInt() == 0

      assert(executionCycles < maxCycles, "Ran out of cycles")
    }

//    if (dumpMem) {
//      Predef.printf("Dumping data memory contents:\n")
//
//      for (i <- 0 until memDepth) {
//        dut.io.memDump.dumpAddr.poke(i.U)
//        dut.clock.step(1)
//
//        Predef.printf("%d: 0x%08x\n", i, dut.io.memDump.dumpData.peekInt())
//      }
//    }

    Predef.printf("Execution cycles: %d\n", executionCycles)

    // Expect the SM to be done
    dut.io.ready.expect(true.B, "SM did not finish in time.\n")
  }

  "Sm" should "execute axpy" in {
    val dataMemDepth = 1024
    test(new SmTopDe2115(
      blockCount = 4,
      warpCount = 8,
      warpSize = 16,
      instrMemDepth = 64,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/axpy.hex",
      dataFile = "hex/data/sequential.hex",
      freq = 100,
      baud = 1
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(dut, "b001111", dataMemDepth, dumpMem = false)
    }
  }

  "Sm" should "execute hamming" in {
    val dataMemDepth = 1024
    test(new SmTopDe2115(
      blockCount = 4,
      warpCount = 8,
      warpSize = 16,
      instrMemDepth = 64,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/hamming.hex",
      dataFile = "hex/data/sequential.hex",
      freq = 100,
      baud = 1
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(dut, "b001111", dataMemDepth, dumpMem = false)
    }
  }

  "Sm" should "execute fibonacci" in {
    val dataMemDepth = 1024
    test(new SmTopDe2115(
      blockCount = 4,
      warpCount = 8,
      warpSize = 16,
      instrMemDepth = 32,
      dataMemDepth = dataMemDepth,
      instructionFile = "hex/instructions/fibonacci.hex",
      freq = 100,
      baud = 1
    )).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      testProgram(dut, "b001111", dataMemDepth, dumpMem = false)
    }
  }
}
