package SM.Backend.Alu

import chisel3._
import chiseltest._
import Constants.AluOps
import org.scalatest.flatspec.AnyFlatSpec


class AluTest extends AnyFlatSpec with ChiselScalatestTester {
  "Alu" should "add two numbers" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(AluOps.ADD.asUInt(5.W))

      dut.io.out.expect(15.S)
      dut.io.zero.expect(false.B)
    }
  }

  "Alu" should "subtract two numbers" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(AluOps.SUB.asUInt(5.W))

      dut.io.out.expect(5.S)
      dut.io.zero.expect(false.B)
    }
  }

  "Alu" should "perform bitwise AND operation" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(AluOps.AND.asUInt(5.W))

      dut.io.out.expect(0.S)
      dut.io.zero.expect(true.B)
    }
  }

  "Alu" should "perform bitwise OR operation" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(AluOps.OR.asUInt(5.W))

      dut.io.out.expect(15.S)
      dut.io.zero.expect(false.B)
    }
  }

  "Alu" should "perform logical right shift operation" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(4.S)
      dut.io.b.poke(1.S)
      dut.io.op.poke(AluOps.SRL.asUInt(5.W))

      dut.io.out.expect(2.S)
      dut.io.zero.expect(false.B)
    }
  }

  "Alu" should "perform logical left shift operation" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(4.S)
      dut.io.b.poke(1.S)
      dut.io.op.poke(AluOps.SLL.asUInt(5.W))

      dut.io.out.expect(8.S)
      dut.io.zero.expect(false.B)
    }
  }
}
