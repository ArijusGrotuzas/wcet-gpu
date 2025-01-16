import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.util._
import chisel3._

class DataMem(width: Int, depth: Int, dataFile: String = "") extends Module {
  val addrLen = log2Up(depth)
  val io = IO(new Bundle {
    val we = Input(Bool())
    val dataW = Input(UInt(width.W))
    val addr = Input(UInt(addrLen.W))

    val dataR = Output(UInt(width.W))
  })

  val dataR = WireDefault(0.U(width.W))
  val mem = SyncReadMem(depth, UInt(width.W))
  val rdwrPort = mem(io.addr)

  // Initialize memory from a file
  if (dataFile.trim().nonEmpty) {
    loadMemoryFromFileInline(mem, dataFile)
  }

  when(io.we) {
    rdwrPort := io.dataW
  }.otherwise {
    dataR := rdwrPort
  }

  io.dataR := dataR
}
