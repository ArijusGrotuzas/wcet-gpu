package SM.Backend

import SM.Backend.Alu.AluPipeline
import SM.Backend.Mem.MemPipeline
import chisel3._

class Backend(warpCount: Int, warpSize: Int, warpAddrLen: Int) extends Module {
  val io = IO(new Bundle {
    val front = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val imm = Input(UInt(32.W))
    }

    val funcUnits = new Bundle {
      val memStall = Output(Bool())
      val aluStall = Output(Bool())
    }

    val wb = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val setInactive = Output(Bool())
      val setNotPending = Output(Bool())
    }

    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  val of = Module(new OperandFetch(warpCount, warpSize, warpAddrLen))
  val alu = Module(new AluPipeline(warpSize, warpAddrLen))
  val mem = Module(new MemPipeline(warpSize, warpAddrLen))
  val wb = Module(new WriteBack(warpSize, warpAddrLen))

  of.io.iss <> io.front

  alu.io.of.warp := RegNext(of.io.aluOf.warp, 0.U)
  alu.io.of.opcode := RegNext(of.io.aluOf.opcode, 0.U)
  alu.io.of.dest := RegNext(of.io.aluOf.dest, 0.U)
  alu.io.of.rs1 := RegNext(of.io.aluOf.rs1, 0.U)
  alu.io.of.rs2 := RegNext(of.io.aluOf.rs2, 0.U)
  alu.io.of.rs3 := RegNext(of.io.aluOf.rs3, 0.U)
  alu.io.of.imm := RegNext(of.io.aluOf.imm, 0.U)

  mem.io.of.warp := RegNext(of.io.memOf.warp, 0.U)
  mem.io.of.opcode := RegNext(of.io.memOf.opcode, 0.U)
  mem.io.of.dest := RegNext(of.io.memOf.dest, 0.U)
  mem.io.of.rs1 := RegNext(of.io.memOf.rs1, 0.U)
  mem.io.of.rs2 := RegNext(of.io.memOf.rs2, 0.U)
  mem.io.of.imm := RegNext(of.io.memOf.imm, 0.U)

  wb.io.alu.warp := RegNext(alu.io.alu.warp, 0.U)
  wb.io.alu.done := RegNext(alu.io.alu.done, false.B)
  wb.io.alu.valid := RegNext(alu.io.alu.valid, false.B)
  wb.io.alu.dest := RegNext(alu.io.alu.dest, 0.U)
  wb.io.alu.out := RegNext(alu.io.alu.out, 0.U)

  wb.io.mem.warp := RegNext(mem.io.mem.warp, 0.U)
  wb.io.mem.valid := RegNext(mem.io.mem.valid, false.B)
  wb.io.mem.pending := RegNext(mem.io.mem.pending, false.B)
  wb.io.mem.dest := RegNext(mem.io.mem.dest, 0.U)
  wb.io.mem.out := RegNext(mem.io.mem.out, 0.U)

  of.io.wb <> wb.io.wbOf
  io.wb <> wb.io.wbIf

  io.funcUnits.memStall := false.B
  io.funcUnits.aluStall := false.B
  io.wbOutTest := wb.io.outTest
}
