package SM.Backend

import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

class AluPipelineTest extends AnyFlatSpec with ChiselScalatestTester {
  "AluPipeline" should "add two numbers" in {
    test(new AluPipeline(32)) { dut =>
    }
  }
}
