package SM.Frontend

import chisel3._
import chisel3.util._

class InstructionDecode extends Module {
  val io = IO(new Bundle {
    val pcDec = Input(UInt(32.W))
    val instrDec = Input(UInt(32.W))
    val warpDec = Input(UInt(2.W))

    val opcode = Output(UInt(5.W))
    val dest = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val rs3 = Output(UInt(5.W))
    val imm = Output(UInt(22.W))

    val pcIss = Output(UInt(32.W))
    val warpIss = Output(UInt(2.W))
  })

  val imm = WireDefault(0.U(22.W))
  val opcode = WireDefault(0.U(5.W))

  opcode := io.instrDec(4, 0)

  // TODO: Extract the correct Immediate value based on instruction type

  io.dest := io.instrDec(9, 5)
  io.rs1 := io.instrDec(14, 10)
  io.rs2 := io.instrDec(19, 15)
  io.rs3 := io.instrDec(24, 20)
  io.opcode := opcode
  io.imm := imm
  io.pcIss := io.pcDec
  io.warpIss := io.warpDec
}
