package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// TODO: Rewrite test
class InstructionFetchTest extends AnyFlatSpec with ChiselScalatestTester {
  "InstructionFetch" should "fetch the instructions in correct order" in {
    test(new InstructionFetch(4, 4)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
//      // Default assignments
//      dut.io.scheduler.warp.poke(0.U)
//      dut.io.scheduler.stall.poke(true.B)
//      dut.io.instrMem.data.poke(0.U)
//
//      dut.clock.step(1)
//
//      // Set valid warps and load instructions
//      dut.io.scheduler.setValid.poke(true.B)
//      dut.io.scheduler.setValidWarps.poke("b0001".U)
//
//      dut.clock.step(1)
//
//      dut.io.scheduler.setValid.poke(false.B)
//
//      dut.clock.step(1)
//
//      // Let the fetch stage fetch instructions
//      dut.io.scheduler.stall.poke(false.B)
//      dut.io.scheduler.warp.poke(0.U)
//
//      dut.io.instrMem.addr.expect(0.U)
//      dut.io.instrMem.data.poke(0.U)
//
//      dut.clock.step(1)
//
//      dut.io.instrMem.addr.expect(1.U)
//      dut.io.instrMem.data.poke("hdeadbeef".U)
//
//      // Expect the first warp and the first instruction
//      dut.io.instrF.pc.expect(0.U)
//      dut.io.instrF.warp.expect(0.U)
//      dut.io.instrF.valid.expect(true.B)
//      dut.io.instrF.instr.expect("hdeadbeef".U)
//
//      dut.clock.step(1)
//
//      dut.io.instrMem.addr.expect(2.U)
//      dut.io.instrMem.data.poke("hcafebabe".U)
//
//      // Expect the first warp and the second instruction
//      dut.io.instrF.pc.expect(1.U)
//      dut.io.instrF.warp.expect(0.U)
//      dut.io.instrF.valid.expect(true.B)
//      dut.io.instrF.instr.expect("hcafebabe".U)
//
//      dut.clock.step(1)
//
//      dut.io.instrMem.addr.expect(3.U)
//      dut.io.instrMem.data.poke("hdeadbaff".U)
//
//      // Expect the first warp and the second instruction
//      dut.io.instrF.pc.expect(2.U)
//      dut.io.instrF.warp.expect(0.U)
//      dut.io.instrF.valid.expect(true.B)
//      dut.io.instrF.instr.expect("hdeadbaff".U)
//
//      dut.clock.step(5)
//
//      // Expect an NOP
//      dut.io.instrF.warp.expect(0.U)
//      dut.io.instrF.instr.expect(0.U)
//
//      dut.clock.step(1)
//
//      // Set first warp as inactive
//      dut.io.wbIfCtrl.setInactive.poke(true.B)
//
//      dut.clock.step(1)
//
//      // Expect that the correct warp entry was updated in the warp table
//      dut.io.wbIfCtrl.setInactive.poke(false.B)
//      dut.io.warpTableStatus.active.expect("b1110".U)
//
//      dut.clock.step(1)
    }
  }
}
