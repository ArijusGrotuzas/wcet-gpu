import chisel3._
import chisel3.util.experimental.loadMemoryFromFileInline

class InstructionMemory(width: Int, depth: Int, addrLen: Int, instructionFile: String = "") extends Module {
  val io = IO(new Bundle {
    val we = Input(Bool())
    val dataIn = Input(UInt(width.W))
    val wAddr = Input(UInt(addrLen.W))

    val addr = Input(UInt(addrLen.W))
    val dataOut = Output(UInt(width.W))
  })

  val dataOut = WireDefault(0.U(width.W))
  val mem = SyncReadMem(depth, UInt(width.W))
  val rdwrPort = mem(Mux(io.we, io.wAddr, io.addr))

  dataOut := 0.U

  // Initialize memory from a file
  if (instructionFile.trim().nonEmpty) {
    loadMemoryFromFileInline(mem, instructionFile)
  }

  when(io.we) {
    rdwrPort := io.dataIn
  }.otherwise {
    dataOut := rdwrPort
  }

  io.dataOut := dataOut
}
