package SM

import chisel3._
import chisel3.util._

class PredicateRegister(warpCount: Int, warpSize: Int) extends Module {
  val dataWidth = 3 * warpSize
  val addrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val we = Input(Bool())
    val addrR = Input(UInt(addrLen.W))
    val addrW = Input(UInt(addrLen.W))
    val dataW = Input(UInt(dataWidth.W))
    val dataR = Output(UInt(dataWidth.W))
  })

  val nzpRegFile = RegInit(VecInit(Seq.fill(warpCount)(0.U((3 * warpSize).W))))
  val dataOut = WireDefault(0.U((3 * warpSize).W))
  // Forward the write-data value if the read and write addresses are the same
  val forwardSel = io.addrW === io.addrR && io.we

  dataOut := nzpRegFile(io.addrR)

  // Update the correct nzp register
  when(io.we) {
    nzpRegFile(io.addrW) := io.dataW
  }

  io.dataR := Mux(forwardSel, io.dataW, dataOut)
}
