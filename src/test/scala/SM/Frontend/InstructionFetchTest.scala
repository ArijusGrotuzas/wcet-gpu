package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionFetchTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadMem(dut: InstructionFetch, program: Array[UInt]): Unit = {
    dut.io.loadInstr.poke(true.B)

    for (i <- program.indices) {
      dut.io.loadInstrAddr.poke(i)
      dut.io.loadInstrVal.poke(program(i))
      dut.clock.step()
    }

    dut.io.loadInstr.poke(false.B)
  }

  "InstructionFetch" should "work" in {
    test(new InstructionFetch(4)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Set valid warps and load instructions
      dut.io.setValid.poke(true.B)
      dut.io.valid.poke("b0011".U)

      dut.clock.step(1)

      // Load the instructions
      dut.io.setValid.poke(false.B)

      loadMem(dut, Array("hdeadbeef".U, "hcafebabe".U, "hdeadbaff".U))

      dut.clock.step(1)

      dut.io.start.poke(true.B)

      dut.clock.step(10)

      // TODO: Need to unset active bit for warp 0, to make the warp scheduler switch to warp 1

//      dut.io.warp.expect(0.U)
//      dut.io.instr.expect("hdeadbeef".U)
//      dut.io.ready.expect(false.B)
//      dut.io.stall.expect(false.B)
    }
  }
}
