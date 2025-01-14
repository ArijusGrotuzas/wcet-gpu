package SM

import chisel3._
import chisel3.util._
import math.pow

class PredicateRegisterFile(warpCount: Int, warpSize: Int) extends Module {
  val dataWidth = warpSize
  val addrLen = log2Up(warpCount) + 2
  val io = IO(new Bundle {
    // Inputs
    val we = Input(Bool())
    val addr1R = Input(UInt(addrLen.W))
    val addr2R = Input(UInt(addrLen.W))
    val addrW = Input(UInt(addrLen.W))
    val dataW = Input(UInt(dataWidth.W))
    // Outputs
    val data1R = Output(UInt(dataWidth.W))
    val data2R = Output(UInt(dataWidth.W))
  })

  def readPort(readAddr: UInt, writeAddr: UInt, writeData: UInt): UInt = {
    val isZeroReg = readAddr(1, 0).asUInt === 0.U
    val dataOut = WireDefault(0.U(dataWidth.W))
    dataOut := nzpRegFile(readAddr)

    // Forward the write-data value if the read and write addresses are the same
    val forwardSel = (writeAddr === readAddr) && io.we
    Mux(isZeroReg, (pow(2, dataWidth).toInt - 1).U, Mux(forwardSel, writeData, dataOut))
  }

  val nzpRegFile = RegInit(VecInit(Seq.fill(warpCount * 4)(0.U(dataWidth.W))))

  // Update the correct nzp register and prevent writing to the zero register
  when(io.we && io.addrW(1, 0).asUInt =/= 0.U) {
    nzpRegFile(io.addrW) := io.dataW
  }

  // Two read ports
  io.data1R := readPort(io.addr1R, io.addrW, io.dataW)
  io.data2R := readPort(io.addr2R, io.addrW, io.dataW)
}
