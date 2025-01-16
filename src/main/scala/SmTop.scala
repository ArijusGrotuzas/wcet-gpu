import SM.Sm
import chisel3._
import chisel3.util._

class SmTop(
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
    val led = Output(Bool())
    val ready = Output(Bool())
//    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  def blinkingLed(freq: Int): UInt = {
    val tickReg = RegInit(0.U(32.W))
    val ledReg = RegInit(0.U(1.W))
    val tick = tickReg === (freq / 2 - 1).U

    tickReg := tickReg + 1.U
    when(tick) {
      tickReg := 0.U
      ledReg := ~ledReg
    }

    ledReg
  }

  // Need two separate data modules, since it seems that chisel does not allow initializing
  // one instance of the same module with data, and to initialize another instance of the same module with no data
  val debounce = Module(new DebounceSw(blockAddrLen + warpCount, freq))
  val serialPort = Module(new SerialPort(freq, baud, dataMemDepth))
  val instrMem = Module(new InstrMem(32, instrMemDepth, instructionFile))
  val dataMem = Module(new DataMem(32, dataMemDepth, dataFile))
  val sm = Module(new Sm(blockCount, warpCount, warpSize))

  // Debounce switch signals
  debounce.io.sw := io.sw

  // Instruction memory signals
  instrMem.io.we := false.B
  instrMem.io.dataW := 0.U
  instrMem.io.addr := sm.io.instrMem.addr

  // Data memory signals
  dataMem.io.we := sm.io.dataMem.we
  dataMem.io.dataW := sm.io.dataMem.dataW
  dataMem.io.addr := sm.io.dataMem.addr
  dataMem.io.addrR2 := serialPort.io.addrR

  // Start signal to SM connection
  sm.io.start.valid := io.valid
  sm.io.start.data := debounce.io.swDb
  sm.io.dataMem.dataR := dataMem.io.dataR
  sm.io.instrMem.data := instrMem.io.dataR

  // Serial port for dumping memory content to the UART
  serialPort.io.dump := io.dump
  serialPort.io.dataR := dataMem.io.dataR2

  io.tx := serialPort.io.tx
  io.led := blinkingLed(freq)
  io.ready := sm.io.start.ready
//  io.wbOutTest := sm.io.wbOutTest
}

object SmTop extends App {
  println("Generating the SM hardware")
  (new chisel3.stage.ChiselStage).emitVerilog(new SmTop(4, 4, 8, 64, 64, 50000000, 115200, "bootkernel.hex", ""), Array("--target-dir", "generated"))
}
