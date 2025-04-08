package SM.Frontend.Ipdom

import chisel3.util._
import chisel3._

class WarpStacks(warpCount: Int, warpSize: Int) extends Module {
  val io = IO(new Bundle {
    val warp = Input(UInt(log2Up(warpCount).W))
    val updateTosPc = Input(Bool())
    val newTosPc = Input(UInt(32.W))
    // Outputs
    val full = Output(Bool())
    val empty = Output(Bool())
    val tosPc = Output(UInt(32.W))
    val tosRcPc = Output(UInt(32.W))
    val tosMask = Output(UInt(warpSize.W))
  })

  val stacks = Array.fill(warpCount)(Module(new IpdomStack(warpSize, 16)))

  val outFulls = VecInit(stacks.toSeq.map(_.io.full))
  val outEmpties = VecInit(stacks.toSeq.map(_.io.empty))
  val outTosPcs = VecInit(stacks.toSeq.map(_.io.tosPc))
  val outTosRcPcs = VecInit(stacks.toSeq.map(_.io.tosRcPc))
  val outTosMasks = VecInit(stacks.toSeq.map(_.io.tosMask))

  for (i <- 0 until warpCount) {
    stacks(i).io.push := false.B
    stacks(i).io.pop := false.B
    stacks(i).io.updateTosPc := (i.U === io.warp) & io.updateTosPc
    stacks(i).io.newTosPc := io.newTosPc
    stacks(i).io.pushPc := 0.U
    stacks(i).io.pushRcPc := 0.U
    stacks(i).io.pushMask := 0.U
  }

  io.full := outFulls(io.warp)
  io.empty := outEmpties(io.warp)
  io.tosPc := outTosPcs(io.warp)
  io.tosRcPc := outTosRcPcs(io.warp)
  io.tosMask := outTosMasks(io.warp)
}
