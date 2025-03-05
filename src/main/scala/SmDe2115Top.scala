import chisel3._
import chisel3.util._

class SmDe2115Top(
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

  val serialPort = Module(new SerialPort(freq, baud, dataMemDepth))
  val debSw = Module(new DebounceSw(blockAddrLen + warpCount, freq))
  val smTop = Module(new SmTop(blockCount, warpCount, warpSize, instrMemDepth, dataMemDepth, instructionFile, dataFile))

  debSw.io.sw := io.sw

  smTop.io.valid := !io.valid
  smTop.io.data := debSw.io.swDb
  smTop.io.memDump.dumpAddr := serialPort.io.addrR

  // Must invert the signals since the debounced buttons on the DE2-115 board are high when not pressed
  serialPort.io.dump := !io.dump
  serialPort.io.dataR := smTop.io.memDump.dumpData

  // Cycle counter
  val cycleCount = cycleCounter(smTop.io.ready, smTop.io.done)

  io.tx := serialPort.io.tx
  io.ready := smTop.io.ready
  io.cycles := cycleCount
}

/**
 * @see https://github.com/chipsalliance/firrtl/issues/2168
 */
object SmDe2115Top extends App {
  println("Generating the SM hardware for the DE2-115 board")
  (new chisel3.stage.ChiselStage).emitVerilog(new SmDe2115Top(4, 4, 8, 64, 1024, 50000000, 115200, "bootkernel.hex", "bootdata.hex"), Array("--target-dir", "generated", "--no-dedup"))
}
