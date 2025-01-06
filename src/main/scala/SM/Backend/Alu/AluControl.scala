package SM.Backend.Alu

import SM.Opcodes
import chisel3._
import chisel3.util._

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

  when(io.instrOpcode === Opcodes.RET) {
    done := true.B
  }

  switch (io.instrOpcode) {
    is(Opcodes.ADDI) {
      aluOp := AluOps.ADD
      bSel := "b10".U
      we := true.B
    }
    is(Opcodes.LUI) {
      aluOp := AluOps.FORB
      bSel := "b10".U
      we := true.B
    }
    is(Opcodes.ADD) {
      aluOp := AluOps.ADD
      we := true.B
    }
    is(Opcodes.SUB) {
      aluOp := AluOps.SUB
      we := true.B
    }
    is(Opcodes.AND) {
      aluOp := AluOps.AND
      we := true.B
    }
    is(Opcodes.OR) {
      aluOp := AluOps.OR
      we := true.B
    }
    is(Opcodes.MUL) {
      aluOp := AluOps.FORA
      aSel := "b10".U
      we := true.B
    }
    is(Opcodes.MAD) {
      aluOp := AluOps.ADD
      aSel := "b10".U
      bSel := "b01".U
      we := true.B
    }
    is(Opcodes.CMP) {
      aluOp := AluOps.SUB
      nzpUpdate := true.B
    }
    is(Opcodes.LDS) {
      aluOp := AluOps.FORA
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
