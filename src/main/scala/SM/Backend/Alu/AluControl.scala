package SM.Backend.Alu

import chisel3._
import chisel3.util._
import Constants.Opcodes
import Constants.AluOps

class AluControl extends Module {
  val io = IO(new Bundle {
    val instrOpcode = Input(UInt(5.W))
    val func3 = Input(UInt(3.W))
    val we = Output(Bool())
    val done = Output(Bool())
    val aluOp = Output(UInt(4.W))
    val aSel = Output(Bool())
    val bSel = Output(Bool())
    val nzpUpdate = Output(Bool())
    val resSel = Output(UInt(2.W))
  })

  val we = WireDefault(false.B)
  val aluOp = WireDefault(0.U(4.W))
  val aSel = WireDefault(false.B)
  val bSel = WireDefault(false.B)
  val done = WireDefault(false.B)
  val nzpUpdate = WireDefault(false.B)
  val resSel = WireDefault(0.U(2.W))

  when(io.instrOpcode === Opcodes.RET.asUInt(5.W)) {
    done := true.B
  }

  switch (io.instrOpcode) {
    is(Opcodes.ADDI.asUInt(5.W)) {
      aluOp := AluOps.ADD.asUInt(3.W)
      bSel := true.B
      we := true.B
    }
    is(Opcodes.SRLI.asUInt(5.W)) {
      aluOp := AluOps.SRL.asUInt(3.W)
      bSel := true.B
      we := true.B
    }
    is(Opcodes.SLLI.asUInt(5.W)) {
      aluOp := AluOps.SLL.asUInt(3.W)
      bSel := true.B
      we := true.B
    }
    is(Opcodes.LUI.asUInt(5.W)) {
      aluOp := AluOps.FORB.asUInt(3.W)
      bSel := true.B
      we := true.B
    }
    is(Opcodes.ADD.asUInt(5.W)) {
      aluOp := AluOps.ADD.asUInt(3.W)
      we := true.B
    }
    is(Opcodes.SUB.asUInt(5.W)) {
      aluOp := AluOps.SUB.asUInt(3.W)
      we := true.B
    }
    is(Opcodes.AND.asUInt(5.W)) {
      aluOp := AluOps.AND.asUInt(3.W)
      we := true.B
    }
    is(Opcodes.OR.asUInt(5.W)) {
      aluOp := AluOps.OR.asUInt(3.W)
      we := true.B
    }
    is(Opcodes.MUL.asUInt(5.W)) {
      aluOp := AluOps.FORA.asUInt(3.W)
      resSel := "b01".U
      we := true.B
    }
    is(Opcodes.MAD.asUInt(5.W)) {
      aluOp := AluOps.ADD.asUInt(3.W)
      resSel := "b11".U
      we := true.B
    }
    is(Opcodes.CMP.asUInt(5.W)) {
      aluOp := AluOps.SUB.asUInt(3.W)
      nzpUpdate := io.func3 =/= "b000".U && io.func3 =/= "b111".U
    }
    is(Opcodes.LDS.asUInt(5.W)) {
      aluOp := AluOps.FORA.asUInt(3.W)
      aSel := true.B
      we := true.B
    }
  }

  io.we := we
  io.done := done
  io.aluOp := aluOp
  io.aSel := aSel
  io.bSel := bSel
  io.nzpUpdate := nzpUpdate
  io.resSel := resSel
}
