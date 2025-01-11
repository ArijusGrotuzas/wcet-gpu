package SM.Frontend

import Constants.Opcodes
import chisel3._

class BranchCtrlUnit extends Module {
  val io = IO(new Bundle {
    // Inputs
    val instr = Input(UInt(32.W))
    val pcCurr = Input(UInt(32.W))
    val nzpPred = Input(UInt(3.W))
    // Outputs
    val jump = Output(Bool())
    val jumpAddr = Output(UInt(32.W))
  })

  val opcode = io.instr(4, 0)
  val nzpSel = io.instr(12, 10)
  val imm = (io.instr(31, 13) ## io.instr(9, 5)).asSInt

  io.jump := Mux(opcode === Opcodes.BNZP.asUInt(5.W), Mux(nzpSel =/= 0.U, (nzpSel & io.nzpPred).orR, true.B), false.B)
  io.jumpAddr := ((0.U ## io.pcCurr).asSInt + imm).asUInt
}
