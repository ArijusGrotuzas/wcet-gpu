import chisel3._
import chisel3.util._

class SmTopDe2115(
                   blockCount: Int,
                   warpCount: Int,
                   warpSize: Int,
                   instrMemDepth: Int,
                   dataMemDepth: Int,
                   freq: Int,
                   baud: Int,
                   instructionFile: String = "",
                   dataFile: String = ""
                 ) extends Module {
  private val blockAddrLen = log2Up(blockCount)
  val io = IO(new Bundle {
    // Inputs
    val valid = Input(Bool())
    val dump = Input(Bool())
    val sw = Input(UInt((blockAddrLen + warpCount).W))
    // Outputs
    val tx = Output(Bool())
    val ready = Output(Bool())
    val cycles = Output(UInt(18.W))
  })

  def cycleCounter(ready: Bool, done: Bool): UInt = {
    val sIdle :: sCount :: Nil = Enum(2)
    val stateReg = RegInit(sIdle)

    val cycleCount = RegInit(0.U(18.W))
    val startCount = !ready & RegNext(ready)

    switch(stateReg) {
      is(sIdle) {
        when(startCount) {
          stateReg := sCount
          cycleCount := 0.U
        }
      }
      is(sCount) {
        cycleCount := cycleCount + 1.U

        when(done) {
          stateReg := sIdle
        }
      }
    }

    cycleCount
  }

  val debSw = Module(new DebounceSw(blockAddrLen + warpCount, freq))
  val smTop = Module(new SmTop(blockCount, warpCount, warpSize, instrMemDepth, dataMemDepth, freq, baud, instructionFile, dataFile))

  debSw.io.sw := io.sw

  // Must invert the signals since the debounced buttons on the DE2-115 board are high when not pressed
  smTop.io.dump := !io.dump
  smTop.io.valid := !io.valid
  smTop.io.data := debSw.io.swDb

  // Cycle counter
  val cycleCount = cycleCounter(smTop.io.ready, smTop.io.done)

  io.tx := smTop.io.tx
  io.ready := smTop.io.ready
  io.cycles := cycleCount
}

/**
 * @see https://github.com/chipsalliance/firrtl/issues/2168
 */
object SmTopDe2115 extends App {
  println("Generating the SM hardware for the DE2-115 board")
  (new chisel3.stage.ChiselStage).emitVerilog(new SmTopDe2115(4, 4, 8, 128, 128, 50000000, 115200, "bootkernel.hex", "bootdata.hex"), Array("--target-dir", "generated", "--no-dedup"))
}
