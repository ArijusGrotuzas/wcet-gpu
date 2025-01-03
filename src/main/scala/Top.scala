import SM.Sm
import chisel3._

class Top(warpCount: Int, warpSize: Int, warpAddrLen: Int, instructionFile: String = "") extends Module {
  val io = IO(new Bundle {
    val reset = Input(Bool())
    val valid = Input(Bool())
    val data = Input(UInt(4.W))

    val wbOutTest = Output(UInt((warpSize * 32).W))
    val ready = Output(Bool())
  })

  // TODO: Debounce the input signals
  // TODO: Add data memory
  val sm = Module(new Sm(warpCount, warpSize, warpAddrLen))
  val instrMem = Module(new InstructionMemory(32, 1024, 32, instructionFile))

  // Load instruction inputs
  instrMem.io.we := false.B
  instrMem.io.wAddr := 0.U
  instrMem.io.dataIn := 0.U

  // Access to the instruction memory from SM
  instrMem.io.addr := sm.io.instrMem.addr
  sm.io.instrMem.data := instrMem.io.dataOut

  // Start signal to SM connection
  sm.io.start.valid := io.valid
  sm.io.start.data := io.data

  io.ready := sm.io.start.ready
  io.wbOutTest := sm.io.wbOutTest
}
