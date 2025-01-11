package SM.Backend

import chisel3._

class PredicateRegister(warpCount: Int, warpSize: Int) extends Module {
  val dataWidth = 3 * warpSize
  val io = IO(new Bundle {
    val we = Input(Bool())
    val addrR = Input(UInt(warpCount.W))
    val addrW = Input(UInt(warpCount.W))
    val dataW = Input(UInt(dataWidth.W))
    val dataR = Output(UInt(dataWidth.W))
  })

  val nzpRegFile = RegInit(VecInit(Seq.fill(warpCount)(0.U((3 * warpSize).W))))
  val dataOut = WireDefault(0.U((3 * warpSize).W))

  // Update the correct nzp register
  when(io.we) {
    nzpRegFile(io.addrW) := io.dataW
  }

  // Forward the write-data value if the read and write addresses are the same
  when(io.addrW === io.addrR && io.we) {
    dataOut := io.dataW
  }.otherwise {
    dataOut := nzpRegFile(io.addrR)
  }

  io.dataR := dataOut
}
