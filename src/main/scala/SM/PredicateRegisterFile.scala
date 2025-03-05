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
    val addrW = Input(UInt(addrLen.W))
    val dataW = Input(UInt(dataWidth.W))
    val addr1R = Input(UInt(addrLen.W))
    val addr2R = Input(UInt(addrLen.W))
    // Outputs
    val data1R = Output(UInt(dataWidth.W))
    val data2R = Output(UInt(dataWidth.W))
  })

  val regFileContents = (0 until warpCount * 4).map(i => if (i % 4 == 0) (pow(2, warpSize).toLong - 1).U else 0.U(dataWidth.W))
  val predRegFile = RegInit(VecInit(regFileContents))

  // Update the correct predicate register and prevent writing to the zero register
  when(io.we && io.addrW(1, 0).asUInt =/= 0.U) {
    predRegFile(io.addrW) := io.dataW
  }

  // Two read ports
  io.data1R := predRegFile(io.addr1R)
  io.data2R := Mux((io.addr2R === io.addrW) && io.we, io.dataW, predRegFile(io.addr2R))
}
