import SM.Sm
import chisel3._

class TopFpga extends Module {
  val io = new Bundle {
    val valid = Input(Bool())
    val data = Input(UInt(4.W))
    val ready = Output(Bool())
  }

  // TODO: Debounce the input signals
  // TODO: Add data memory
  val sm = Module(new Sm(4, 8, 2))
  val instrMem = Module(new InstructionMemory(32, 1024, 32))

  instrMem.io.addr := sm.io.instrMem.addr
  instrMem.io.we := false.B
  instrMem.io.dataIn := 0.U

  sm.io.instrMem.data := instrMem.io.dataOut
  sm.io.start.valid := io.valid
  sm.io.start.data := io.data

  io.ready := sm.io.start.ready
}
