package SM.Backend.Mem

import chisel3._
import chisel3.util._

class MemPipeline(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val of = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt((32 * warpSize).W))
      val rs2 = Input(UInt((32 * warpSize).W))
      val imm = Input(SInt(32.W))
    }

    val mem = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val valid = Output(Bool())
      val pending = Output(Bool())
      val dest = Output(UInt(5.W))
      val out = Output(UInt((32 * warpSize).W))
    }

    val stall = Output(Bool())
  })

  // TODO: Create a data cache/memory interface
  // TODO: Instantiate an array of LSU modules
  // TODO: Create a state machine that stalls until all LSUs are done
  // TODO: Add pending input signals

  io.mem.warp := io.of.warp
  io.mem.valid := false.B
  io.mem.pending := false.B
  io.mem.dest := io.of.dest
  io.mem.out := 0.U
  io.stall := false.B // TODO: Implement the stall signal
}