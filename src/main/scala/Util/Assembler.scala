package Util

import scala.io._
import Constants.Opcodes

object Assembler {
  // collect destination addresses in first pass
  private val symbols = collection.mutable.Map[String, Int]()

  def assembleProgram(prog: String): Array[Int] = {
    findSymbols(prog)
    assemble(prog)
  }

  private def findSymbols(prog: String): Unit = {
    val source = Source.fromFile(prog)
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]")
      val Pattern = "(.*:)".r

      tokens(0) match {
        case Pattern(l) => symbols += (l.substring(0, l.length - 1) -> pc)
        case _ =>
      }

      pc += 1
    }
  }

  private def assemble(prog: String): Array[Int] = {
    val source = Source.fromFile(prog)
    var program = List[Int]()
    var pc = 0

    for (line <- source.getLines()) {
      val tokens = line.trim.split("[,\\s]+")
      def brOff: Int = (symbols(tokens(1)) - pc) & 0x0fff

      val instr = tokens(0) match {
        case "//" => // comment
        case "nop" => Opcodes.NOP
        case "ret" => Opcodes.RET
        case "ld" => Opcodes.LD // TODO: Finish
        case "st" => Opcodes.ST // TODO: Finish
        case "lds" => Opcodes.LDS // TODO: Finish
        case "addi" => (getConst(tokens(3)) << 15) + (getRegNumber(tokens(2)) << 10) + (getRegNumber(tokens(1)) << 5) + Opcodes.ADDI
        case "lui" => (getConst(tokens(2)) << 10) + (getRegNumber(tokens(1)) << 5) + Opcodes.LUI
        case "add" => (getRegNumber(tokens(3)) << 15) + (getRegNumber(tokens(2)) << 10) + (getRegNumber(tokens(1)) << 5) + Opcodes.ADD
        case "sub" => (getRegNumber(tokens(3)) << 15) + (getRegNumber(tokens(2)) << 10) + (getRegNumber(tokens(1)) << 5) + Opcodes.SUB
        case "and" => (getRegNumber(tokens(3)) << 15) + (getRegNumber(tokens(2)) << 10) + (getRegNumber(tokens(1)) << 5) + Opcodes.AND
        case "or" => (getRegNumber(tokens(3)) << 15) + (getRegNumber(tokens(2)) << 10) + (getRegNumber(tokens(1)) << 5) + Opcodes.OR
        case "mul" => Opcodes.MUL // TODO: Finish
        case "mad" => Opcodes.MAD // TODO: Finish
        case "brnzp" => Opcodes.BRNZP // TODO: Finish
        case "cmp" => Opcodes.CMP // TODO: Finish
        case "" => // println("Empty line")
        case t: String => throw new Exception("Unexpected instruction: " + t)
        case _ => throw new Exception("Unhandled case")
      }

      instr match {
        case a: Int =>
          program = a :: program
          pc += 1
        case _ =>
      }
    }

    program.reverse.toArray
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

  private def getRegNumber(s: String): Int = {
    assert(s.startsWith("x"), "Register numbers must start with \'x\'")
    s.substring(1).toInt
  }
}

object Main extends App {
  private val program = Assembler.assembleProgram("asm/kernel1.asm")

  for (i <- program) {
    val instr = f"${i & 0xFFFFFFFF}%08X"
    println(instr)
  }
}
