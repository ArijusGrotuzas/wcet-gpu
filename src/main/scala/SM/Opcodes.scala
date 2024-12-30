package SM

import chisel3._

object Opcodes {
  val NOP = "b00000".U
  val RET = "b11111".U
  // Loads/Stores
  val LD = "b00001".U
  val ST = "b00101".U
  val ADDI = "b01001".U
  val LUI = "b01101".U
  // Arithmetic
  val ADD = "b00011".U
  val SUB = "b00111".U
  val AND = "b01011".U
  val OR = "b01111".U
}
