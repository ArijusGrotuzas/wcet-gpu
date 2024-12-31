package SM.Backend.Alu

import chisel3._
import chisel3.util._

object AluOps{
  val ADD = "b000".U
  val SUB = "b001".U
  val AND = "b010".U
  val OR = "b011".U
  val SRL = "b100".U
  val SLL = "b101".U
  val FORA = "b110".U
  val FORB = "b111".U
}

class Alu(operandWidth: Int) extends Module {
  val io = IO(new Bundle{
    // Inputs
    val a = Input(SInt(operandWidth.W))
    val b = Input(SInt(operandWidth.W))
    val op = Input(UInt(4.W))

    // Outputs
    val zero = Output(Bool())
    val out = Output(SInt(operandWidth.W))
  })

  // Default value
  val out = WireDefault(0.S(operandWidth.W))

  switch (io.op) {
    is(AluOps.ADD) {out := io.a + io.b}
    is(AluOps.SUB) {out := io.a - io.b}
    is(AluOps.AND) {out := io.a & io.b}
    is(AluOps.OR) {out := io.a | io.b}
    is(AluOps.FORA) {out := io.a}
    is(AluOps.FORB) {out := io.b}
  }

  io.zero := !out.asUInt.orR
  io.out := out
}
