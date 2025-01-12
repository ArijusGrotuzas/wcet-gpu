package SM.Frontend.IF

import Constants.Opcodes
import chisel3._

class BranchCtrlUnit(warpSize: Int) extends Module {
  val io = IO(new Bundle {
    // Inputs
    val instr = Input(UInt(32.W))
    val pcCurr = Input(UInt(32.W))
    val nzpPred = Input(UInt((3 * warpSize).W))
    // Outputs
    val jump = Output(Bool())
    val jumpAddr = Output(UInt(32.W))
  })

  val opcode = io.instr(4, 0)
  val nzpSel = io.instr(12, 10)
  val imm = (io.instr(31, 13) ## io.instr(9, 5)).asSInt
  val cond = VecInit(Seq.fill(warpSize)(false.B))

  for (i <- 0 until warpSize) {
    val threadNzpPred = io.nzpPred(((i + 1) * 3) - 1, i * 3)
    cond(i) := (nzpSel & threadNzpPred).orR
  }

  io.jump := Mux(opcode === Opcodes.BNZP.asUInt(5.W), Mux(nzpSel =/= 0.U, cond.asUInt.orR, true.B), false.B)
  io.jumpAddr := ((0.U ## io.pcCurr).asSInt + imm).asUInt
}
