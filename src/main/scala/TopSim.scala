import SM.Sm
import chisel3._

class TopSim(warpCount: Int, warpSize: Int, warpAddrLen: Int) extends Module {
  val io = IO(new Bundle {
    val valid = Input(Bool())
    val data = Input(UInt(4.W))
    val loadInstr = Input(UInt(32.W))
    val loadInstrEn = Input(Bool())
    val loadInstrAddr = Input(UInt(32.W))

    val wbOutTest = Output(UInt((warpSize * 32).W))
    val ready = Output(Bool())
  })

  // TODO: Add data memory
  val sm = Module(new Sm(warpCount, warpSize, warpAddrLen))
  val instrMem = Module(new InstructionMemory(32, 1024, 32))

  // Load instruction inputs
  instrMem.io.we := io.loadInstrEn
  instrMem.io.wAddr := io.loadInstrAddr
  instrMem.io.dataIn := io.loadInstr

  // Access to the instruction memory from SM
  instrMem.io.addr := sm.io.instrMem.addr
  sm.io.instrMem.data := instrMem.io.dataOut

  // Start signal to SM connection
  sm.io.start.valid := io.valid
  sm.io.start.data := io.data

  io.ready := sm.io.start.ready
  io.wbOutTest := sm.io.wbOutTest
}
