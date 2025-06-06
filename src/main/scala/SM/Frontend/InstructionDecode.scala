package SM.Frontend

import Constants.Opcodes
import chisel3._
import chisel3.util._

class InstructionDecode(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val instrF = new Bundle {
      val valid = Input(Bool())
      val threadMask = Input(UInt(warpSize.W))
      val warp = Input(UInt(warpAddrLen.W))
      val instr = Input(UInt(32.W))
    }

    val id = new Bundle {
      val valid = Output(Bool())
      val warp = Output(UInt(warpAddrLen.W))
      val threadMask = Output(UInt(warpSize.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val srs = Output(UInt(3.W))
      val imm = Output(SInt(32.W))
      val pred = Output(UInt(2.W))
    }
  })

  val imm = WireDefault(0.S(32.W))

  when(io.id.opcode === Opcodes.LUI.asUInt(5.W)) {
    imm := (io.instrF.instr(29, 10) << 12).asSInt // Load the upper 20 bits as an immediate
  }.otherwise {
    imm := io.instrF.instr(26, 15).asSInt // Load the 12 bit immediate
  }

  io.id.valid := io.instrF.valid
  io.id.warp := io.instrF.warp
  io.id.threadMask := io.instrF.threadMask
  io.id.opcode := io.instrF.instr(4, 0)
  io.id.dest := io.instrF.instr(9, 5)
  io.id.rs1 := io.instrF.instr(14, 10)
  io.id.rs2 := io.instrF.instr(19, 15)
  io.id.rs3 := io.instrF.instr(24, 20)
  io.id.srs := io.instrF.instr(12, 10)
  io.id.pred := io.instrF.instr(31, 30)
  io.id.imm := imm
}
