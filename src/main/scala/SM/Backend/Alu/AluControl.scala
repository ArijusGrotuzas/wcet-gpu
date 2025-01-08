package SM.Backend.Alu

import chisel3._
import chisel3.util._
import Constants.Opcodes
import Constants.AluOps

class AluControl extends Module {
  val io = IO(new Bundle {
    val instrOpcode = Input(UInt(5.W))

    val we = Output(Bool())
    val done = Output(Bool())
    val aluOp = Output(UInt(4.W))
    val aSel = Output(UInt(2.W))
    val bSel = Output(UInt(2.W))
    val nzpUpdate = Output(Bool())
  })

  val we = WireDefault(false.B)
  val aluOp = WireDefault(0.U(4.W))
  val aSel = WireDefault(0.U(2.W))
  val bSel = WireDefault(0.U(2.W))
  val done = WireDefault(false.B)
  val nzpUpdate = WireDefault(false.B)

  when(io.instrOpcode === Opcodes.RET.asUInt(5.W)) {
    done := true.B
  }

  switch (io.instrOpcode) {
    is(Opcodes.ADDI.asUInt(5.W)) {
      aluOp := AluOps.ADD.asUInt(3.W)
      bSel := "b10".U
      we := true.B
    }
    is(Opcodes.LUI.asUInt(5.W)) {
      aluOp := AluOps.FORB.asUInt(3.W)
      bSel := "b10".U
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
      aSel := "b10".U
      we := true.B
    }
    is(Opcodes.MAD.asUInt(5.W)) {
      aluOp := AluOps.ADD.asUInt(3.W)
      aSel := "b10".U
      bSel := "b01".U
      we := true.B
    }
    is(Opcodes.CMP.asUInt(5.W)) {
      aluOp := AluOps.SUB.asUInt(3.W)
      nzpUpdate := true.B
    }
    is(Opcodes.LDS.asUInt(5.W)) {
      aluOp := AluOps.FORA.asUInt(3.W)
      aSel := "b01".U
      we := true.B
    }
  }

  io.we := we
  io.done := done
  io.aluOp := aluOp
  io.aSel := aSel
  io.bSel := bSel
  io.nzpUpdate := nzpUpdate
}
