package SM.Backend.Alu

import chisel3._
import chisel3.util._
import Constants.AluOps


class Alu(operandWidth: Int) extends Module {
  val io = IO(new Bundle{
    // Inputs
    val a = Input(SInt(operandWidth.W))
    val b = Input(SInt(operandWidth.W))
    val op = Input(UInt(4.W))

    // Outputs
    val zero = Output(Bool())
    val neg = Output(Bool())
    val pos = Output(Bool())
    val out = Output(SInt(operandWidth.W))
  })

  // Default value
  val out = WireDefault(0.S(operandWidth.W))

  switch (io.op) {
    is(AluOps.ADD.asUInt(3.W)) {out := io.a + io.b}
    is(AluOps.SUB.asUInt(3.W)) {out := io.a - io.b}
    is(AluOps.AND.asUInt(3.W)) {out := io.a & io.b}
    is(AluOps.OR.asUInt(3.W)) {out := io.a | io.b}
    is(AluOps.SRL.asUInt(3.W)) {out := (io.a >> io.b.asUInt).asSInt}
    // TODO: Synthesizing the SLL results in errors in Quartus
    // is(AluOps.SLL.asUInt(3.W)) {out := (io.a << io.b.asUInt(18, 0)).asSInt}
    is(AluOps.FORA.asUInt(3.W)) {out := io.a}
    is(AluOps.FORB.asUInt(3.W)) {out := io.b}
  }

  io.zero := !out.asUInt.orR
  io.neg := out < 0.S
  io.pos := out > 0.S
  io.out := out
}
