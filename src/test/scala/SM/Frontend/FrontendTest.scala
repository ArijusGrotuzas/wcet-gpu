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
      loadMem(dut, Array(
          "b00000000000010111101010110001111".U, // 0000000 00000 10111 10101 01100 01111
          "b00000000000111011001100110111110".U, // 0000000 00001 11011 00110 01101 11110
          "b00000000001000011001010111011111".U  // 0000000 00010 00011 00101 01110 11111
        )
      )

      dut.clock.step(1)

      // Start computation
      dut.io.start.ready.expect(true.B)
      dut.io.start.valid.poke(true.B)
      dut.io.start.data.poke("b0011".U)

      dut.clock.step(1)

      // Expect ready to be de-asserted
      dut.io.start.ready.expect(false.B)
      dut.io.start.valid.poke(false.B)

      dut.clock.step(6)

      dut.io.front.warp.expect(0.U)
      dut.io.front.opcode.expect("b01111".U)
      dut.io.front.dest.expect("b01100".U)
      dut.io.front.rs1.expect("b10101".U)
      dut.io.front.rs2.expect("b10111".U)
      dut.io.front.rs3.expect("b00000".U)
      dut.io.front.imm.expect(0.U)

      dut.clock.step(1)

      dut.io.front.warp.expect(0.U)
      dut.io.front.opcode.expect("b11110".U)
      dut.io.front.dest.expect("b01101".U)
      dut.io.front.rs1.expect("b00110".U)
      dut.io.front.rs2.expect("b11011".U)
      dut.io.front.rs3.expect("b00001".U)
      dut.io.front.imm.expect(0.U)

      dut.clock.step(1)

      // Expect a RET instruction
      dut.io.front.warp.expect(0.U)
      dut.io.front.opcode.expect("b11111".U)
      dut.io.front.dest.expect("b01110".U)
      dut.io.front.rs1.expect("b00101".U)
      dut.io.front.rs2.expect("b00011".U)
      dut.io.front.rs3.expect("b00010".U)
      dut.io.front.imm.expect(0.U)

      dut.clock.step(1)

      // Expect an NOP after issuing the RET instruction
      dut.io.front.warp.expect(0.U)
      dut.io.front.opcode.expect(0.U)

      dut.clock.step(3)

      // Set the warp as inactive
      dut.io.wb.setInactive.poke(true.B)
      dut.io.wb.warp.poke(0.U)

      dut.clock.step(1)

      dut.io.wb.setInactive.poke(false.B)
      dut.io.wb.warp.poke(0.U)
      }
    }
  }
}
