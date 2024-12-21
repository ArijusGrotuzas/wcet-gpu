package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WarpTableTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpTable" should "hold and update entries" in {
    test(new WarpTable(8, 32)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Set valid bits
      val validBits = "b00001111"
      dut.io.validCtrl.set.poke(true.B)
      dut.io.validCtrl.data.poke(validBits.U)

      dut.clock.step(1)

      // Update warp 0 with new pc and set it as done
      val idx1 = 0
      dut.io.pcCtrl.update.poke(true.B)
      dut.io.pcCtrl.idx.poke(idx1.U)
      dut.io.pcCtrl.data.poke(2.U)
      dut.io.doneCtrl.set.poke(true.B)
      dut.io.doneCtrl.idx.poke(idx1.U)

      dut.clock.step(1)

      // Expect that warp 0 has pc of 2 and that it is done
      dut.io.doneCtrl.set.poke(false.B)
      dut.io.pcCtrl.update.poke(false.B)
      dut.io.pc(idx1).expect(2.U)
      dut.io.done.expect("b00000001".U)

      dut.clock.step(1)

      // Update warp 1 with new pc and set it as inactive
      val idx2 = 1
      dut.io.pcCtrl.update.poke(true.B)
      dut.io.pcCtrl.idx.poke(idx2.U)
      dut.io.pcCtrl.data.poke(4.U)
      dut.io.activeCtrl.set.poke(true.B)
      dut.io.activeCtrl.idx.poke(idx2.U)

      dut.clock.step(1)

      // Expect that warp 1 has pc of 4 and that it is inactive
      dut.io.activeCtrl.set.poke(false.B)
      dut.io.pcCtrl.update.poke(false.B)
      dut.io.pc(idx2).expect(4.U)
      dut.io.active.expect("b11111101".U)

      dut.clock.step(1)

      // Update warp 2 with new pc and set it as pending
      val idx3 = 2
      dut.io.pcCtrl.update.poke(true.B)
      dut.io.pcCtrl.idx.poke(idx3.U)
      dut.io.pcCtrl.data.poke(8.U)
      dut.io.pendingCtrlIssue.set.poke(true.B)
      dut.io.pendingCtrlIssue.idx.poke(idx3.U)

      dut.clock.step(1)

      // Expect that warp 2 has pc of 8 and that it is pending
      dut.io.pendingCtrlIssue.set.poke(false.B)
      dut.io.pcCtrl.update.poke(false.B)
      dut.io.pc(idx3).expect(8.U)
      dut.io.pending.expect("b00000100".U)

      dut.clock.step(1)

      // Expect the correct valid bits
      dut.io.valid.expect(validBits.U)

      dut.clock.step(1)
    }
  }
}
