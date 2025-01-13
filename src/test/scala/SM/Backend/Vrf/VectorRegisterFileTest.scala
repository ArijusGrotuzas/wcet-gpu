package SM.Backend.Vrf

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class VectorRegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {
  "VectorRegisterFile" should "write and read register contents" in {
    test(new VectorRegisterFile(32, 1, 32)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Perform first write operation
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0001010".U) // w0, r10
      dut.io.writeData.poke("hdeadbeef".U)
      dut.io.writeMask.poke(1.U)

      dut.clock.step(1)

      // Perform second write operation
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0001101".U) // w0, r13
      dut.io.writeData.poke("hcafebabe".U)

      dut.clock.step(1)

      // Perform third write operation
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0010011".U) // w0, r19
      dut.io.writeData.poke("hdeadbabe".U)

      dut.clock.step(1)

      // Disable write
      dut.io.we.poke(0.B)

      dut.io.readAddr1.poke("b0001010".U)
      dut.io.readAddr2.poke("b0001101".U)
      dut.io.readAddr3.poke("b0010011".U)

      dut.clock.step(1)

      dut.io.readAddr1.poke("b0100100".U) // r4
      dut.io.readAddr2.poke("b0100101".U) // r5
      dut.io.readAddr3.poke("b0100111".U) // r7

      // Expect the read data to be correct
      dut.io.readData1.expect("hdeadbeef".U)
      dut.io.readData2.expect("hcafebabe".U)
      dut.io.readData3.expect("hdeadbabe".U)

      dut.clock.step(20)
    }
  }
}