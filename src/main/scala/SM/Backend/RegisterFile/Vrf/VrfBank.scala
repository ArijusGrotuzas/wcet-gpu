package SM.Backend.RegisterFile.Vrf

import chisel3._
import chisel3.util._

class VrfBank(depth: Int, warpSize: Int) extends Module {
  val width = 32 * warpSize
  val addrLen = log2Up(depth)
  val io = IO(new Bundle {
    // Inputs
    val we = Input(Bool())
    val readAddr = Input(UInt(addrLen.W))
    val writeAddr = Input(UInt(addrLen.W))
    val writeData = Input(Vec(warpSize, UInt(32.W)))
    val writeMask = Input(Vec(warpSize, Bool()))
    // Outputs
    val readData = Output(Vec(warpSize, UInt(32.W)))
  })

  val mem = SyncReadMem(depth, Vec(warpSize, UInt(32.W)))
  val readData = VecInit(Seq.fill(warpSize)(0.U(32.W)))
  val writeDataReg = RegNext(io.writeData)
  val forwardSelReg = RegNext(io.writeAddr === io.readAddr && io.we)

  readData := mem.read(io.readAddr)

  // Create one write port and one read port
  when(io.we) {
    mem.write(io.writeAddr, io.writeData, io.writeMask)
  }

  io.readData := Mux(forwardSelReg, writeDataReg, readData)
}