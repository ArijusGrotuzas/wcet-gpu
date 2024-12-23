package SM.Backend.Old

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class VectorRegisterFileOldTest extends AnyFlatSpec with ChiselScalatestTester {
  "VectorRegisterFile" should "allow simultaneous read and write operations" in {
    test(new VectorRegisterFileOld(32, 32, 7)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>
      // Perform first write operation
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0000001".U)
      dut.io.writeData.poke("hdeadbeef".U)
      dut.io.writeMask.poke(0.U)

      dut.clock.step(1)

      // Perform second write operation
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0100001".U)
      dut.io.writeData.poke("hcafebabe".U)

      dut.clock.step(1)

      // Perform third write operation
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b1000001".U)
      dut.io.writeData.poke("hdeadbabe".U)

      dut.clock.step(1)

      // Reset read signals
      dut.io.we.poke(0.B)
      dut.io.writeAddr.poke("b0000000".U)
      dut.io.writeData.poke("h00000000".U)

      dut.io.readAddr1.poke("b0000001".U)
      dut.io.readAddr2.poke("b0100001".U)
      dut.io.readAddr3.poke("b1000001".U)

      dut.clock.step(1)

      dut.io.readAddr1.poke("b1000001".U)
      dut.io.readAddr2.poke("b000001".U)
      dut.io.readAddr3.poke("b0100001".U)

      // Expect the read data to be correct
      dut.io.readData1.expect("hdeadbeef".U)
      dut.io.readData2.expect("hcafebabe".U)
      dut.io.readData3.expect("hdeadbabe".U)

      dut.clock.step(1)
    }
  }

  "VectorRegisterFile" should "write and read data correctly across different banks" in {
    test(new VectorRegisterFileOld(32, 16, 7)) { dut =>
      def calcAddr(bankIdx: Int, addr: Int): Int = (bankIdx << 5) | addr

      // Write to each bank
      for (bankIdx <- 0 until 4) {
        dut.io.we.poke(true.B)
        dut.io.writeAddr.poke(calcAddr(bankIdx, bankIdx).U)
        dut.io.writeMask.poke(0.U)
        dut.io.writeData.poke(bankIdx.U)
        
        dut.clock.step()
      }

      // Read from each bank
      for (bankIdx <- 0 until 4) {
        dut.io.readAddr1.poke(calcAddr(bankIdx, bankIdx).U)
        dut.io.readAddr2.poke(calcAddr(bankIdx ^ 1, bankIdx).U)
        dut.io.readAddr3.poke(calcAddr(bankIdx ^ 2, bankIdx).U)

        dut.clock.step()

        dut.io.readAddr1.poke(calcAddr(0, 0).U)
        dut.io.readAddr2.poke(calcAddr(1, 0).U)
        dut.io.readAddr3.poke(calcAddr(2, 0).U)

        dut.io.readData1.expect(bankIdx.U)
      }
    }
  }
}