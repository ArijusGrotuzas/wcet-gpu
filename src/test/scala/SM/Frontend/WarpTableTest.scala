package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WarpTableTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpTable" should "hold and update entries" in {
    test(new WarpTable(8, 4)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Set valid bits
      val validBits = "b00001111"
      dut.io.validCtrl.set.poke(true.B)
      dut.io.validCtrl.data.poke(validBits.U)

      dut.clock.step(1)

      // Update warp 0 with new pc and set it as done
      val idx1 = 0
      dut.io.doneCtrl.set.poke(true.B)
      dut.io.doneCtrl.idx.poke(idx1.U)

      dut.clock.step(1)

      // Expect that warp 0 has pc of 2 and that it is done
      dut.io.doneCtrl.set.poke(false.B)
      dut.io.done.expect("b00000001".U)

      dut.clock.step(1)

      // Update warp 1 with new pc and set it as inactive
      val idx2 = 1
      dut.io.activeCtrl.set.poke(true.B)
      dut.io.activeCtrl.idx.poke(idx2.U)

      dut.clock.step(1)

      // Expect that warp 1 has pc of 4 and that it is inactive
      dut.io.activeCtrl.set.poke(false.B)
      dut.io.active.expect("b11111101".U)

      dut.clock.step(1)

      // Update warp 2 with new pc and set it as pending
      val idx3 = 2
      dut.io.setPendingCtrl.set.poke(true.B)
      dut.io.setPendingCtrl.idx.poke(idx3.U)

      dut.clock.step(1)

      // Expect that warp 2 has pc of 8 and that it is pending
      dut.io.setPendingCtrl.set.poke(false.B)
      dut.io.pending.expect("b00000100".U)

      dut.clock.step(1)

      // Expect the correct valid bits
      dut.io.valid.expect(validBits.U)

      dut.clock.step(1)
    }
  }
}
