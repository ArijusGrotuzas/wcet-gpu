import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils

class debug extends Bundle {
  val pc = Output(UInt(32.W))
  val warp = Output(UInt(32.W))
  val instr = Output(UInt(32.W))
  val valid = Output(Bool())
}

class SmTestTop(
                 blockCount: Int,
                 warpCount: Int,
                 warpSize: Int,
                 instrMemDepth: Int,
                 dataMemDepth: Int,
                 instructionFile: String = "",
                 dataFile: String = ""
               ) extends Module {
  val io = IO(new Bundle {
    val dbg = new debug()
    val memDump = new memoryDump(log2Up(dataMemDepth))
    val valid = Input(Bool())
    val data = Input(UInt((log2Up(blockCount) + warpCount).W))
    val ready = Output(Bool())
    val done = Output(Bool())
  })

  val smTop = Module(new SmTop(blockCount, warpCount, warpSize, instrMemDepth, dataMemDepth, instructionFile, dataFile))

  smTop.io.memDump.dumpAddr := io.memDump.dumpAddr
  io.memDump.dumpData := smTop.io.memDump.dumpData
  smTop.io.valid := io.valid
  smTop.io.data := io.data
  io.ready := smTop.io.ready
  io.done := smTop.io.done

  io.dbg.pc := DontCare
  io.dbg.warp := DontCare
  io.dbg.instr := DontCare
  io.dbg.valid := DontCare
  BoringUtils.bore(smTop.sm.frontend.instrF.pcReg, Seq(io.dbg.pc))
  BoringUtils.bore(smTop.sm.frontend.instrF.warpReg, Seq(io.dbg.warp))
  BoringUtils.bore(smTop.sm.frontend.instrF.io.instrF.instr, Seq(io.dbg.instr))
  BoringUtils.bore(smTop.sm.frontend.instrF.validReg, Seq(io.dbg.valid))
}
