package SM.Backend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WriteBackTest extends AnyFlatSpec with ChiselScalatestTester {
  "InstructionDecode" should "route correct data" in {
    test(new WriteBack(1)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default signal assignments
      dut.io.alu.warp.poke(0.U)
      dut.io.alu.valid.poke(false.B)
      dut.io.alu.done.poke(false.B)
      dut.io.alu.dest.poke(0.U)
      dut.io.alu.out.poke(0.U)
      dut.io.mem.warp.poke(0.U)
      dut.io.mem.valid.poke(false.B)
      dut.io.mem.dest.poke(0.U)
      dut.io.mem.out.poke(0.U)

      dut.clock.step(1)

      // alu result valid and memory result not valid
      dut.io.alu.valid.poke(true.B)
      dut.io.alu.dest.poke(5.U)
      dut.io.alu.out.poke("hdeadbeef".U)

      dut.io.wbOf.we.expect(true.B)
      dut.io.wbOf.writeAddr.expect(5.U)
      dut.io.wbOf.writeData.expect("hdeadbeef".U)

      dut.clock.step(1)

      // alu result valid and memory result valid
      dut.io.alu.valid.poke(true.B)
      dut.io.alu.dest.poke(7.U)
      dut.io.alu.out.poke("hcafebabe".U)
      dut.io.mem.valid.poke(true.B)
      dut.io.mem.dest.poke(16.U)
      dut.io.mem.out.poke("hdeadbabe".U)

      dut.io.wbOf.we.expect(true.B)
      dut.io.wbOf.writeAddr.expect(16.U)
      dut.io.wbOf.writeData.expect("hdeadbabe".U)

      dut.clock.step(1)

      // alu result not valid and mem result not valid
      dut.io.alu.valid.poke(false.B)
      dut.io.alu.dest.poke(0.U)
      dut.io.alu.out.poke(0.U)
      dut.io.mem.valid.poke(false.B)
      dut.io.mem.dest.poke(0.U)
      dut.io.mem.out.poke(0.U)

      dut.io.wbOf.we.expect(true.B)
      dut.io.wbOf.writeAddr.expect(7.U)
      dut.io.wbOf.writeData.expect("hcafebabe".U)

      dut.clock.step(1)

      // alu result valid and mem result not valid
      dut.io.alu.valid.poke(true.B)
      dut.io.alu.dest.poke(23.U)
      dut.io.alu.out.poke("hbabecafe".U)

      dut.io.wbOf.we.expect(true.B)
      dut.io.wbOf.writeAddr.expect(23.U)
      dut.io.wbOf.writeData.expect("hbabecafe".U)
    }
  }
}
