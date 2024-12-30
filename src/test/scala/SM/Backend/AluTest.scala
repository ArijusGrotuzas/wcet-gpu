package SM.Backend

import SM.Isa
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AluTest extends AnyFlatSpec with ChiselScalatestTester {
  "Alu" should "add two numbers" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(Isa.ADD)

      dut.io.out.expect(15.S)
      dut.io.zero.expect(false.B)
    }
  }

  "Alu" should "subtract two numbers" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(Isa.SUB)

      dut.io.out.expect(5.S)
      dut.io.zero.expect(false.B)
    }
  }

  "Alu" should "perform bitwise AND operation" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(Isa.AND)

      dut.io.out.expect(0.S)
      dut.io.zero.expect(true.B)
    }
  }

  "Alu" should "perform bitwise OR operation" in {
    test(new Alu(32)) { dut =>
      dut.io.a.poke(10.S)
      dut.io.b.poke(5.S)
      dut.io.op.poke(Isa.OR)

      dut.io.out.expect(15.S)
      dut.io.zero.expect(false.B)
    }
  }

//  "Alu" should "perform bitwise XOR operation" in {
//    test(new Alu(32)) { dut =>
//      dut.io.a.poke(10.S)
//      dut.io.b.poke(5.S)
//      dut.io.op.poke(AluOps.XOR)
//
//      dut.io.out.expect(15.S)
//      dut.io.zero.expect(false.B)
//    }
//  }

//  "Alu" should "zero output should go high" in {
//    test(new Alu(32)) { dut =>
//      dut.io.a.poke(10.S)
//      dut.io.b.poke(10.S)
//      dut.io.op.poke(AluOps.XOR)
//
//      dut.io.out.expect(0.S)
//      dut.io.zero.expect(true.B)
//    }
//  }
}
