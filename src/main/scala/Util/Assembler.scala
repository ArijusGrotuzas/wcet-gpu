package Util

import Constants.Opcodes

import scala.io._

// TODO: Add processing of predicate value
object Assembler {
  private val symbols = collection.mutable.Map[String, Int]()

  def assembleProgram(prog: String): Array[Int] = {
    findSymbols(prog)
    assemble(prog)
  }

  private def findSymbols(prog: String): Unit = {
    val source = Source.fromFile(prog)
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]+")
      val instr = assembleInstruction(tokens, pc, getSymbols = true)

      instr match {
        case _: Int =>
          pc += 1
        case _ =>
      }
    }
  }

  private def assemble(prog: String): Array[Int] = {
    val source = Source.fromFile(prog)
    var program = List[Int]()
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]+")
      val instr = assembleInstruction(tokens, pc, getSymbols = false)

      instr match {
        case a: Int =>
          program = a :: program
          pc += 1
        case _ =>
      }
    }

    program.reverse.toArray
  }

  private def assembleInstruction(tokens: Array[String], pc: Int, getSymbols: Boolean): Any = {
    val Pattern = "(.*:)".r

    val instr = tokens(0) match {
      case Pattern(l) => if (getSymbols) symbols += (l.substring(0, l.length - 1) -> pc)
      case "nop" => Opcodes.NOP
      case "ret" => Opcodes.RET
      case "ld" => (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.LD
      case "st" => (getVecRegNum(tokens(2)) << 15) + (getVecRegNum(tokens(1)) << 10) + Opcodes.ST
      case "lds" => (getSpRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.LDS
      case "addi" => (getConst(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.ADDI // TODO: And the immediate value so it doesn't spill into predicate bit field
      case "lui" => (getConst(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.LUI // TODO: And the immediate value so it doesn't spill into predicate bit field
      case "add" => (getVecRegNum(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.ADD
      case "sub" => (getVecRegNum(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.SUB
      case "and" => (getVecRegNum(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.AND
      case "or" => (getVecRegNum(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.OR
      case "mul" => (getVecRegNum(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.MUL
      case "mad" => (getVecRegNum(tokens(4)) << 20) + (getVecRegNum(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1)) << 5) + Opcodes.MAD
      case "br" => ((getBrnOff(tokens(1), pc) << 5) & 0x3FFFFFFF) + Opcodes.BR
      case "cmp" => (getVecRegNum(tokens(3)) << 15) + (getVecRegNum(tokens(2)) << 10) + (getNZP(tokens(1)) << 5) + Opcodes.CMP
      case "split" => println("Split instruction not yet implemented")
      case "join" => println("Join instruction not yet implemented")
      case s if s.startsWith("@") => (getPredicateReg(tokens(0)) << 30) + convertToInt(assembleInstruction(tokens.drop(1), pc, getSymbols)) // Predicate
      case "//" => // Comment
      case "" => // Empty line
      case t: String => throw new Exception("Unexpected instruction: " + t)
      case _ => throw new Exception("Unhandled case")
    }

    instr
  }

  def convertToInt(value: Any): Int = {
    value match {
      case i: Int => i
      case _ => 0
    }
  }

  private def getConst(s: String): Int = {
    if (s.startsWith("0x")) {
      Integer.parseInt(s.substring(2), 16) & 0xff
    } else if (s.startsWith("<")) {
      symbols(s.drop(1)) & 0xff
    } else if (s.startsWith(">")) {
      symbols(s.drop(1)) >> 8
    } else {
      Integer.parseInt(s) & 0xffff
    }
  }

  private def getVecRegNum(s: String): Int = {
    assert(s.startsWith("x"), "Vector register number must start with an \'x\'")
    s.substring(1).toInt
  }

  private def getSpRegNum(s: String): Int = {
    assert(s.startsWith("s"), "Special register number must start with an \'s\'")
    s.substring(1).toInt
  }

  private def getPredicateReg(s: String): Int = {
    assert(s.startsWith("@p"), "Predicate register number must start with an \'@p\'")
    s.substring(2).toInt
  }

  private def getNZP(s: String): Int = {
    assert(s.startsWith("%"), "NZP values must start with a \'%\'")
    val encoding = s.substring(1) match {
      case "nz" => 6 // Less than or equal
      case "np" => 5 // Greater than or equal
      case "n" => 4 // Less than
      case "zp" => 3 // Not equal
      case "z" => 2 // Equal
      case "p" => 1 // Greater than
      case _ => throw new Exception("Invalid NZP value")
    }

    encoding
  }

  private def getBrnOff(s: String, pc: Int): Int = {
    val brnOff = symbols(s) - pc
    brnOff
  }
}

object Main extends App {
  private val program = Assembler.assembleProgram("asm/kernel2.asm")

  for (i <- program) {
    val instr = f"${i & 0xFFFFFFFF}%08X"
    println(instr)
  }
}
