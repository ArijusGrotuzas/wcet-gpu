package Util

import Constants.Opcodes

import java.io.{File, PrintWriter}
import scala.io._

/**
 * Compiles the assembly code into machine code for the SM ISA.
 */
object Assembler {
  private val labels = collection.mutable.Map[String, Int]()

  def assembleProgram(file: String): Array[Int] = {
    findLabels(file)
    assemble(file)
  }

  private def findLabels(file: String): Unit = {
    val source = Source.fromFile(file)
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]+")
      val instr = getLabel(tokens, pc)

      instr match {
        case _: Int =>
          pc += 1
        case _ =>
      }
    }
  }

  private def getLabel(tokens: Array[String], pc: Int): Any = {
    val Pattern = "(.*:)".r

    val label = tokens(0) match {
      case Pattern(t) => labels += (t.substring(0, t.length - 1) -> pc)
      case t if Opcodes.getOpcodes.keys.toArray contains t => 0
      case t if t.startsWith("@") => 0
      case "//" => //
      case "" => //
      case _ => //
    }

    label
  }

  private def assemble(file: String): Array[Int] = {
    val source = Source.fromFile(file)
    var program = List[Int]()
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]+")
      val instr = assembleInstruction(tokens, pc, file)

      instr match {
        case a: Int =>
          program = a :: program
          pc += 1
        case _ =>
      }
    }

    program.reverse.toArray
  }

  private def assembleInstruction(tokens: Array[String], pc: Int, fileName: String): Any = {
    val Pattern = "(.*:)".r

    val instr = tokens(0) match {
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
      case "prepare" => ((getBrnOff(tokens(1), pc) << 5) & 0x3FFFFFFF) + Opcodes.PREPARE
      case "split" => ((getBrnOff(tokens(1), pc) << 5) & 0x3FFFFFFF) + Opcodes.SPLIT
      case "join" => Opcodes.JOIN
      case "cmp" => (getVecRegNum(tokens(3), pc) << 15) + (getVecRegNum(tokens(2), pc) << 10) + (getNZP(tokens(1)) << 5) + Opcodes.CMP
      case t if t.startsWith("@") => (getPredicateReg(tokens(0)) << 30) + convertToInt(assembleInstruction(tokens.drop(1), pc, fileName)) // Predicate
      case Pattern(t) => //
      case "//" => // Comment
      case "" => // Empty line
      case t: String => throw new Exception("Unexpected instruction: " + t + " in file: " + fileName + ", at line: " + pc)
    }

    instr
  }

  private def getConst(s: String): Int = {
    if (s.startsWith("0x")) {
      Integer.parseInt(s.substring(2), 16) & 0xff
    } else if (s.startsWith("<")) {
      labels(s.drop(1)) & 0xff
    } else if (s.startsWith(">")) {
      labels(s.drop(1)) >> 8
    } else {
      Integer.parseInt(s) & 0xffff
    }
  }

  private def getVecRegNum(s: String, pc: Int): Int = {
    assert(s.startsWith("x"), "Vector register number must start with an \'x\' :" + pc)
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
    val brnOff = labels(s) - pc
    brnOff
  }

  // TODO: Add checking if any instruction attempts to read two or more operands from the same bank
  private def vrfBankConflicts(registers: List[Int], pc: Int): Int = {
    val banks = registers.map(i => i % 4).distinct
    val noConflict = banks.diff(banks.distinct).distinct.isEmpty

    if (! noConflict) {
      throw new Exception("Bank conflicts at line: " + pc)
    }

    0
  }

  private def convertToInt(value: Any): Int = {
    value match {
      case i: Int => i
      case _ => 0
    }
  }
}

object Main extends App {
  val files = getListOfFiles("asm")

  def getListOfFiles(dir: String): List[String] = {
    val file = new File(dir)
    file.listFiles.filter(_.isFile)
      .filter(_.getName.endsWith(".asm"))
      .map(_.getPath).toList
  }

  // Assemble all the programs contained in the asm directory
  for (file <- files) {
    val program = (Assembler.assembleProgram(file) :+ 0).map(i => f"${i & 0xFFFFFFFF}%08X").mkString("\n")

    println(file + ":")
    println(program)
    println("")

    new PrintWriter(file.replace(".asm", ".hex").replace("asm/", "hex/instructions/")) {
      write(program); close()
    }
  }
}
