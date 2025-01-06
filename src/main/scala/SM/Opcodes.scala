package SM

import chisel3._

object Opcodes {
  val NOP = "b00000".U // X 00000
  val RET = "b11111".U // X 11111
  // Loads/Stores
  val LD = "b00001".U // TODO: Implement
  val ST = "b00101".U // TODO: Implement
  val LDS = "b10001".U // X xxSRS RD 01001
  // Immediate instructions
  val ADDI = "b01001".U // IMM RS1 RD 01001
  val LUI = "b01101".U // X IMM RD 01101
  // Arithmetic
  val ADD = "b00011".U // X RS2 RS1 RD 00011
  val SUB = "b00111".U // X RS2 RS1 RD 00111
  val AND = "b01011".U // X RS2 RS1 RD 01011
  val OR = "b01111".U // X RS2 RS1 RD 01111
  val MUL = "b10011".U // X RS2 RS1 RD 10011
  // Large Arithmetic
  val MAD = "b10111".U // X RS3 RS2 RS1 RD 10011
  // Control
  val BRNZP = "b00010".U // IMM xxNZP 00010
  val CMP = "b00110".U // X RS2 RS1 xxxxx 00010
}
