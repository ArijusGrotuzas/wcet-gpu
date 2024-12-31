package SM.Backend

import chisel3._
import chisel3.util._
import SM.Opcodes

class AluControl extends Module {
  val io = IO(new Bundle {
    val instrOpcode = Input(UInt(5.W))

    val aluOp = Output(UInt(4.W))
    val rs2Sel = Output(Bool())
    val done = Output(Bool())
    val valid = Output(Bool())
  })

  val aluOp = WireDefault(0.U(4.W))
  val rs2Sel = WireDefault(false.B)
  val done = WireDefault(false.B)
  val valid = WireDefault(false.B)

  when(io.instrOpcode === Opcodes.RET) {
    done := true.B
  }

  when(io.instrOpcode =/= Opcodes.NOP) {
    valid := true.B
  }

  switch (io.instrOpcode) {
    is(Opcodes.ADDI) {
      aluOp := AluOps.ADD
      rs2Sel := true.B
    }
    is(Opcodes.LUI) {
      aluOp := AluOps.FORB
      rs2Sel := true.B
    }
    is(Opcodes.ADD) {
      aluOp := AluOps.ADD
    }
    is(Opcodes.SUB) {
      aluOp := AluOps.SUB
    }
    is(Opcodes.AND) {
      aluOp := AluOps.AND
    }
    is(Opcodes.OR) {
      aluOp := AluOps.OR
    }
  }

  io.aluOp := aluOp
  io.rs2Sel := rs2Sel
  io.done := done
  io.valid := valid
}
