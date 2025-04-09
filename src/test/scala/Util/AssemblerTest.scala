package Util

import org.scalatest.flatspec.AnyFlatSpec

class AssemblerTest extends AnyFlatSpec {
  "Assembler" should "assemble a simple program" in {
    val program = Assembler.assembleProgram("src/test/scala/resources/simple.asm")
    // TODO: Assert program length and values
  }

  it should "handle labels correctly" in {
    val program = Assembler.assembleProgram("src/test/scala/resources/labels.asm")
    // TODO: Assert program length and values
  }
}
