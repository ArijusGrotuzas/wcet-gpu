package SM.Backend

import SM.Isa
import chisel3._

class AluPipeline(warpSize: Int) extends Module {
  val io = IO(new Bundle {
    val of = new Bundle {
      val warp = Input(UInt(2.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt((32 * warpSize).W))
      val rs2 = Input(UInt((32 * warpSize).W))
      val rs3 = Input(UInt((32 * warpSize).W))
      val imm = Input(UInt(22.W))
    }

    val alu = new Bundle {
      val warp = Output(UInt(2.W))
      val done = Output(Bool())
      val valid = Output(Bool())
      val dest = Output(UInt(5.W))
      val out = Output(UInt((32 * warpSize).W))
    }
  })

  val out = VecInit(Seq.fill(warpSize)(0.S(32.W)))
  val done = WireDefault(false.B)
  val valid = WireDefault(false.B)

  when(io.of.opcode === Isa.RET) {
    done := true.B
  }

  when(io.of.opcode === 0.U) {
    valid := true.B
  }

  for (i <- 0 until warpSize) {
    val alu = Module(new Alu(32))
    alu.io.a := io.of.rs1(((i + 1) * 32) - 1, i * 32).asSInt
    alu.io.b := io.of.rs2(((i + 1) * 32) - 1, i * 32).asSInt
    alu.io.op := io.of.opcode
    out(i) := alu.io.out
  }

  io.alu.warp := io.of.warp
  io.alu.dest := io.of.dest
  io.alu.done := done
  io.alu.valid := valid
  io.alu.out := out.asUInt
}