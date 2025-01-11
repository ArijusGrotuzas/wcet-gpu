package SM.Backend.Vrf

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

  val mem = SyncReadMem(depth, UInt(width.W))
  val readData = WireDefault(0.U(width.W))
  val writeDataReg = RegNext(io.writeData)
  val forwardSelReg = RegNext(io.writeAddr === io.readAddr && io.we)

  readData := mem.read(io.readAddr)

  // Create one write port and one read port
  when(io.we) {
    mem.write(io.writeAddr, io.writeData)
  }

  io.readData := Mux(forwardSelReg, writeDataReg, readData)
}