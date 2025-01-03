package SM.Backend

import chisel3._
import chiseltest._
import chisel3.util._
import org.scalatest.flatspec.AnyFlatSpec

class WriteBackTest extends AnyFlatSpec with ChiselScalatestTester {
  "WriteBack" should "route correct data" in {
    test(new WriteBack(1, 1)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
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

  "WriteBack" should "set correct control signals" in {
    test(new WriteBack(4, 1)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
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

      dut.io.alu.done.poke(true.B)
      dut.io.alu.warp.poke(2.U)

      dut.io.wbIf.setInactive.expect(true.B)
      dut.io.wbIf.setNotPending.expect(false.B)
      dut.io.wbIf.warp.expect(2.U)

      dut.clock.step(1)

      dut.io.alu.done.poke(false.B)
      dut.io.mem.valid.poke(true.B)
      dut.io.mem.warp.poke(1.U)
      dut.io.mem.pending.poke(true.B)

      dut.io.wbIf.setInactive.expect(false.B)
      dut.io.wbIf.setNotPending.expect(true.B)
      dut.io.wbIf.warp.expect(1.U)
    }
  }
}
