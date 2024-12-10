package SM.Frontend

import chisel3._

class DualPortedRam(depth: Int, width: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    // Inputs
    val we = Input(Bool())
    val readAddr = Input(UInt(addrLen.W))
    val writeAddr = Input(UInt(addrLen.W))
    val writeData = Input(UInt(width.W))
    // Outputs
    val readData = Output(UInt(width.W))
  })

  val readData = WireDefault(0.U(width.W))
  val mem = SyncReadMem(depth, UInt(width.W))

  // Create one write port and one read port
  when(io.we) {
    mem.write(io.writeAddr, io.writeData)
  }

  readData := mem.read(io.readAddr, true.B)

  io.readData := readData
}