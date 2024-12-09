package SM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class VectorRegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {
  "VectorRegisterFile" should "allow simultaneous read and write operations" in {
    test(new VectorRegisterFile(32, 16, 7)) { dut =>
      // Perform a write operation
      dut.io.we.poke(1.B)
      dut.io.writeAddr.poke("b0100000".U)
      dut.io.writeData.poke(22.U)
      dut.io.writeMask.poke(0.U)

      // Assign default values to read addresses
      dut.io.readAddr1.poke("b1100000".U)
      dut.io.readAddr2.poke("b0000000".U)
      dut.io.readAddr3.poke("b1000000".U)

      dut.clock.step(5)

      // Expect the read data to be empty
      dut.io.readData1.expect(0.U)
      dut.io.readData2.expect(0.U)
      dut.io.readData3.expect(0.U)

      // Read from the same address that was written to
      dut.io.we.poke(0.B)
      dut.io.readAddr1.poke("b0100000".U)

      dut.clock.step(1)

      // Expect the correct read data
      dut.io.readData1.expect(22.U)
      dut.io.readData2.expect(0.U)
      dut.io.readData3.expect(0.U)
    }
  }

  "VectorRegisterFile" should "write and read data correctly across different banks" in {
    test(new VectorRegisterFile(32, 16, 7)) { dut =>
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

        dut.io.readData1.expect(bankIdx.U)
      }
    }
  }
}