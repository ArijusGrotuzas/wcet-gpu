package SM.Backend.Mem

import SM.Opcodes
import chisel3._
import chisel3.util._

class MemControl extends Module {
  val io = IO(new Bundle {
    val valid = Input(Bool())
    val opcode = Input(UInt(5.W))
    val allLsuDone = Input(Bool())

    val memReadEn = Output(Bool())
    val memWriteEn = Output(Bool())
    val request = Output(Bool())
    val memStall = Output(Bool())
  })

  val sIdle :: sStore :: sLoad :: Nil = Enum(3)
  val stateReg = RegInit(sIdle)

  val memReadEn = WireDefault(false.B)
  val memWriteEn = WireDefault(false.B)
  val request = WireDefault(false.B)
  val memStall = WireDefault(false.B)

  // FSM
  switch(stateReg) {
    is(sIdle) {
      when(io.valid) {
        memStall := true.B
        when(io.opcode === Opcodes.LD) {
          stateReg := sLoad
        }.elsewhen(io.opcode === Opcodes.ST) {
          stateReg := sStore
        }
      }
    }
    is(sStore) {
      memWriteEn := true.B
      request := true.B
      memStall := true.B
      when(io.allLsuDone) {
        stateReg := sIdle
      }
    }
    is(sLoad) {
      memReadEn := true.B
      request := true.B
      memStall := true.B
      when(io.allLsuDone) {
        stateReg := sIdle
      }
    }
  }

  io.memReadEn := memReadEn
  io.memWriteEn := memWriteEn
  io.request := request
  io.memStall := memStall
}
