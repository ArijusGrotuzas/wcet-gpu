import chisel3._
import chisel3.util._

class Debounce(dataSize: Int, freq: Int) extends Module {
  val io = IO(new Bundle {
    val reset = Input(Bool())
    val valid = Input(Bool())
    val data = Input(UInt(dataSize.W))

    val resetDb = Output(Bool())
    val validDb = Output(Bool())
    val dataDb = Output(UInt(dataSize.W))
  })

  def syncIn(din: UInt): UInt = RegNext(RegNext(din))

  def tickGen(max: Int): Bool = {
    val reg = RegInit(0.U(log2Up(max).W))
    val tick = reg === (max - 1).U
    reg := Mux(tick, 0.U, reg + 1.U)
    tick
  }

  // Synchronized inputs
  val syncRest = syncIn(io.reset)
  val syncValid = syncIn(io.valid)
  val syncData = syncIn(io.data)

  // Debounced inputs
  val debResetReg = RegInit(false.B)
  val debValidReg = RegInit(false.B)
  val debDataReg = RegInit(0.U(dataSize.W))

  // Tick generation
  val tick = tickGen(freq / 100)

  // Debounce Inputs
  when(tick) {
    debResetReg := syncRest
    debValidReg := syncValid
    debDataReg := syncData
  }

  io.resetDb := debResetReg
  io.validDb := debValidReg
  io.dataDb := debDataReg
}
