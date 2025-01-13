package SM.Backend

import chisel3._
import chisel3.util._

class Back(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  val blockAddrLen = log2Up(blockCount)
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val front = new Bundle {
      val threadMask = Input(UInt(warpSize.W))
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val srs = Input(UInt(3.W))
      val imm = Input(SInt(32.W))
      val pred = Input(UInt(5.W))
    }

    val aluInitCtrl = new Bundle {
      val setBlockIdx = Input(Bool())
      val blockIdx = Input(UInt(blockAddrLen.W))
    }

    val lsu = new Bundle {
      // Read signals
      val readAck = Input(UInt(warpSize.W))
      val readReq = Output(UInt(warpSize.W))
      val readData = Input(UInt((32 * warpSize).W))
      // Write Signals
      val writeAck = Input(UInt(warpSize.W))
      val writeReq = Output(UInt(warpSize.W))
      val writeData = Output(UInt((32 * warpSize).W))
      // Shared address signal
      val addr = Output(UInt((32 * warpSize).W))
    }

    val wbIfCtrl = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val setInactive = Output(Bool())
    }

    val memIfCtrl = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val setNotPending = Output(Bool())
    }

    val nzpUpdateCtrl = new Bundle {
      val en = Output(Bool())
      val nzp = Output(UInt((3 * warpSize).W))
      val warp = Output(UInt(warpAddrLen.W))
    }

    val memStall = Output(Bool())
    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  val of = Module(new OperandFetch(warpCount, warpSize))
  val alu = Module(new AluPipeline(blockCount, warpCount, warpSize))
  val mem = Module(new MemPipeline(warpCount, warpSize))
  val wb = Module(new WriteBack(warpCount, warpSize))

  val threadMaskMemOfReg = RegInit(0.U(warpSize.W))
  val validMemOfReg = RegInit(false.B)
  val warpMemOfReg = RegInit(0.U(warpAddrLen.W))
  val opcodeMemOfReg = RegInit(0.U(5.W))
  val destMemOfReg = RegInit(0.U(5.W))
  val rs1MemOfReg = RegInit(0.U((32 * warpSize).W))
  val rs2MemOfReg = RegInit(0.U((32 * warpSize).W))

  // Inputs to the operand fetch stage
  of.io.iss <> io.front

  // Gate the pipeline register for memory unit
  threadMaskMemOfReg := Mux(mem.io.memStall, threadMaskMemOfReg, of.io.memOf.threadMask)
  validMemOfReg := Mux(mem.io.memStall, validMemOfReg, of.io.memOf.valid)
  warpMemOfReg := Mux(mem.io.memStall, warpMemOfReg, of.io.memOf.warp)
  opcodeMemOfReg := Mux(mem.io.memStall, opcodeMemOfReg, of.io.memOf.opcode)
  destMemOfReg := Mux(mem.io.memStall, destMemOfReg, of.io.memOf.dest)
  rs1MemOfReg := Mux(mem.io.memStall, rs1MemOfReg, of.io.memOf.rs1)
  rs2MemOfReg := Mux(mem.io.memStall, rs2MemOfReg, of.io.memOf.rs2)

  of.io.iss <> io.front

  // Pipeline registers between the operand fetch and alu stages
  alu.io.of.threadMask := RegNext(of.io.aluOf.threadMask, 0.U)
  alu.io.of.warp := RegNext(of.io.aluOf.warp, 0.U)
  alu.io.of.opcode := RegNext(of.io.aluOf.opcode, 0.U)
  alu.io.of.dest := RegNext(of.io.aluOf.dest, 0.U)
  alu.io.of.rs1 := RegNext(of.io.aluOf.rs1, 0.U)
  alu.io.of.rs2 := RegNext(of.io.aluOf.rs2, 0.U)
  alu.io.of.rs3 := RegNext(of.io.aluOf.rs3, 0.U)
  alu.io.of.srs := RegNext(of.io.aluOf.srs, 0.U)
  alu.io.of.imm := RegNext(of.io.aluOf.imm, 0.S)
  alu.io.aluInitCtrl <> io.aluInitCtrl

  // Pipeline registers between the operand fetch and memory stages
  mem.io.of.threadMask := threadMaskMemOfReg
  mem.io.of.valid := validMemOfReg
  mem.io.of.warp := warpMemOfReg
  mem.io.of.opcode := opcodeMemOfReg
  mem.io.of.dest := destMemOfReg
  mem.io.of.rs1 := rs1MemOfReg
  mem.io.of.rs2 := rs2MemOfReg

  // Pipeline registers between the alu and write-back stages
  wb.io.alu.threadMask := RegNext(alu.io.alu.threadMask, 0.U)
  wb.io.alu.warp := RegNext(alu.io.alu.warp, 0.U)
  wb.io.alu.done := RegNext(alu.io.alu.done, false.B)
  wb.io.alu.we := RegNext(alu.io.alu.we, false.B)
  wb.io.alu.dest := RegNext(alu.io.alu.dest, 0.U)
  wb.io.alu.out := RegNext(alu.io.alu.out, 0.U)

  // Pipeline registers between the memory and write-back stages
  wb.io.mem.threadMask := RegNext(mem.io.mem.threadMask, 0.U)
  wb.io.mem.warp := RegNext(mem.io.mem.warp, 0.U)
  wb.io.mem.we := RegNext(mem.io.mem.we, false.B)
  wb.io.mem.dest := RegNext(mem.io.mem.dest, 0.U)
  wb.io.mem.out := RegNext(mem.io.mem.out, 0.U)

  // Connect operand fetch with write-back
  of.io.wb <> wb.io.wbOf

  // backend control signals
  io.lsu <> mem.io.lsu
  io.wbIfCtrl <> wb.io.wbIfCtrl
  io.memIfCtrl <> mem.io.memIfCtrl
  io.nzpUpdateCtrl <> alu.io.nzpUpdateCtrl
  io.memStall := (mem.io.memStall || of.io.ofContainsMemInstr)
  io.wbOutTest := wb.io.outTest
}
