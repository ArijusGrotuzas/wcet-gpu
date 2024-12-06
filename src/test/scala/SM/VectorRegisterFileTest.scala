package SM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class VectorRegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {
  "VectorRegisterFile" should "work" in {
    test(new VectorRegisterFile(1024, 32))
      .withAnnotations (Seq( WriteVcdAnnotation )) { dut =>
      dut.io.we.poke(1.B)

      dut.io.writeAddr.poke("b0100000001".U)
      dut.io.writeData.poke(22.U)
      dut.io.writeMask.poke(0.U)

      dut.clock.step(1)

      dut.io.we.poke(0.B)

      dut.io.readAddr1.poke("b0100000001".U)
      dut.io.readAddr2.poke("b0000000001".U)
      dut.io.readAddr3.poke("b1000000001".U)

      dut.clock.step(1)

      println(dut.io.readData1.peek().litValue.toLong)
      println(dut.io.readData2.peek().litValue.toLong)
      println(dut.io.readData3.peek().litValue.toLong)
    }
  }
}