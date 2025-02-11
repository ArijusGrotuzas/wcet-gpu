package SM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import scala.math.pow

class PredicateRegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {
  "PredicateRegisterFile" should "output always true predicate" in {
    val warpSize = 8
    val warpCount = 4
    test(new PredicateRegisterFile(warpCount, warpSize)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Default assignments
      dut.io.we.poke(false.B)
      dut.io.addrW.poke(0.U)
      dut.io.dataW.poke(0.U)
      dut.io.addr1R.poke(1.U)
      dut.io.addr2R.poke(2.U)

      dut.clock.step(1)

      // Read the first predicate register
      dut.io.addr1R.poke(0.U)
      dut.io.addr2R.poke(0.U)
      dut.io.data1R.expect((pow(2, warpSize).toInt - 1).U)
      dut.io.data2R.expect((pow(2, warpSize).toInt - 1).U)
    }
  }
}
