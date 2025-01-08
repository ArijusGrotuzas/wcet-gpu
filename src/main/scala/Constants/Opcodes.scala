package Constants

object Opcodes {
  val NOP = 0x00 // 00000
  val RET = 0x1F // 11111
  // Loads/Stores
  val LD = 0x01 // 00001
  val ST = 0x05 // 00101
  val LDS = 0x11 // 01001
  // Immediate instructions
  val ADDI = 0x09 // 01001
  val LUI = 0x0D // 01101
  // Arithmetic
  val ADD = 0x03 // 00011
  val SUB = 0x07 // 00111
  val AND = 0x0B // 01011
  val OR = 0x0F // 01111
  val MUL = 0x13 // 10011
  // Large Arithmetic
  val MAD = 0x17 // 10011
  // Control
  val BRNZP = 0x02 // 00010
  val CMP = 0x06 // 00010
}
