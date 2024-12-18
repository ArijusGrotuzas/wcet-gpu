package SM.Frontend

import chisel3._
import chisel3.util._

class InstructionDecode extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val instr = Input(UInt(32.W))
    val warpId = Input(UInt(32.W))

    val opcode = Output(UInt(5.W))
    val dest = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val rs3 = Output(UInt(5.W))
    val imm = Output(UInt(32.W))

    val pcOut = Output(UInt(32.W))
    val warpIdOut = Output(UInt(32.W))
  })

  val imm = WireDefault(0.U(32.W))
  val opcode = WireDefault(0.U(5.W))

  opcode := io.instr(4, 0)

  // TODO: Extract the correct Immediate value based on instruction type

  io.dest := io.instr(9, 5)
  io.rs1 := io.instr(14, 10)
  io.rs2 := io.instr(19, 15)
  io.rs3 := io.instr(24, 20)
  io.opcode := opcode
  io.imm := imm
  io.pcOut := io.pc
  io.warpIdOut := io.warpId
}
