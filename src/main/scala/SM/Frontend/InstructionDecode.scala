package SM.Frontend

import chisel3._
import chisel3.util._

class InstructionDecode(warpAddrLen: Int) extends Module {
  val io = IO(new Bundle {
    val instrF = new Bundle() {
      val valid = Input(Bool())
      val pc = Input(UInt(32.W))
      val instr = Input(UInt(32.W))
      val warp = Input(UInt(warpAddrLen.W))
    }

    val id = new Bundle {
      val valid = Output(Bool())
      val pc = Output(UInt(32.W))
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val imm = Output(UInt(22.W))
    }
  })

  val imm = WireDefault(0.U(22.W))

  // TODO: Extract the correct Immediate value based on instruction type

  io.id.valid := io.instrF.valid
  io.id.pc := io.instrF.pc
  io.id.warp := io.instrF.warp
  io.id.dest := io.instrF.instr(9, 5)
  io.id.rs1 := io.instrF.instr(14, 10)
  io.id.rs2 := io.instrF.instr(19, 15)
  io.id.rs3 := io.instrF.instr(24, 20)
  io.id.opcode := io.instrF.instr(4, 0)
  io.id.imm := imm
}
