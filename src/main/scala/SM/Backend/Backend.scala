package SM.Backend

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
      val imm = Input(UInt(22.W))
    }

    val aluStall = Output(Bool())
    val memStall = Output(Bool())

    val wb = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val setInactive = Output(Bool())
      val setNotPending = Output(Bool())
    }

    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  val of = Module(new OperandFetch(warpCount, warpSize, warpAddrLen))
  val alu = Module(new AluPipeline(warpSize))
  val mem = Module(new MemPipeline(warpSize, warpAddrLen))
  val wb = Module(new WriteBack(warpSize, warpAddrLen))

  of.io.iss <> io.front

  alu.io.of.warp := RegNext(of.io.aluOf.warp)
  alu.io.of.opcode := RegNext(of.io.aluOf.opcode)
  alu.io.of.dest := RegNext(of.io.aluOf.dest)
  alu.io.of.rs1 := RegNext(of.io.aluOf.rs1)
  alu.io.of.rs2 := RegNext(of.io.aluOf.rs2)
  alu.io.of.rs3 := RegNext(of.io.aluOf.rs3)
  alu.io.of.imm := RegNext(of.io.aluOf.imm)

  mem.io.of.warp := RegNext(of.io.memOf.warp)
  mem.io.of.opcode := RegNext(of.io.memOf.opcode)
  mem.io.of.dest := RegNext(of.io.memOf.dest)
  mem.io.of.rs1 := RegNext(of.io.memOf.rs1)
  mem.io.of.rs2 := RegNext(of.io.memOf.rs2)
  mem.io.of.imm := RegNext(of.io.memOf.imm)

  wb.io.alu.warp := RegNext(alu.io.alu.warp)
  wb.io.alu.done := RegNext(alu.io.alu.done)
  wb.io.alu.valid := RegNext(alu.io.alu.valid)
  wb.io.alu.dest := RegNext(alu.io.alu.dest)
  wb.io.alu.out := RegNext(alu.io.alu.out)

  wb.io.mem.warp := RegNext(mem.io.mem.warp)
  wb.io.mem.valid := RegNext(mem.io.mem.valid)
  wb.io.mem.pending := RegNext(mem.io.mem.pending)
  wb.io.mem.dest := RegNext(mem.io.mem.dest)
  wb.io.mem.out := RegNext(mem.io.mem.out)

  of.io.wb <> wb.io.wbOf
  io.wb <> wb.io.wbIf

  io.memStall := false.B
  io.aluStall := false.B
  io.wbOutTest := wb.io.outTest
}
