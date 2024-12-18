package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionFetchTest extends AnyFlatSpec with ChiselScalatestTester {
  "InstructionFetch" should "work" in {
    test(new InstructionFetch(4)).withAnnotations(Seq( WriteVcdAnnotation )) { dut =>

    }
  }
}
