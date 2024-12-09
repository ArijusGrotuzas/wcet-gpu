package SM.Backend

import chisel3._
import chisel3.util._

object AluOps{
  val ADD = "b0000".U
  val SUB = "b0001".U
  val AND = "b0010".U
  val OR = "b0011".U
  val XOR = "b0100".U
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
    is(AluOps.XOR) {out := io.a ^ io.b}
  }

  io.zero := !out.asUInt.orR
  io.out := out
}
