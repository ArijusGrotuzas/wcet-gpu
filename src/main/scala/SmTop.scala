import SM.Sm
import chisel3._
import chisel3.util._

class memoryDump(memAddrWidth: Int) extends Bundle {
  val dumpAddr = Input(UInt(memAddrWidth.W))
  val dumpData = Output(UInt(32.W))
}

class SmTop(
             blockCount: Int,
             warpCount: Int,
             warpSize: Int,
             instrMemDepth: Int,
             dataMemDepth: Int,
             instructionFile: String = "",
             dataFile: String = ""
           ) extends Module {
  val io = IO(new Bundle {
    val memDump = new memoryDump(log2Up(dataMemDepth))
    val valid = Input(Bool())
    val data = Input(UInt((log2Up(blockCount) + warpCount).W))
    val ready = Output(Bool())
    val done = Output(Bool())
  })

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
  // When the SM is not processing, allow dumping the data memory contents
  dataMem.io.addr := Mux(sm.io.start.ready, io.memDump.dumpAddr, sm.io.dataMem.addr)

  // Start signal to SM connection
  sm.io.start.valid := io.valid
  sm.io.start.data := io.data
  sm.io.dataMem.dataR := dataMem.io.dataR
  sm.io.instrMem.data := instrMem.io.dataR

  // Interface for dumping data memory contents
  io.memDump.dumpData := dataMem.io.dataR

  io.ready := sm.io.start.ready
  io.done := sm.io.done
}
