package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WarpSchedulerTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpScheduler" should "schedule warps correctly" in {
    test(new WarpScheduler(4, 4)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Default assignments
      dut.io.start.valid.poke(false.B)
      dut.io.start.data.poke(0.U)
      dut.io.warpTableStatus.valid.poke(0.U)
      dut.io.warpTableStatus.active.poke(0.U)
      dut.io.warpTableStatus.pending.poke(0.U)
      dut.io.headInstrType.poke(0.U)
      dut.io.memStall.poke(false.B)

      dut.clock.step(1)

      // Set valid warps and expect that correct warps are set
      dut.io.start.valid.poke(true.B)
      dut.io.start.data.poke("b0111".U)

      dut.io.scheduler.setValid.expect(true.B)
      dut.io.scheduler.setValidWarps.expect("b0111".U)
      // Expect the scheduler to still be ready
      dut.io.start.ready.expect(true.B)

      dut.clock.step(1)

      // De-assert valid
      dut.io.start.valid.poke(false.B)

      // Expect the scheduler to not be in the idle state
      dut.io.scheduler.setValid.expect(false.B)
      dut.io.scheduler.setValidWarps.expect(0.U)

      // Poke some data to allow scheduler to make a choice
      dut.io.warpTableStatus.valid.poke("b0111".U)
      dut.io.warpTableStatus.active.poke("b1101".U)
      dut.io.warpTableStatus.pending.poke("b0000".U)
      dut.io.headInstrType.poke("b0000".U)

      // Expect the first warp to be scheduled and the scheduler not stalled
      dut.io.scheduler.warp.expect(0.U)
      dut.io.scheduler.stall.expect(false.B)

      dut.clock.step(1)

      // Set the first warp now as pending and expect not to stall
      dut.io.warpTableStatus.active.poke("b1101".U)
      dut.io.warpTableStatus.pending.poke("b0001".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)

      dut.io.scheduler.warp.expect(2.U)
      dut.io.scheduler.stall.expect(false.B)

      dut.clock.step(1)

      // Set the third warp as pending and the second as inactive
      dut.io.warpTableStatus.active.poke("b1101".U)
      dut.io.warpTableStatus.pending.poke("b0100".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)

      // And since the last warp is not valid, expect the first warp to be scheduled instead
      dut.io.scheduler.warp.expect(0.U)
      dut.io.scheduler.stall.expect(false.B)

      dut.clock.step(1)

      // Set the first warp as having a memory instruction at the head of the queue
      dut.io.warpTableStatus.active.poke("b1101".U)
      dut.io.warpTableStatus.pending.poke("b0000".U)
      dut.io.headInstrType.poke("b0001".U)
      dut.io.memStall.poke(true.B)

      // Thus expect the third warp to be scheduled
      dut.io.scheduler.warp.expect(2.U)
      dut.io.scheduler.stall.expect(false.B)

      dut.clock.step(1)

      // Set all warps as having a memory instruction at the front of their queues
      dut.io.warpTableStatus.active.poke("b1101".U)
      dut.io.warpTableStatus.pending.poke("b0000".U)
      dut.io.headInstrType.poke("b1111".U)
      dut.io.memStall.poke(true.B)

      // Thus expect the stall signal to be asserted
      dut.io.scheduler.warp.expect(0.U)
      dut.io.scheduler.stall.expect(true.B)
    }
  }
}
