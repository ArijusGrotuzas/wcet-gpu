package SM.Backend.Old

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec;

class ThreeStageOperandFetchTest extends AnyFlatSpec with ChiselScalatestTester {
  "OperandFetch" should "read from the correct addresses" in {
    test(new ThreeStageOperandFetch).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // ---- Write some data to VRF first ----
      // Write to the first address in the second bank
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0100000".U)
      dut.io.writeData.poke("hffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".U)

      dut.clock.step(1)

      // Write to the second address in the second bank
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0100001".U)
      dut.io.writeData.poke("h1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef".U)

      dut.clock.step(1)

      // Write to the third address in the second bank
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0100010".U)
      dut.io.writeData.poke("hdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef".U)

      dut.clock.step(1)

      // Reset write signals
      dut.io.we.poke(0.B)
      dut.io.writeAddr.poke("b0000000".U)
      dut.io.writeData.poke(0.U)

      dut.io.reg1Addr.poke("b0100000".U)
      dut.io.reg2Addr.poke("b0100001".U)
      dut.io.reg3Addr.poke("b0100010".U)

      dut.clock.step(1)

      dut.io.reg1Addr.poke("b0000000".U)
      dut.io.reg2Addr.poke("b0000001".U)
      dut.io.reg3Addr.poke("b0000010".U)

      dut.clock.step(1)

      dut.io.reg1Addr.poke("b1000000".U)
      dut.io.reg2Addr.poke("b1000001".U)
      dut.io.reg3Addr.poke("b1000010".U)

      dut.clock.step(1)

      dut.io.reg1Addr.poke("b1100000".U)
      dut.io.reg2Addr.poke("b1100001".U)
      dut.io.reg3Addr.poke("b1100010".U)

      dut.io.op1.expect("hffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".U)
      dut.io.op2.expect("h1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef".U)
      dut.io.op3.expect("hdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef".U)
    }
  }
}
