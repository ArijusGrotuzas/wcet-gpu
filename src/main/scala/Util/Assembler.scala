package Util

import Constants.Opcodes
import scala.io._
import java.io.File
import java.io.PrintWriter


object Assembler {
  private val symbols = collection.mutable.Map[String, Int]()

  def assembleProgram(file: String): Array[Int] = {
    findSymbols(file)
    assemble(file)
  }

  private def findSymbols(file: String): Unit = {
    val source = Source.fromFile(file)
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]+")
      val instr = assembleInstruction(tokens, pc, file, getSymbols = true)

      instr match {
        case _: Int =>
          pc += 1
        case _ =>
      }
    }
  }

  private def assemble(file: String): Array[Int] = {
    val source = Source.fromFile(file)
    var program = List[Int]()
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]+")
      val instr = assembleInstruction(tokens, pc, file, getSymbols = false)

      instr match {
        case a: Int =>
          program = a :: program
          pc += 1
        case _ =>
      }
    }

    program.reverse.toArray
  }

  private def assembleInstruction(tokens: Array[String], pc: Int, file: String, getSymbols: Boolean): Any = {
    val Pattern = "(.*:)".r

    val instr = tokens(0) match {
      case Pattern(l) => if (getSymbols) symbols += (l.substring(0, l.length - 1) -> pc)
      case "nop" => Opcodes.NOP
      case "ret" => Opcodes.RET
      case "ld" => (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.LD
      case "st" => (getVecRegNum(tokens(2), pc) << 15) + (getVecRegNum(tokens(1), pc) << 10) + Opcodes.ST
      case "lds" => (getSpRegNum(tokens(2)) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.LDS
      case "addi" => ((getConst(tokens(3)) << 15) & 0x07FFFFFF) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.ADDI
      case "lui" => ((getConst(tokens(2)) << 10) & 0x3FFFFFFF) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.LUI
      case "srli" => ((getConst(tokens(3)) << 15) & 0x07FFFFFF) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.SRLI
      case "add" => (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.ADD
      case "sub" => (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.SUB
      case "and" => (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.AND
      case "or" => (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.OR
      case "mul" => (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.MUL
      case "mad" => (getVecRegNum(tokens(4), pc) << 20) + (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getVecRegNum(tokens(1), pc) << 5) + Opcodes.MAD
      case "br" => ((getBrnOff(tokens(1), pc) << 5) & 0x3FFFFFFF) + Opcodes.BR
      case "cmp" => (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getNZP(tokens(1)) << 5) + Opcodes.CMP
      case "split" => println("Split instruction not yet implemented")
      case "join" => println("Join instruction not yet implemented")
      case s if s.startsWith("@") => (getPredicateReg(tokens(0)) << 30) + convertToInt(assembleInstruction(tokens.drop(1), pc, file, getSymbols)) // Predicate
      case "//" => // Comment
      case "" => // Empty line
      case t: String => throw new Exception("Unexpected instruction: " + t + " in file" + file)
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

  private def getVecRegNum(s: String, pc: Int): Int = {
    assert(s.startsWith("x"), "Vector register number must start with an \'x\' :" + pc )
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

  private def vrfBankConflicts(registers: List[Int], pc: Int): Int = {
    val banks = registers.map(i => i % 4).distinct
    val noConflict = banks.diff(banks.distinct).distinct.isEmpty

    if (noConflict) { 0 } else { throw new Exception("Bank conflicts for instruction at : " + pc) }
  }
}

// TODO: Add checking if any instruction attempts to read two or more operands from the same bank
object Main extends App {
  def getListOfFiles(dir: String): List[String] = {
    val file = new File(dir)
    file.listFiles.filter(_.isFile)
      .filter(_.getName.endsWith(".asm"))
      .map(_.getPath).toList
  }

  val files = getListOfFiles("asm")

  // Assemble all the programs contained in the asm directory
  for (file <- files) {
    val program = Assembler.assembleProgram(file).map(i => f"${i & 0xFFFFFFFF}%08X").mkString("\n")

    println(file + ":")
    println(program)
    println("")

    new PrintWriter(file.replace(".asm", ".hex").replace("asm/", "hex/instructions/")) { write(program); close() }
  }
}
