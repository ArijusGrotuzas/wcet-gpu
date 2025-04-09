package SM.Frontend.Ipdom

import chisel3.util._
import chisel3._

class WarpStacks(warpCount: Int, warpSize: Int, stacksDepth: Int) extends Module {
  val io = IO(new Bundle {
    val warp = Input(UInt(log2Up(warpCount).W))
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

  val stacks = Array.fill(warpCount)(Module(new IpdomStack(warpSize, stacksDepth)))

  val outFulls = VecInit(stacks.toSeq.map(_.io.full))
  val outEmpties = VecInit(stacks.toSeq.map(_.io.empty))
  val outTosPcs = VecInit(stacks.toSeq.map(_.io.tosPc))
  val outTosMasks = VecInit(stacks.toSeq.map(_.io.tosMask))

  for (i <- 0 until warpCount) {
    stacks(i).io.prepare := io.prepare
    stacks(i).io.prepareAddr := io.prepareAddr
    stacks(i).io.split := io.split
    stacks(i).io.splitAddr := io.splitAddr
    stacks(i).io.splitMask := io.splitMask
    stacks(i).io.join := io.join
    stacks(i).io.updateTosPc := (i.U === io.warp) & io.updateTosPc
    stacks(i).io.newTosPc := io.newTosPc
  }

  io.full := outFulls(io.warp)
  io.empty := outEmpties(io.warp)
  io.tosPc := outTosPcs(io.warp)
  io.tosMask := outTosMasks(io.warp)
}
