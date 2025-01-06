package SM.Backend.Alu

import chisel3._
import chisel3.util._

class AluPipeline(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  val blockAddrLen = log2Up(blockCount)
  val warpAddrLen = log2Up(warpCount)

  val io = IO(new Bundle {
    val of = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt((32 * warpSize).W))
      val rs2 = Input(UInt((32 * warpSize).W))
      val rs3 = Input(UInt((32 * warpSize).W))
      val srs = Input(UInt(3.W))
      val imm = Input(SInt(32.W))
    }

    val aluInitCtrl = new Bundle {
      val setBlockIdx = Input(Bool())
      val blockIdx = Input(UInt(blockAddrLen.W))
    }

    val alu = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val done = Output(Bool())
      val we = Output(Bool())
      val dest = Output(UInt(5.W))
      val out = Output(UInt((32 * warpSize).W))
    }

    val nzpUpdate = new Bundle {
      val nzp = Output(UInt(3.W))
      val en = Output(Bool())
      val warp = Output(UInt(warpAddrLen.W))
    }
  })

  private def getSpecialValue(srs: UInt, laneId: Int, blockIdx: UInt): SInt = {
    val out = WireDefault(0.S(32.W))

    switch(srs) {
      is(0.U) { out := (0.U ## laneId.U).asSInt } // thread ID
      is(1.U) { out := (0.U ## io.of.warp).asSInt } // warp ID
      is(2.U) { out := (0.U ## blockIdx).asSInt } // block ID
      is(3.U) { out := (0.U ## warpSize.U).asSInt } // warp width
      is(4.U) { out := (0.U ## warpSize.U).asSInt } // block width
    }

    out
  }

  val blockIdxReg = RegInit(0.U(blockAddrLen.W))

  when(io.aluInitCtrl.setBlockIdx) {
    blockIdxReg := io.aluInitCtrl.blockIdx
  }

  // ALU lane control unit
  val aluCtrl = Module(new AluControl)
  aluCtrl.io.instrOpcode := io.of.opcode

  val out = VecInit(Seq.fill(warpSize)(0.S(32.W)))
  val nzp = WireDefault(0.U(3.W))
  val done = WireDefault(false.B)
  val we = WireDefault(false.B)
  val nzpUpdate = WireDefault(false.B)

  done := aluCtrl.io.done
  we := aluCtrl.io.we
  nzpUpdate := aluCtrl.io.nzpUpdate

  // Generate different ALU lanes
  for (i <- 0 until warpSize) {
    val alu = Module(new Alu(32))

    // Select the first operand
    val srs = getSpecialValue(io.of.srs, i, blockIdxReg)
    val a = Mux(aluCtrl.io.rs1Sel, srs, io.of.rs1(((i + 1) * 32) - 1, i * 32).asSInt)
    alu.io.a := a

    // Select the second operand
    val b = Mux(aluCtrl.io.rs2Sel, io.of.imm, io.of.rs2(((i + 1) * 32) - 1, i * 32).asSInt)
    alu.io.b := b

    alu.io.op := aluCtrl.io.aluOp
    out(i) := alu.io.out

    if (i == 0) {
      nzp := !alu.io.neg ## alu.io.zero ## alu.io.neg
    }
  }

  io.alu.warp := io.of.warp
  io.alu.dest := io.of.dest
  io.alu.done := done
  io.alu.we := we
  io.alu.out := out.asUInt

  io.nzpUpdate.nzp := nzp
  io.nzpUpdate.en := nzpUpdate
  io.nzpUpdate.warp := io.of.warp
}