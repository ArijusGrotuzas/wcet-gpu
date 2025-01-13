package SM.Frontend.IF

import Constants.Opcodes
import chisel3._

class BranchCtrlUnit(warpSize: Int) extends Module {
  val io = IO(new Bundle {
    // Inputs
    val instr = Input(UInt(32.W))
    val pcCurr = Input(UInt(32.W))
    val nzpPred = Input(UInt(warpSize.W))
    // Outputs
    val jump = Output(Bool())
    val jumpAddr = Output(UInt(32.W))
  })

  val opcode = io.instr(4, 0)
  val imm = io.instr(29, 5).asSInt

  io.jump := Mux(opcode === Opcodes.BR.asUInt(5.W), io.nzpPred.orR, false.B)
  io.jumpAddr := ((0.U ## io.pcCurr).asSInt + imm).asUInt
}
