package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FrontendTest extends AnyFlatSpec with ChiselScalatestTester {
  def loadMem(dut: Frontend, program: Array[UInt]): Unit = {
    dut.io.loadInstr.en.poke(true.B)

    for (i <- program.indices) {
      dut.io.loadInstr.addr.poke(i)
      dut.io.loadInstr.instr.poke(program(i))
      dut.clock.step()
    }

    dut.io.loadInstr.en.poke(false.B)
  }

  "Frontend" should "work" in {
    test(new Frontend(4)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => {
      // Default assignments
      dut.io.loadInstr.en.poke(false.B)
      dut.io.loadInstr.instr.poke(0.U)
      dut.io.loadInstr.addr.poke(0.U)
      dut.io.wb.setNotPending.poke(false.B)
      dut.io.wb.warp.poke(0.U)
      dut.io.start.valid.poke(false.B)
      dut.io.start.data.poke(0.U)
      dut.io.memStall.poke(false.B)
      dut.io.aluStall.poke(false.B)

      dut.clock.step(1)

      // Load the instructions
      loadMem(dut, Array("hdeadbeef".U, "hcafebabe".U, "hdeadbaff".U))

      dut.clock.step(1)

      // Start computation
      dut.io.start.ready.expect(true.B)
      dut.io.start.valid.poke(true.B)
      dut.io.start.data.poke("b0011".U)

      dut.clock.step(1)

      // Expect ready to be de-asserted
      dut.io.start.ready.expect(false.B)
      dut.io.start.valid.poke(false.B)

      dut.clock.step(20)
      }
    }
  }
}
