package SM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SmTest extends AnyFlatSpec with ChiselScalatestTester {
  "WarpTable" should "hold and update entries" in {
    test(new Sm(4, 8, 2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // TODO: Implement test
    }
  }
}
