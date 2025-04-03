package SM.Frontend.Ipdom

import chisel3._
import chisel3.util._

import scala.math.pow

class IpdomStack(warpSize: Int, stackDepth: Int) extends Module {
  val stackAddrLen = log2Up(stackDepth)
  val io = IO(new Bundle{
    // Inputs
    val push = Input(Bool())
    val pop = Input(Bool())
    val pushPc = Input(UInt(32.W))
    val pushRcPc = Input(UInt(32.W))
    val updateTosPc = Input(Bool())
    val newTosPc = Input(UInt(32.W))
    val pushMask = Input(UInt(warpSize.W))
    // Outputs
    val full = Output(Bool())
    val empty = Output(Bool())
    val tosPc = Output(UInt(32.W))
    val tosRcPc = Output(UInt(32.W))
    val tosMask = Output(UInt(warpSize.W))
  })

  val pcStack = RegInit(VecInit(Seq.fill(stackDepth)(0.U(32.W))))
  val rcPcStack = RegInit(VecInit(Seq.fill(stackDepth)(0.U(32.W))))
  val maskStack = RegInit(VecInit(Seq.fill(stackDepth)((pow(2, warpSize).toLong - 1).U(warpSize.W))))
  val tos = RegInit(0.U(stackAddrLen.W))

  val full = tos.andR
  val empty = tos.orR

  when(io.updateTosPc) {
    pcStack(tos) := io.newTosPc
  }

  when(io.push && !io.pop && !io.updateTosPc && !full) {
    val nextTos = tos + 1.U
    pcStack(nextTos) := io.pushPc
    rcPcStack(nextTos) := io.pushRcPc
    maskStack(nextTos) := io.pushMask
    tos := nextTos
  }

  when(!io.push && io.pop && !io.updateTosPc && !empty) {
    val nextTos = tos - 1.U
    pcStack(tos) := 0.U
    rcPcStack(tos) := 0.U
    maskStack(tos) := 0.U
    tos := nextTos
  }

  io.full := full
  io.empty := empty
  io.tosPc := pcStack(tos)
  io.tosRcPc := rcPcStack(tos)
  io.tosMask := maskStack(tos)
}
