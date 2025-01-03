import SM.Sm
import chisel3._

class SmTop(warpCount: Int, warpSize: Int, freq: Int, instructionFile: String = "") extends Module {
  val io = IO(new Bundle {
    val reset = Input(Bool())
    val valid = Input(Bool())
    val data = Input(UInt(warpCount.W))

//    val wbOutTest = Output(UInt((warpSize * 32).W))
    val ready = Output(Bool())
  })

  // TODO: Add data memory
  val debounce = Module(new Debounce(warpCount, freq))
  val sm = Module(new Sm(warpCount, warpSize))
  val instrMem = Module(new InstructionMemory(32, 1024, 32, instructionFile))

  debounce.io.reset := io.reset
  debounce.io.valid := io.valid
  debounce.io.data := io.data

  // Load instruction inputs
  instrMem.io.we := false.B
  instrMem.io.wAddr := 0.U
  instrMem.io.dataIn := 0.U

  // Access to the instruction memory from SM
  instrMem.io.addr := sm.io.instrMem.addr
  sm.io.instrMem.data := instrMem.io.dataOut

  // Start signal to SM connection
  sm.io.start.valid := debounce.io.validDb
  sm.io.start.data := debounce.io.dataDb

  io.ready := sm.io.start.ready
//  io.wbOutTest := sm.io.wbOutTest
}

object SmTop extends App {
  println("Generating the SM hardware")
  (new chisel3.stage.ChiselStage).emitVerilog(new SmTop(4, 8, 100000000, "bootkernel.hex"), Array("--target-dir", "generated"))
}
