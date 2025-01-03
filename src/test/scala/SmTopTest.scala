import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

// TODO: Add a way to assert correct output
class SmTopTest extends AnyFlatSpec with ChiselScalatestTester {
  "Sm" should "execute program 1" in {
    test(new SmTop(4, 8, 100, "src/test/scala/kernel1.hex")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke("b1111".U)
      dut.io.ready.expect(true.B)

      // Step for a few clock cycles to allow the debounced signals to propagate
      dut.clock.step(20)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(100)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }

  "Sm" should "execute program 2" in {
    test(new SmTop(4, 8, 100, "src/test/scala/kernel2.hex")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)

      dut.clock.step(1)

      // Start the SM
      dut.io.valid.poke(true.B)
      dut.io.data.poke(1.U)
      dut.io.ready.expect(true.B)

      // Step for a few clock cycles to allow the debounced signals to propagate
      dut.clock.step(20)

      // Reset the start signals
      dut.io.valid.poke(false.B)
      dut.io.data.poke(0.U)
      dut.io.ready.expect(false.B)

      dut.clock.step(150)

      // Expect the SM to be done
      dut.io.ready.expect(true.B)
    }
  }
}
