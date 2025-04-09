package SM.Frontend.IF

import chisel3.util._
import Constants.Opcodes
import chisel3._

class BranchCtrlUnit(warpSize: Int) extends Module {
  val io = IO(new Bundle {
    // Inputs
    val pc = Input(UInt(32.W))
    val instr = Input(UInt(32.W))
    val pred = Input(UInt(warpSize.W))
    // Outputs
    val prepare = Output(Bool())
    val prepareAddr = Output(UInt(32.W))
    val split = Output(Bool())
    val splitAddr = Output(UInt(32.W))
    val splitMask = Output(UInt(warpSize.W))
    val join = Output(Bool())
    val jump = Output(Bool())
    val jumpAddr = Output(UInt(32.W))
  })

  val prepare = WireDefault(false.B)
  val prepareAddr = WireDefault(0.U(32.W))
  val split = WireDefault(false.B)
  val splitAddr = WireDefault(0.U(32.W))
  val splitMask = WireDefault(0.U(warpSize.W))
  val join = WireDefault(false.B)
  val jump = WireDefault(false.B)
  val jumpAddr = WireDefault(0.U(32.W))

  val opcode = io.instr(4, 0)
  val brnOffset = io.instr(29, 5).asSInt

  switch(opcode) {
    is(Opcodes.BR.asUInt(5.W)) {
      jump := io.pred.orR
      jumpAddr := ((0.U ## io.pc).asSInt + brnOffset).asUInt
    }
    is(Opcodes.PREPARE.asUInt(5.W)) {
      prepare := true.B
      prepareAddr := ((0.U ## io.pc).asSInt + brnOffset).asUInt
    }
    is(Opcodes.SPLIT.asUInt(5.W)) {
      // TODO: Add cases for when all threads agree on the same path
      //  if all are true do nothing
      //  if all are false jump to the branch offset address
      //  either way do not push entries to the stack
      split := true.B
      splitAddr := ((0.U ## io.pc).asSInt + brnOffset).asUInt
      splitMask := io.pred
    }
    is(Opcodes.JOIN.asUInt(5.W)) {
      join := true.B
    }
  }

  io.prepare := prepare
  io.prepareAddr := prepareAddr
  io.split := split
  io.splitAddr := splitAddr
  io.splitMask := splitMask
  io.join := join
  io.jump := jump
  io.jumpAddr := jumpAddr
}
