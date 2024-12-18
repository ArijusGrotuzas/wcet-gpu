package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WarpTableTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpTable" should "should hold and update entries" in {
    test(new WarpTable(8, 3)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      val validBits = "b00001111"

      // Set valid bits
      dut.io.setValid.poke(true.B)
      dut.io.valid.poke(validBits.U)

      dut.clock.step(1)

      // Update warp 0 with new pc and set it as done
      dut.io.update.poke(true.B)
      dut.io.warp.poke(0.U)
      dut.io.newPc.poke(2.U)
      dut.io.toggleDone.poke(true.B)

      dut.clock.step(1)

      // Expect that warp 0 has pc of 2 and that it is done
      dut.io.update.poke(false.B)
      dut.io.warp.poke(0.U)
      dut.io.pcOut.expect(2.U)
      dut.io.doneOut(0).expect(true.B)

      dut.clock.step(1)

      // Update warp 1 with new pc and set it as inactive
      dut.io.update.poke(true.B)
      dut.io.warp.poke(1.U)
      dut.io.newPc.poke(4.U)
      dut.io.toggleActive.poke(true.B)

      dut.clock.step(1)

      // Expect that warp 1 has pc of 4 and that it is active
      dut.io.update.poke(false.B)
      dut.io.warp.poke(1.U)
      dut.io.pcOut.expect(4.U)
      dut.io.activeOut(1).expect(false.B)

      dut.clock.step(1)

      // Update warp 2 with new pc and set it as inactive
      dut.io.update.poke(true.B)
      dut.io.warp.poke(2.U)
      dut.io.newPc.poke(8.U)
      dut.io.togglePending.poke(true.B)

      dut.clock.step(1)

      // Expect that warp 2 has pc of 8 and that it is pending
      dut.io.update.poke(false.B)
      dut.io.warp.poke(1.U)
      dut.io.pcOut.expect(4.U)
      dut.io.activeOut(1).expect(false.B)

      dut.clock.step(1)

      // Expect the correct valid bits
      for (i <- 1 until validBits.length()) {
        dut.io.validOut(i-1).expect(validBits.charAt(validBits.length() - i).asDigit.B)
      }
    }
  }
}

// val valid = dut.io.valid.peek().litValue
// println(valid)
