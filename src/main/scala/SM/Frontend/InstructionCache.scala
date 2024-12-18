package SM.Frontend

import chisel3._
import chisel3.util._

class InstructionCache(width: Int, depth: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    val wEn = Input(Bool())
    val addr = Input(UInt(addrLen.W))
    val loadInstr = Input(UInt(width.W))

    val instr = Output(UInt(width.W))
  })

  val mem = SyncReadMem(depth, UInt(width.W))
  val instr = WireDefault(0.U(width.W))
  instr := DontCare
  val rdwrPort = mem(io.addr)

  when (io.wEn) {
    rdwrPort := io.loadInstr
  } .otherwise{
    instr := rdwrPort
  }

  io.instr := instr
}
