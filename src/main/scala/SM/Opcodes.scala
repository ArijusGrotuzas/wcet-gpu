package SM

import chisel3._

object Opcodes {
  val NOP = "b00000".U //*
  val RET = "b11111".U //*
  // Loads/Stores
  val LD = "b00001".U // TODO: Implement
  val ST = "b00101".U // TODO: Implement
  // Immediate instructions
  val ADDI = "b01001".U //*
  val ADDIU = "b01101".U // TODO: Implement for adding the lower bits to upper immediate
  val LUI = "b01101".U //*
  // Arithmetic
  val ADD = "b00011".U //*
  val SUB = "b00111".U //*
  val AND = "b01011".U //*
  val OR = "b01111".U //*
  val MAD = "b10011".U // TODO: Implement
  // Branch
  val BRNZP = "b00010".U //*
  val CMP = "b00110".U //*
}
