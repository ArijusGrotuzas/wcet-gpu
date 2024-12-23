package SM.Frontend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class InstructionDecodeTest extends AnyFlatSpec with ChiselScalatestTester {
  "InstructionDecode" should "work" in {
    test(new InstructionDecode(2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // Perform first write operation
    }
  }
}

