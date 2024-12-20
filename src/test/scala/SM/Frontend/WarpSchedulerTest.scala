package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WarpSchedulerTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpScheduler" should "schedule warps correctly" in {
    test(new WarpScheduler).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      dut.io.start.poke(true.B)
      dut.io.valid.poke("b0111".U)

      dut.clock.step(1)

      dut.io.active.poke("b1101".U)
      dut.io.pending.poke("b0000".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)
      dut.io.aluStall.poke(false.B)

      dut.io.warp.expect(0.U)

      dut.clock.step(1)

      dut.io.active.poke("b1101".U)
      dut.io.pending.poke("b0001".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)
      dut.io.aluStall.poke(false.B)

      dut.io.warp.expect(2.U)

      dut.clock.step(1)

      dut.io.active.poke("b1101".U)
      dut.io.pending.poke("b0100".U)
      dut.io.headInstrType.poke("b0000".U)
      dut.io.memStall.poke(false.B)
      dut.io.aluStall.poke(false.B)

      dut.io.warp.expect(0.U)

      dut.clock.step(1)

      dut.io.active.poke("b1101".U)
      dut.io.pending.poke("b0000".U)
      dut.io.headInstrType.poke("b0001".U)
      dut.io.memStall.poke(true.B)
      dut.io.aluStall.poke(false.B)

      dut.io.warp.expect(2.U)
    }
  }
}
