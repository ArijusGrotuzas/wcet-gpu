package SM

import chisel3._

class InstructionMemory(width: Int, depth: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    val we = Input(Bool())
    val dataIn = Input(UInt(width.W))

    val addr = Input(UInt(addrLen.W))
    val dataOut = Output(UInt(width.W))
  })

  val dataOut = WireDefault(0.U(width.W))
  val mem = SyncReadMem(depth, UInt(width.W))
  val rdwrPort = mem(io.addr)
  dataOut := 0.U

  when(io.we) {
    rdwrPort := io.dataIn
  }.otherwise {
    dataOut := rdwrPort
  }

  io.dataOut := dataOut
}
