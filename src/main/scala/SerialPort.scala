import chisel3._
import chisel3.util._
import chisel.lib.uart._

class SerialPort(frequency: Int, baud: Int, memDepth: Int) extends Module {
  val io = IO(new Bundle {
    // Inputs
    val dump = Input(Bool())
    val dataR = Input(UInt(32.W))
    // Outputs
    val addrR = Output(UInt(32.W))
    val tx = Output(Bool())
  })

  val uart = Module(new BufferedTx(frequency, baud))

  val sIdle :: sRead :: sStore :: sDump :: Nil = Enum(4)
  val stateReg = RegInit(sIdle)

  val memData = RegInit(VecInit(Seq.fill(8)(0.U(8.W))))
  val memAddrReg = RegInit(0.U(32.W))
  val sendCountReg = RegInit(0.U(8.W))
  val valid = WireDefault(false.B)
  val lastChar = sendCountReg === 4.U
  val dumpData = WireDefault(0.U(8.W))

  switch(stateReg) {
    is(sIdle) {
      when(io.dump) {
        stateReg := sRead
      }
    }
    is(sRead) {
      when(memAddrReg =/= memDepth.U) {
        stateReg := sStore
        memAddrReg := memAddrReg + 1.U
      } .otherwise {
        stateReg := sIdle
      }
    }
    is(sStore) {
      memData(3) := io.dataR(7, 0)
      memData(2) := io.dataR(15, 8)
      memData(1) := io.dataR(23, 16)
      memData(0) := io.dataR(31, 24)
      stateReg := sDump
    }
    is(sDump) {
      when(!lastChar) {
        sendCountReg := Mux(uart.io.channel.ready, sendCountReg + 1.U, sendCountReg)
        dumpData := memData(sendCountReg)
        valid := true.B
      } .otherwise {
        sendCountReg := 0.U
        stateReg := sRead
      }
    }
  }

  uart.io.channel.bits := dumpData
  uart.io.channel.valid := valid

  io.addrR := memAddrReg
  io.tx := uart.io.txd
}
