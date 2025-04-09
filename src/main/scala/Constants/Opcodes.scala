package Constants

object Opcodes {
  val NOP = 0x00 // 00000
  val RET = 0x1F // 11111
  // Loads/Stores
  val LD = 0x01 // 00001
  val ST = 0x05 // 00101
  val LDS = 0x09 // 01001
  // Immediate instructions
  val ADDI = 0x0D // 01101
  val LUI = 0x11 // 10001
  val SRLI = 0x15 // 10101
  val SLLI = 0x19 // 11001
  // Arithmetic
  val ADD = 0x03 // 00011
  val SUB = 0x07 // 00111
  val AND = 0x0B // 01011
  val OR = 0x0F // 01111
  val MUL = 0x13 // 10011
  // Large Arithmetic
  val MAD = 0x17 // 10111
  // Control
  val BR = 0x02 // 00010
  val CMP = 0x06 // 00110
  val PREPARE = 0x0A // 01010
  val SPLIT = 0x0E // 01110
  val JOIN = 0x12 // 10010

  def getOpcodes: Map[String, Int] = {
    val opcodeNames = Map(
      "nop" -> NOP,
      "ret" -> RET,
      "ld" -> LD,
      "st" -> ST,
      "lds" -> LDS,
      "addi" -> ADDI,
      "lui" -> LUI,
      "srli" -> SRLI,
      "slli" -> SLLI,
      "add" -> ADD,
      "sub" -> SUB,
      "and" -> AND,
      "or" -> OR,
      "mul" -> MUL,
      "mad" -> MAD,
      "br" -> BR,
      "cmp" -> CMP,
      "prepare" -> PREPARE,
      "split" -> SPLIT,
      "join" -> JOIN
    )
    opcodeNames
  }
}
