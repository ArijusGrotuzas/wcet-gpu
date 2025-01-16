import chisel3._
import chisel3.util.log2Up

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
  })

  val debSw = Module(new DebounceSw(blockAddrLen + warpCount, freq))
  val smTop = Module(new SmTop(blockCount, warpCount, warpSize, instrMemDepth, dataMemDepth, freq, baud, instructionFile, dataFile))

  debSw.io.sw := io.sw

  // Must invert the signals since the debounced buttons on the DE2-115 board are high when not pressed
  smTop.io.dump := !io.dump
  smTop.io.valid := !io.valid
  smTop.io.data := debSw.io.swDb

  io.tx := smTop.io.tx
  io.ready := smTop.io.ready
}

/**
 * @see https://github.com/chipsalliance/firrtl/issues/2168
 */
object SmTopDe2115 extends App {
  println("Generating the SM hardware for the DE2-115 board")
  (new chisel3.stage.ChiselStage).emitVerilog(new SmTopDe2115(4, 4, 8, 64, 64, 50000000, 115200, "bootkernel.hex", ""), Array("--target-dir", "generated", "--no-dedup"))
}
