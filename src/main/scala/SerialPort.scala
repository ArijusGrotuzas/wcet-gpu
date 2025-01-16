import chisel.lib.uart._
import chisel3._
import chisel3.util._

class SerialPort(frequency: Int, baud: Int, memDepth: Int) extends Module {
  val addrLen = log2Up(memDepth)
  val io = IO(new Bundle {
    // Inputs
    val dump = Input(Bool())
    val dataR = Input(UInt(32.W))
    // Outputs
    val addrR = Output(UInt(addrLen.W))
    val tx = Output(Bool())
  })

  // Create the UART module
  val uart = Module(new BufferedTx(frequency, baud))

  val sIdle :: sRead :: sStore :: sSend :: sDone :: Nil = Enum(5)
  val stateReg = RegInit(sIdle)

  val memData = RegInit(VecInit(Seq.fill(4)(0.U(8.W))))
  val memAddrReg = RegInit(0.U(addrLen.W))
  val sendCountReg = RegInit(0.U(32.W))

  val lastChar = sendCountReg === 4.U
  val send = WireDefault(false.B)
  val valid = WireDefault(false.B)

  switch(stateReg) {
    is(sIdle) {
      when(io.dump) {
        stateReg := sRead
      }
    }
    is(sRead) {
      when(memAddrReg =/= (memDepth - 1).U) {
        stateReg := sStore
        memAddrReg := memAddrReg + 1.U
      }.otherwise {
        stateReg := sDone
        memAddrReg := 0.U
      }
    }
    is(sStore) {
      memData(3) := io.dataR(7, 0)
      memData(2) := io.dataR(15, 8)
      memData(1) := io.dataR(23, 16)
      memData(0) := io.dataR(31, 24)
      stateReg := sSend
    }
    is(sSend) {
      when(!lastChar) {
        send := true.B
      }.otherwise {
        stateReg := sRead
      }
    }
    is(sDone) {
      when(!io.dump) {
        stateReg := sIdle
      }
    }
  }

  // Begin sending of characters
  when(send) {
    // Wait for ready signal before sending the next character
    sendCountReg := Mux(uart.io.channel.ready, sendCountReg + 1.U, sendCountReg)
    valid := true.B
  }.otherwise {
    sendCountReg := 0.U
    valid := false.B
  }

  val sendBits = memData(sendCountReg)
  uart.io.channel.bits := sendBits
  uart.io.channel.valid := valid

  io.addrR := memAddrReg
  io.tx := uart.io.txd
}
