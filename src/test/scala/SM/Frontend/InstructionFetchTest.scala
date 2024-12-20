package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionFetchTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadMem(dut: InstructionFetch, program: Array[UInt]): Unit = {
    dut.io.loadInstr.poke(true.B)

    for (i <- program.indices) {
      dut.io.loadInstrAddr.poke(i)
      dut.io.loadInstrData.poke(program(i))
      dut.clock.step()
    }

    dut.io.loadInstr.poke(false.B)
  }

  "InstructionFetch" should "work" in {
    test(new InstructionFetch(4)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Set valid warps and load instructions
      dut.io.setValid.poke(true.B)
      dut.io.setValidData.poke("b0011".U)

      dut.clock.step(1)

      dut.io.setValid.poke(false.B)

      // Load the instructions
      loadMem(dut, Array("hdeadbeef".U, "hcafebabe".U, "hdeadbaff".U))

      dut.io.stall.poke(true.B)

      dut.clock.step(1)

      // Let the fetch stage fetch instructions
      dut.io.stall.poke(false.B)
      dut.io.fetchWarp.poke(0.U)

      dut.clock.step(1)

      // Expect the first warp and the first instruction
      dut.io.warpDec.expect(0.U)
      dut.io.instrDec.expect("hdeadbeef".U)

      dut.clock.step(1)

      // Expect the first warp and the second instruction
      dut.io.warpDec.expect(0.U)
      dut.io.instrDec.expect("hcafebabe".U)

      dut.clock.step(1)

      // Expect the first warp and the third instruction
      dut.io.warpDec.expect(0.U)
      dut.io.instrDec.expect("hdeadbaff".U)

      dut.clock.step(1)

      // Expect the first warp and an NOP
      dut.io.warpDec.expect(0.U)
      dut.io.instrDec.expect(0.U)

      dut.clock.step(1)

      // Set first warp as inactive
      dut.io.issueSetInactive.poke(true.B)

      dut.clock.step(1)

      // Expect that the correct warp entry was updated in the warp table
      dut.io.issueSetInactive.poke(false.B)
      dut.io.active.expect("b1110".U)
      dut.io.fetchWarp.poke(1.U)

      dut.clock.step(1)

      // Expect second warp and first instruction
      dut.io.issueSetPending.poke(true.B)
      dut.io.warpDec.expect(1.U)
      dut.io.instrDec.expect("hdeadbeef".U)

      dut.clock.step(1)

      // Expect second warp and second instruction, and expect that the warp has been set as pending
      dut.io.issueSetPending.poke(false.B)
      dut.io.pending.expect("b0010".U)
      dut.io.fetchWarp.poke(2.U)

      dut.io.warpDec.expect(1.U)
      dut.io.instrDec.expect("hcafebabe".U)

      dut.clock.step(1)

      dut.io.warpDec.expect(2.U)
      dut.io.instrDec.expect("hdeadbeef".U)

      dut.clock.step(1)
    }
  }
}
