package SM.Backend.Mem

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MemPipelineTest extends AnyFlatSpec with ChiselScalatestTester {
  "MemPipeline" should "work" in {
    test(new MemPipeline(1, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // TODO: Implement
    }
  }
}
