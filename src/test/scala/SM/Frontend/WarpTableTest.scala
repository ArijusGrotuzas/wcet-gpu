package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WarpTableTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpTable" should "should hold and update entries" in {
    test(new WarpTable(8, 3)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Set valid bits
      val validBits = "b00001111"
      dut.io.setValid.poke(true.B)
      dut.io.valid.poke(validBits.U)

      dut.clock.step(1)

      // Update warp 0 with new pc and set it as done
      dut.io.warp.poke(0.U)
      dut.io.newPc.poke(2.U)
      dut.io.setDone.poke(true.B)

      dut.clock.step(1)

      // Expect that warp 0 has pc of 2 and that it is done
      dut.io.setDone.poke(false.B)
      dut.io.warp.poke(0.U)
      dut.io.pcOut.expect(2.U)
      dut.io.doneOut.expect("b00000001".U)

      dut.clock.step(1)

      // Update warp 1 with new pc and set it as inactive
      dut.io.warp.poke(1.U)
      dut.io.newPc.poke(4.U)
      dut.io.setInactive.poke(true.B)

      dut.clock.step(1)

      // Expect that warp 1 has pc of 4 and that it is active
      dut.io.setInactive.poke(false.B)
      dut.io.warp.poke(1.U)
      dut.io.pcOut.expect(4.U)
      dut.io.activeOut.expect("b11111101".U)

      dut.clock.step(1)

      // Update warp 2 with new pc and set it as inactive
      dut.io.warp.poke(2.U)
      dut.io.newPc.poke(8.U)
      dut.io.setPending.poke(true.B)

      dut.clock.step(1)

      // Expect that warp 2 has pc of 8 and that it is pending
      dut.io.setPending.poke(false.B)
      dut.io.warp.poke(2.U)
      dut.io.pcOut.expect(8.U)
      dut.io.pendingOut.expect("b00000100".U)

      dut.clock.step(1)

      // Expect the correct valid bits
      dut.io.validOut.expect(validBits.U)

      dut.clock.step(1)
    }
  }
}
