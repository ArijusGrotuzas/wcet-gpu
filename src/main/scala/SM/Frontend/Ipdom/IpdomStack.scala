package SM.Frontend.Ipdom

import chisel3._
import chisel3.util._

import scala.math.pow

class IpdomStack(warpSize: Int, stackDepth: Int) extends Module {
  val stackAddrLen = log2Up(stackDepth)
  val io = IO(new Bundle{
    // Inputs
    val prepare = Input(Bool())
    val prepareAddr = Input(UInt(32.W))
    val split = Input(Bool())
    val splitAddr = Input(UInt(32.W))
    val splitMask = Input(UInt(warpSize.W))
    val join = Input(Bool())
    val updateTosPc = Input(Bool())
    val newTosPc = Input(UInt(32.W))
    // Outputs
    val full = Output(Bool())
    val empty = Output(Bool())
    val tosPc = Output(UInt(32.W))
    val tosMask = Output(UInt(warpSize.W))
  })

  val pcStack = RegInit(VecInit(Seq.fill(stackDepth)(0.U(32.W))))
  val maskStack = RegInit(VecInit(Seq.fill(stackDepth)((pow(2, warpSize).toLong - 1).U(warpSize.W))))
  val tos = RegInit(0.U(stackAddrLen.W))

  val full = tos.andR
  val empty = tos.orR

  when(io.prepare && !io.split && !io.join && !io.full) {
    val nextTos = tos + 1.U
    pcStack(tos) := io.prepareAddr
    pcStack(nextTos) := pcStack(tos) + 1.U
    maskStack(nextTos) := maskStack(tos)
    tos := nextTos
  }.elsewhen(!io.prepare && io.split && !io.join && !io.full) {
    val nextTos = tos + 1.U
    pcStack(tos) := io.splitAddr
    maskStack(tos) := ~io.splitMask
    pcStack(nextTos) := pcStack(tos) + 1.U
    maskStack(nextTos) := io.splitMask
    tos := nextTos
  }.elsewhen(!io.prepare && !io.split && io.join && !io.empty) {
    val nextTos = tos - 1.U
    pcStack(tos) := 0.U
    maskStack(tos) := 0.U
    tos := nextTos
  }.elsewhen(io.updateTosPc) {
    pcStack(tos) := io.newTosPc
  }

  io.full := full
  io.empty := empty
  io.tosPc := pcStack(tos)
  io.tosMask := maskStack(tos)
}
