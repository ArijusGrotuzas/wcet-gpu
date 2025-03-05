package SM.Backend.RegisterFile

import chisel3._
import chisel3.util._

// TODO: Add this to a larger register file module
class SpecialRegisterFile(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  val blockAddrLen = log2Up(blockCount)
  val io = IO(new Bundle {
    val readAddr = Input(UInt(2.W))
    val setBlockIdx = Input(Bool())
    val blockIdx = Input(UInt(blockAddrLen.W))
    val readData = Output(UInt((warpSize * 32).W))
  })

  // TODO: Add warp ID as an output in a higher level module
  // TODO: Think if zero register can be just zero and if other indexes can be filled with other relevant values
  val srfContents = RegInit(VecInit(
    VecInit((0 until warpSize).map(i => i.U(32.W))),        // 0: thread ID
    VecInit(Seq.fill(warpSize)(0.U(32.W))),                       // 1: block ID
    VecInit(Seq.fill(warpSize)(warpSize.U(32.W))),                // 2: warp width
    VecInit(Seq.fill(warpSize)((warpCount * warpSize).U(32.W))),  // 3: block width
  ))

  val newBlockID = VecInit(Seq.fill(warpSize)(0.U(32.W)))
  for (i <- 0 until warpSize) {
    newBlockID(i) := io.blockIdx
  }

  when(io.setBlockIdx) {
    srfContents(1) := newBlockID
  }

  val readData = WireDefault(0.U((warpSize * 32).W))
  readData := srfContents(io.readAddr)

  io.readData := readData
}
