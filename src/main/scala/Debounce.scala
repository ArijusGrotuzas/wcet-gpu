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

  def tickGen(fac: Int): Bool = {
    val cntReg = RegInit(0.U(log2Up(fac).W))
    val tick = cntReg === (fac - 1).U
    cntReg := Mux(tick, 0.U, cntReg + 1.U)
    tick
  }

  def majorityFilter(din: Bool, tick: Bool): Bool = {
    val reg = RegInit(0.U(3.W))
    when(tick) {
      reg := reg(1, 0) ## din
    }
    (reg(2) & reg(1)) | (reg(2) & reg(0)) | (reg(1) & reg(0))
  }

  // Synchronized inputs
  val syncReset = syncIn(io.reset)
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
    debResetReg := syncReset
    debValidReg := syncValid
    debDataReg := syncData
  }

  io.resetDb := majorityFilter(debResetReg, tick)
  io.validDb := majorityFilter(debValidReg, tick)
  io.dataDb := debDataReg
}
