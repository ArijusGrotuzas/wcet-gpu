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
             instructionFile: String = "",
             dataFile: String = ""
           ) extends Module {
  private val blockAddrLen = log2Up(blockCount)
  val io = IO(new Bundle {
    val reset = Input(Bool())
    val valid = Input(Bool())
    val data = Input(UInt((blockAddrLen + warpCount).W))

    val wbOutTest = Output(UInt((warpSize * 32).W))
    val ready = Output(Bool())
  })

  // TODO: Add data memory controller
  val debounce = Module(new Debounce(blockAddrLen + warpCount, freq))
  val instrMem = Module(new SyncMem(32, instrMemDepth, instructionFile))
  val dataMem = Module(new SyncMem(32, dataMemDepth, dataFile))
  val sm = Module(new Sm(blockCount, warpCount, warpSize))

  debounce.io.reset := io.reset
  debounce.io.valid := io.valid
  debounce.io.data := io.data

  // Set load instruction inputs to be always 0
  instrMem.io.we := false.B
  instrMem.io.dataW := 0.U

  // Data memory signals
  dataMem.io.we := sm.io.dataMem.we
  dataMem.io.dataW := sm.io.dataMem.dataW
  dataMem.io.addr := sm.io.dataMem.addr
  sm.io.dataMem.dataR := dataMem.io.dataR

  // Access to the instruction memory from SM
  instrMem.io.addr := sm.io.instrMem.addr
  sm.io.instrMem.data := instrMem.io.dataR

  // Start signal to SM connection
  sm.io.start.valid := debounce.io.validDb
  sm.io.start.data := debounce.io.dataDb

  io.ready := sm.io.start.ready
  io.wbOutTest := sm.io.wbOutTest
}

object SmTop extends App {
  println("Generating the SM hardware")
  (new chisel3.stage.ChiselStage).emitVerilog(new SmTop(4, 4, 8, 1024, 1024, 50000000, "bootkernel.hex"), Array("--target-dir", "generated"))
}
