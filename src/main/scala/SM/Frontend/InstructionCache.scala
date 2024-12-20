package SM.Frontend

import chisel3._
import chisel3.util._

// NOTE: Mem() create asynchronous read memory, which usually will not be synthesized into a memory, but rather will be
// built out of flip-flops instead
class InstructionCache(width: Int, depth: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    val loadInstr = Input(Bool())
    val instrAddr = Input(UInt(addrLen.W))
    val loadInstrData = Input(UInt(width.W))

    val instr = Output(UInt(width.W))
  })

  val instr = WireDefault(0.U(width.W))
  val mem = SyncReadMem(depth, UInt(width.W))
  val rdwrPort = mem(io.instrAddr)
  instr := 0.U

  when (io.loadInstr) {
    rdwrPort := io.loadInstrData
  } .otherwise{
    instr := rdwrPort
  }

  io.instr := instr
}
