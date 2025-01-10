package SM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LsuArbiterTest extends AnyFlatSpec with ChiselScalatestTester {
  "SM.LsuArbiter" should "execute program 1" in {
    val lsuCount = 4
    val addrLen = 4
    test(new LsuArbiter(lsuCount, addrLen)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val allCombinations = (0 until math.pow(2, lsuCount).toInt)
      val addr = (0 until lsuCount).map(i => i << (addrLen * i)).sum
      val writeData = (0 until math.pow(2, lsuCount).toInt).map(i => i << 32).sum

      // Default Assignments
      dut.io.lsu.readReq.poke(0.U)
      dut.io.lsu.writeReq.poke(0.U)
      dut.io.lsu.writeData.poke(0.U)
      dut.io.lsu.addr.poke(0.U)
      dut.io.dataMem.dataR.poke(0.U)

      // Read request
      for(comb <- allCombinations) {
        println(s"Read request combination: ${comb.toBinaryString}")

        dut.io.lsu.addr.poke(addr.U)
        dut.io.lsu.readReq.poke(comb.U)

        // Step for one clock cycle to enter arbitration state
        dut.clock.step(1)

        // Read Arbitrate state
        println ("Read address: " + dut.io.dataMem.addr.peekInt())

        // Step for one clock cycle to enter read done state
        dut.clock.step(1)

        // Read Done state

        dut.clock.step(1)

        // Read Arbitrate state

        // Reset all requests
        dut.io.lsu.readReq.poke(0.U)

        dut.clock.step(2)
      }

      println("--------------------")

      dut.clock.step(1)

      // Write request
      for(comb <- allCombinations) {
        println(s"Write request combination: ${comb.toBinaryString}")

        dut.io.lsu.addr.poke(addr.U)
        dut.io.lsu.writeReq.poke(comb.U)
        dut.io.lsu.writeData.poke(writeData.U)

        // Step for one clock cycle to enter arbitration state
        dut.clock.step(1)

        // Write Arbitrate state
        println ("Read address: " + dut.io.dataMem.addr.peekInt())

        // Step for one clock cycle to enter read done state
        dut.clock.step(1)

        // Write Done state

        dut.clock.step(1)

        // Write Arbitrate state

        // Reset all requests
        dut.io.lsu.writeReq.poke(0.U)

        dut.clock.step(2)
      }
    }
  }
}
