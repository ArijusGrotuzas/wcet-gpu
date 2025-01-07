import chisel3._
import chisel3.util.experimental.loadMemoryFromFileInline

class synchronousMemory(width: Int, depth: Int, addrLen: Int, dataFile: String = "") extends Module {
  val io = IO(new Bundle {
    val we = Input(Bool())
    val dataW = Input(UInt(width.W))
    val addrW = Input(UInt(addrLen.W))
    val addr = Input(UInt(addrLen.W))

    val dataR = Output(UInt(width.W))
  })

  val dataR = WireDefault(0.U(width.W))
  val mem = SyncReadMem(depth, UInt(width.W))
  val rdwrPort = mem(Mux(io.we, io.addrW, io.addr))

  dataR := 0.U

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
