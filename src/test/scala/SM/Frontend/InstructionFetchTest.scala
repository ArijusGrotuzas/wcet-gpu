package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionFetchTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadMem(dut: InstructionFetch, program: Array[UInt]): Unit = {
    dut.io.loadInstr.en.poke(true.B)

    for (i <- program.indices) {
      dut.io.loadInstr.addr.poke(i)
      dut.io.loadInstr.instr.poke(program(i))
      dut.clock.step()
    }

    dut.io.loadInstr.en.poke(false.B)
  }

  "InstructionFetch" should "work" in {
    test(new InstructionFetch(4)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Set valid warps and load instructions
      dut.io.scheduler.setValid.poke(true.B)
      dut.io.scheduler.validWarps.poke("b0011".U)

      dut.clock.step(1)

      dut.io.scheduler.setValid.poke(false.B)

      // Load the instructions
      loadMem(dut, Array("hdeadbeef".U, "hcafebabe".U, "hdeadbaff".U))

      dut.io.scheduler.stall.poke(true.B)

      dut.clock.step(1)

      // Let the fetch stage fetch instructions
      dut.io.scheduler.stall.poke(false.B)
      dut.io.scheduler.warp.poke(0.U)

      dut.clock.step(1)

      // Expect the first warp and the first instruction
      dut.io.instrF.warp.expect(0.U)
      dut.io.instrF.instr.expect("hdeadbeef".U)

      dut.clock.step(1)

      // Expect the first warp and the second instruction
      dut.io.instrF.warp.expect(0.U)
      dut.io.instrF.instr.expect("hcafebabe".U)

      dut.clock.step(1)

      // Expect the first warp and the third instruction
      dut.io.instrF.warp.expect(0.U)
      dut.io.instrF.instr.expect("hdeadbaff".U)

      dut.clock.step(1)

      // Expect the first warp and an NOP
      dut.io.instrF.warp.expect(0.U)
      dut.io.instrF.instr.expect(0.U)

      dut.clock.step(1)

      // Set first warp as inactive
      dut.io.issCtrl.setInactive.poke(true.B)

      dut.clock.step(1)

      // Expect that the correct warp entry was updated in the warp table
      dut.io.issCtrl.setInactive.poke(false.B)
      dut.io.warpTable.active.expect("b1110".U)
      dut.io.scheduler.warp.poke(1.U)

      dut.clock.step(1)

      // Expect second warp and first instruction
      dut.io.issCtrl.setPending.poke(true.B)
      dut.io.instrF.warp.expect(1.U)
      dut.io.instrF.instr.expect("hdeadbeef".U)

      dut.clock.step(1)

      // Expect second warp and second instruction, and expect that the warp has been set as pending
      dut.io.issCtrl.setPending.poke(false.B)
      dut.io.warpTable.pending.expect("b0010".U)
      dut.io.scheduler.warp.poke(2.U)

      dut.io.instrF.warp.expect(1.U)
      dut.io.instrF.instr.expect("hcafebabe".U)

      dut.clock.step(1)

      dut.io.instrF.warp.expect(2.U)
      dut.io.instrF.instr.expect("hdeadbeef".U)

      dut.clock.step(1)
    }
  }
}
