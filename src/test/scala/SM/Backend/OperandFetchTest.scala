package SM.Backend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class OperandFetchTest extends AnyFlatSpec with ChiselScalatestTester {
  "OperandFetch" should "fetch correct operands" in {
    test(new OperandFetch(4, 1)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.wb.we.poke(false.B)
      dut.io.wb.warp.poke(0.U)
      dut.io.wb.writeAddr.poke(0.U)
      dut.io.wb.writeMask.poke(0.U)
      dut.io.wb.writeData.poke(0.U)
      dut.io.iss.warp.poke(0.U)
      dut.io.iss.opcode.poke(0.U)
      dut.io.iss.dest.poke(0.U)
      dut.io.iss.rs1.poke(0.U)
      dut.io.iss.rs2.poke(0.U)
      dut.io.iss.rs3.poke(0.U)
      dut.io.iss.imm.poke(0.S)

      dut.clock.step(1)

      // Write some data
      dut.io.wb.we.poke(true.B)
      dut.io.wb.warp.poke(1.U)
      dut.io.wb.writeAddr.poke(4.U)
      dut.io.wb.writeMask.poke(0.U)
      dut.io.wb.writeData.poke("hdeadbeef".U)

      dut.clock.step(1)

      // Write some data again
      dut.io.wb.we.poke(true.B)
      dut.io.wb.warp.poke(1.U)
      dut.io.wb.writeAddr.poke(17.U)
      dut.io.wb.writeMask.poke(0.U)
      dut.io.wb.writeData.poke("hcafebabe".U)

      dut.clock.step(1)

      // Write last data
      dut.io.wb.we.poke(true.B)
      dut.io.wb.warp.poke(1.U)
      dut.io.wb.writeAddr.poke(23.U)
      dut.io.wb.writeMask.poke(0.U)
      dut.io.wb.writeData.poke("hdeadbabe".U)

      dut.clock.step(1)

      dut.io.wb.we.poke(false.B)
      dut.io.iss.warp.poke(1.U)
      dut.io.iss.opcode.poke("b11111".U)
      dut.io.iss.dest.poke(2.U)
      dut.io.iss.rs1.poke(4.U)
      dut.io.iss.rs2.poke(17.U)
      dut.io.iss.rs3.poke(23.U)
      dut.io.iss.imm.poke(255.S)

      dut.clock.step(1)

      dut.io.aluOf.warp.expect(1.U)
      dut.io.aluOf.dest.expect(2.U)
      dut.io.aluOf.opcode.expect("b11111".U)
      dut.io.aluOf.rs1.expect("hdeadbeef".U)
      dut.io.aluOf.rs2.expect("hcafebabe".U)
      dut.io.aluOf.rs3.expect("hdeadbabe".U)
      dut.io.aluOf.imm.expect(255.S)
    }
  }
}

