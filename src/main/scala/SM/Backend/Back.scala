package SM.Backend

import SM.Backend.Alu.AluPipeline
import SM.Backend.Mem.MemPipeline
import chisel3._
import chisel3.util._

class Back(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  val blockAddrLen = log2Up(blockCount)
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val front = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val srs = Input(UInt(3.W))
      val imm = Input(SInt(32.W))
    }

    val aluInitCtrl = new Bundle {
      val setBlockIdx = Input(Bool())
      val blockIdx = Input(UInt(blockAddrLen.W))
    }

    val funcUnits = new Bundle {
      val memStall = Output(Bool())
    }

    val wb = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val setInactive = Output(Bool())
      val setNotPending = Output(Bool())
    }

    val nzpUpdate = new Bundle {
      val nzp = Output(UInt(3.W))
      val en = Output(Bool())
      val warp = Output(UInt(log2Up(warpCount).W))
    }

    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  val of = Module(new OperandFetch(warpCount, warpSize))
  val alu = Module(new AluPipeline(blockCount, warpCount, warpSize))
  val mem = Module(new MemPipeline(warpCount, warpSize))
  val wb = Module(new WriteBack(warpCount, warpSize))

  of.io.iss <> io.front

  // Pipeline register between the operand fetch and alu stages
  alu.io.of.warp := RegNext(of.io.aluOf.warp, 0.U)
  alu.io.of.opcode := RegNext(of.io.aluOf.opcode, 0.U)
  alu.io.of.dest := RegNext(of.io.aluOf.dest, 0.U)
  alu.io.of.rs1 := RegNext(of.io.aluOf.rs1, 0.U)
  alu.io.of.rs2 := RegNext(of.io.aluOf.rs2, 0.U)
  alu.io.of.rs3 := RegNext(of.io.aluOf.rs3, 0.U)
  alu.io.of.srs := RegNext(of.io.aluOf.srs, 0.U)
  alu.io.of.imm := RegNext(of.io.aluOf.imm, 0.S)
  alu.io.aluInitCtrl <> io.aluInitCtrl

  // Pipeline register between the operand fetch and memory stages
  mem.io.of.warp := RegNext(of.io.memOf.warp, 0.U)
  mem.io.of.opcode := RegNext(of.io.memOf.opcode, 0.U)
  mem.io.of.dest := RegNext(of.io.memOf.dest, 0.U)
  mem.io.of.rs1 := RegNext(of.io.memOf.rs1, 0.U)
  mem.io.of.rs2 := RegNext(of.io.memOf.rs2, 0.U)
  mem.io.of.imm := RegNext(of.io.memOf.imm, 0.S)

  // Pipeline register between the alu and write-back stages
  wb.io.alu.warp := RegNext(alu.io.alu.warp, 0.U)
  wb.io.alu.done := RegNext(alu.io.alu.done, false.B)
  wb.io.alu.we := RegNext(alu.io.alu.we, false.B)
  wb.io.alu.dest := RegNext(alu.io.alu.dest, 0.U)
  wb.io.alu.out := RegNext(alu.io.alu.out, 0.U)

  // Pipeline register between the memory and write-back stages
  wb.io.mem.warp := RegNext(mem.io.mem.warp, 0.U)
  wb.io.mem.we := RegNext(mem.io.mem.we, false.B)
  wb.io.mem.pending := RegNext(mem.io.mem.pending, false.B)
  wb.io.mem.dest := RegNext(mem.io.mem.dest, 0.U)
  wb.io.mem.out := RegNext(mem.io.mem.out, 0.U)

  of.io.wb <> wb.io.wbOf
  io.wb <> wb.io.wbIf

  io.funcUnits.memStall := mem.io.stall
  io.wbOutTest := wb.io.outTest
  io.nzpUpdate := alu.io.nzpUpdate
}
