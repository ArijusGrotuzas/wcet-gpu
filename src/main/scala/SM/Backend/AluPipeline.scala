package SM.Backend

import SM.Backend.Alu._
import chisel3._
import chisel3.util._

class AluPipeline(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  val blockAddrLen = log2Up(blockCount)
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val of = new Bundle {
      val threadMask = Input(UInt(warpSize.W))
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt((32 * warpSize).W))
      val rs2 = Input(UInt((32 * warpSize).W))
      val rs3 = Input(UInt((32 * warpSize).W))
      val srs = Input(UInt(3.W))
      val imm = Input(SInt(32.W))
      val pred = Input(UInt(2.W))
    }

    val aluInitCtrl = new Bundle {
      val setBlockIdx = Input(Bool())
      val blockIdx = Input(UInt(blockAddrLen.W))
    }

    val alu = new Bundle {
      val threadMask = Output(UInt(warpSize.W))
      val warp = Output(UInt(warpAddrLen.W))
      val done = Output(Bool())
      val we = Output(Bool())
      val dest = Output(UInt(5.W))
      val out = Output(UInt((32 * warpSize).W))
    }

    val predUpdateCtrl = new Bundle {
      val en = Output(Bool())
      val pred = Output(UInt(warpSize.W))
      val addr = Output(UInt((warpAddrLen + 2).W))
    }
  })

  private def getSpecialValue(srs: UInt, laneId: Int, blockIdx: UInt): SInt = {
    val out = WireDefault(0.S(32.W))

    switch(srs) {
      is(0.U) { out := (0.U ## laneId.U).asSInt } // thread ID
      is(1.U) { out := (0.U ## io.of.warp).asSInt } // warp ID
      is(2.U) { out := (0.U ## blockIdx).asSInt } // block ID
      is(3.U) { out := (0.U ## warpSize.U).asSInt } // warp width
      is(4.U) { out := (0.U ## (warpCount * warpSize).U).asSInt } // block width
    }

    out
  }

  private def Mux3To1(sel: UInt, in1: SInt, in2: SInt, in3: SInt): SInt = {
    val out = WireDefault(0.S(32.W))

    val intermediate = Mux(sel(1), in2, in1)
    out := Mux(sel(0), in3, intermediate)

    out
  }

  // Block index register
  val blockIdxReg = RegInit(0.U(blockAddrLen.W))
  when(io.aluInitCtrl.setBlockIdx) {
    blockIdxReg := io.aluInitCtrl.blockIdx
  }

  // ALU lane control unit
  val aluCtrl = Module(new AluControl)
  val func3 = io.of.dest(2, 0)
  aluCtrl.io.instrOpcode := io.of.opcode
  aluCtrl.io.func3 := func3

  val out = VecInit(Seq.fill(warpSize)(0.S(32.W)))
  val cmpOut = VecInit(Seq.fill(warpSize)(false.B))

  // Generate different ALU lanes
  for (i <- 0 until warpSize) {
    val alu = Module(new Alu(32))

    val rs1 = io.of.rs1(((i + 1) * 32) - 1, i * 32).asSInt
    val rs2 = io.of.rs2(((i + 1) * 32) - 1, i * 32).asSInt
    val rs3 = io.of.rs3(((i + 1) * 32) - 1, i * 32).asSInt

    val srs = getSpecialValue(io.of.srs, i, blockIdxReg)

    // Multiplier
    val mulProd = rs1 * rs2

    // Select the first operand
    val a = Mux3To1(aluCtrl.io.aSel, rs1, mulProd, srs)
    alu.io.a := a

    // Select the second operand
    val b = Mux3To1(aluCtrl.io.bSel, rs2, io.of.imm, rs3)
    alu.io.b := b

    alu.io.op := aluCtrl.io.aluOp

    out(i) := alu.io.out

    switch(func3) {
      is("b001".U) { cmpOut(i) := alu.io.pos } // Greater than
      is("b010".U) { cmpOut(i) := alu.io.zero } // Equal
      is("b011".U) { cmpOut(i) := alu.io.zero || alu.io.pos } // Greater than or equal to
      is("b100".U) { cmpOut(i) := alu.io.neg  } // Less than
      is("b101".U) { cmpOut(i) := (alu.io.neg || alu.io.pos) & !alu.io.zero } // Not equal
      is("b110".U) { cmpOut(i) := alu.io.neg || !alu.io.zero  } // Less than or equal to
    }
  }

  // Alu pipeline outputs to write-back
  io.alu.threadMask := io.of.threadMask
  io.alu.warp := io.of.warp
  io.alu.dest := io.of.dest
  io.alu.done := aluCtrl.io.done
  io.alu.we := aluCtrl.io.we
  io.alu.out := out.asUInt

  // Alu pipeline control signals
  io.predUpdateCtrl.pred := cmpOut.asUInt
  io.predUpdateCtrl.en := aluCtrl.io.nzpUpdate
  io.predUpdateCtrl.addr := io.of.warp ## io.of.pred
}