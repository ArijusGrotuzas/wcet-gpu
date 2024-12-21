package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WarpSchedulerTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpScheduler" should "schedule warps correctly" in {
    test(new WarpScheduler(4, 2)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      dut.io.start.valid.poke(true.B)
      dut.io.warpTable.valid.poke("b0111".U)

      dut.clock.step(1)

      dut.io.warpTable.active.poke("b1101".U)
      dut.io.warpTable.pending.poke("b0000".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)
      dut.io.aluStall.poke(false.B)

      dut.io.scheduler.warp.expect(0.U)

      dut.clock.step(1)

      dut.io.warpTable.active.poke("b1101".U)
      dut.io.warpTable.pending.poke("b0001".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)
      dut.io.aluStall.poke(false.B)

      dut.io.scheduler.warp.expect(2.U)

      dut.clock.step(1)

      dut.io.warpTable.active.poke("b1101".U)
      dut.io.warpTable.pending.poke("b0100".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)
      dut.io.aluStall.poke(false.B)

      dut.io.scheduler.warp.expect(0.U)

      dut.clock.step(1)

      dut.io.warpTable.active.poke("b1101".U)
      dut.io.warpTable.pending.poke("b0000".U)
      dut.io.headInstrType.poke("b0001".U)
      dut.io.memStall.poke(true.B)
      dut.io.aluStall.poke(false.B)

      dut.io.scheduler.warp.expect(2.U)
    }
  }
}
