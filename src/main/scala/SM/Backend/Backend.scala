package SM.Backend

import chisel3._
import chisel3.util._

class Backend(warpAddrLen: Int) extends Module {
  val io = IO(new Bundle{
    val front = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val imm = Output(UInt(22.W))
    }
  })

  val of = Module(new OperandFetch(4, 8, 2))
//  val alu = Module(new AluPipeline(8))
//  val mem = Module(new MemPipeline(8, 2))
//  val wb = Module(new WriteBack(8, 2))
//
//  of.io.iss <> io.front
//
//  alu.io.of <> of.io.aluOf
//  mem.io.of <> of.io.memOf
//
//  wb.io.alu <> alu.io.alu
//  wb.io.mem <> mem.io.mem
//
//  of.io.wb <> wb.io.wbOf
}
