package SM.Backend

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BackendTest extends AnyFlatSpec with ChiselScalatestTester{
  "Backend" should "work" in {
    test(new Backend(2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => {

      }
    }
  }
}
