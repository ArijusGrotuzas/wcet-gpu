package SM.Backend

import SM.Opcodes
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BackendTest extends AnyFlatSpec with ChiselScalatestTester {
  def pushInstruction(
                       dut: Backend,
                       warp: Int,
                       opcode: UInt,
                       dest: Int = 0,
                       source1: Int = 0,
                       source2: Int = 0,
                       source3: Int = 0,
                       immediate: Int = 0
                     ): Unit = {
    dut.io.front.warp.poke(warp.U)
    dut.io.front.opcode.poke(opcode)
    dut.io.front.dest.poke(dest.U)
    dut.io.front.rs1.poke(source1.U)
    dut.io.front.rs2.poke(source2.U)
    dut.io.front.rs3.poke(source3.U)
    dut.io.front.imm.poke(immediate.U)
  }

  "Backend" should "process arithmetic instructions" in {
    test(new Backend(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.front.warp.poke(0.U)
      dut.io.front.opcode.poke(0.U)
      dut.io.front.dest.poke(0.U)
      dut.io.front.rs1.poke(0.U)
      dut.io.front.rs2.poke(0.U)
      dut.io.front.rs3.poke(0.U)
      dut.io.front.imm.poke(0.U)

      dut.clock.step(1)

      // Send an ADDI instruction
      pushInstruction(dut, 0, Opcodes.ADDI, 19, 0, 0, 0, 256)

      dut.clock.step(1)

      // Send an ADDI instruction
      pushInstruction(dut, 0, Opcodes.ADDI, 11, 0, 0, 0, 119)

      dut.clock.step(1)

      // Send ADD instruction
      pushInstruction(dut, 0, Opcodes.ADD, 3, 11, 19)

      dut.clock.step(1)

      // Send RET instruction
      pushInstruction(dut, 0, Opcodes.RET)

      dut.clock.step(1)

      // Send AND instruction
      pushInstruction(dut, 1, Opcodes.AND, 19, 20, 25)

      dut.clock.step(1)

      // Send OR instruction
      pushInstruction(dut, 2, Opcodes.OR, 7, 28, 31)

      dut.clock.step(20)
    }
  }
}
