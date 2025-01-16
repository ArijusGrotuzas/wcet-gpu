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
    val dump = Input(Bool())
    val valid = Input(Bool())
    val data = Input(UInt((blockAddrLen + warpCount).W))
    // Outputs
    val tx = Output(Bool())
    val ready = Output(Bool())
    //    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  // Need two separate data modules, since it seems that chisel does not allow initializing
  // one instance of the same module with data, and to initialize another instance of the same module with no data
  val serialPort = Module(new SerialPort(freq, baud, dataMemDepth))
  val instrMem = Module(new SyncMem(32, instrMemDepth, instructionFile))
  val dataMem = Module(new SyncMem(32, dataMemDepth, dataFile))
  val sm = Module(new Sm(blockCount, warpCount, warpSize))

  // Instruction memory signals
  instrMem.io.we := false.B
  instrMem.io.dataW := 0.U
  instrMem.io.addr := sm.io.instrMem.addr

  // Data memory signals
  dataMem.io.we := sm.io.dataMem.we
  dataMem.io.dataW := sm.io.dataMem.dataW
  // When the SM is not ready, allow the serial port to read from the data memory
  dataMem.io.addr := Mux(sm.io.start.ready, serialPort.io.addrR, sm.io.dataMem.addr)

  // Start signal to SM connection
  sm.io.start.valid := io.valid
  sm.io.start.data := io.data
  sm.io.dataMem.dataR := dataMem.io.dataR
  sm.io.instrMem.data := instrMem.io.dataR

  // Serial port for dumping memory content to the UART
  serialPort.io.dump := io.dump
  serialPort.io.dataR := dataMem.io.dataR

  io.tx := serialPort.io.tx
  io.ready := sm.io.start.ready
  //  io.wbOutTest := sm.io.wbOutTest
}
