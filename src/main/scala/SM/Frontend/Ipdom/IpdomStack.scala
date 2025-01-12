package SM.Frontend.Ipdom

import chisel3._
import chisel3.util._

class IpdomStack(warpSize: Int, stackDepth: Int) extends Module {
  val stackAddrLen = log2Up(stackDepth)
  val io = IO(new Bundle{
    val push = Input(Bool())
    val pop = Input(Bool())
    val pushPc = Input(UInt(32.W))
    val pushMask = Input(UInt(warpSize.W))
    val full = Output(Bool())
    val empty = Output(Bool())
    val tosPc = Output(UInt(32.W))
    val tosMask = Output(UInt(warpSize.W))
  })

  val pcStack = RegInit(VecInit(Seq.fill(stackDepth)(0.U(32.W))))
  val maskStack = RegInit(VecInit(Seq.fill(stackDepth)(0.U(warpSize.W))))
  val nextTos = WireDefault(0.U(stackAddrLen.W))
  val tos = RegInit(0.U(stackAddrLen.W))

  when(io.push && !io.pop && !tos.andR) {
    nextTos := tos + 1.U
    pcStack(nextTos) := io.pushPc
    maskStack(nextTos) := io.pushMask
    tos := nextTos
  }

  when(!io.push && io.pop && !tos.orR) {
    nextTos := tos - 1.U
    pcStack(tos) := 0.U
    maskStack(tos) := 0.U
    tos := nextTos
  }

  io.full := tos.andR
  io.empty := tos.orR
  io.tosPc := pcStack(tos)
  io.tosMask := maskStack(tos)
}
