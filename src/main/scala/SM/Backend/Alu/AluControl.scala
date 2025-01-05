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
    val rs1Sel = Output(Bool())
    val rs2Sel = Output(Bool())
    val nzpUpdate = Output(Bool())
  })

  val we = WireDefault(false.B)
  val aluOp = WireDefault(0.U(4.W))
  val rs1Sel = WireDefault(false.B)
  val rs2Sel = WireDefault(false.B)
  val done = WireDefault(false.B)
  val nzpUpdate = WireDefault(false.B)

  when(io.instrOpcode === Opcodes.RET) {
    done := true.B
  }

  switch (io.instrOpcode) {
    is(Opcodes.ADDI) {
      aluOp := AluOps.ADD
      rs2Sel := true.B
      we := true.B
    }
    is(Opcodes.LUI) {
      aluOp := AluOps.FORB
      rs2Sel := true.B
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
    is(Opcodes.CMP) {
      aluOp := AluOps.SUB
      nzpUpdate := true.B
    }
    is(Opcodes.LS) {
      aluOp := AluOps.FORA
      rs1Sel := true.B
      we := true.B
    }
  }

  io.we := we
  io.done := done
  io.aluOp := aluOp
  io.rs2Sel := rs2Sel
  io.rs1Sel := rs1Sel
  io.nzpUpdate := nzpUpdate
}
