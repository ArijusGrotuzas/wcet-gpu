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
      val srs = Input(UInt(3.W)) // TODO: Turn this into an already ready operand
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

    val intermediate = Mux(sel(1), in3, in2)
    out := Mux(sel(0), intermediate, in1)

    out
  }

  val out = VecInit(Seq.fill(warpSize)(0.S(32.W)))
  val cmpOut = VecInit(Seq.fill(warpSize)(false.B))
  val blockIdxReg = RegInit(0.U(blockAddrLen.W))

  when(io.aluInitCtrl.setBlockIdx) {
    blockIdxReg := io.aluInitCtrl.blockIdx
  }

  // ALU lane control unit
  val aluLaneCtrl = Module(new AluControl)
  val func3 = io.of.dest(2, 0)

  aluLaneCtrl.io.instrOpcode := io.of.opcode
  aluLaneCtrl.io.func3 := func3

  // Generate different ALU lanes
  for (i <- 0 until warpSize) {
    val alu = Module(new Alu(32))

    val rs1 = io.of.rs1(((i + 1) * 32) - 1, i * 32).asSInt
    val rs2 = io.of.rs2(((i + 1) * 32) - 1, i * 32).asSInt
    val rs3 = io.of.rs3(((i + 1) * 32) - 1, i * 32).asSInt

    // TODO: Move srf to register file to avoid having MUXes to depend on this combinational path
    // Special register file output
    val srs = getSpecialValue(io.of.srs, i, blockIdxReg)

    // Multiplier
    val mulProd = rs1 * rs2
    val mac = mulProd + rs3

    // Select the first operand
    val a = Mux(aluLaneCtrl.io.aSel, srs, rs1)
    alu.io.a := a

    // Select the second operand
    val b = Mux(aluLaneCtrl.io.bSel, io.of.imm, rs2)
    alu.io.b := b

    // Set the alu operation
    alu.io.op := aluLaneCtrl.io.aluOp

    out(i) := Mux3To1(aluLaneCtrl.io.resSel, alu.io.out, mulProd, mac)

    // TODO: Perform comparisons in parallel with ALU operations to avoid having long critical paths
    // Comparison results
    switch(func3) {
      is("b001".U) { cmpOut(i) := alu.io.pos } // Greater than
      is("b010".U) { cmpOut(i) := alu.io.zero } // Equal
      is("b011".U) { cmpOut(i) := alu.io.zero || alu.io.pos } // Greater than or equal to
      is("b100".U) { cmpOut(i) := alu.io.neg } // Less than
      is("b101".U) { cmpOut(i) := (alu.io.neg || alu.io.pos) & !alu.io.zero } // Not equal
      is("b110".U) { cmpOut(i) := alu.io.neg || !alu.io.zero } // Less than or equal to
    }
  }

  // Alu pipeline outputs to write-back
  io.alu.threadMask := io.of.threadMask
  io.alu.warp := io.of.warp
  io.alu.dest := io.of.dest
  io.alu.done := aluLaneCtrl.io.done
  io.alu.we := aluLaneCtrl.io.we
  io.alu.out := out.asUInt

  // Alu pipeline control signals
  io.predUpdateCtrl.pred := cmpOut.asUInt
  io.predUpdateCtrl.en := aluLaneCtrl.io.predUpdate
  io.predUpdateCtrl.addr := io.of.warp ## io.of.pred
}