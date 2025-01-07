package SM.Backend.Mem

import chisel3._
import chisel3.util._

class Lsu(memDepth: Int, dataWidth: Int) extends Module {
  val memAddrLen = log2Up(memDepth)
  val io = IO(new Bundle {
    val request = Input(Bool())

    val dataIn = new Bundle {
      val memReadEn = Input(Bool())
      val memWriteEn = Input(Bool())
      val rs2 = Input(UInt(dataWidth.W))
      val rs1 = Input(UInt(dataWidth.W))
    }

    val mem = new Bundle {
      // Read signals
      val readAck = Input(Bool())
      val readReq = Output(Bool())
      val readData = Input(UInt(dataWidth.W))
      // Write signals
      val writeAck = Input(Bool())
      val writeReq = Output(Bool())
      val writeData = Output(UInt(dataWidth.W))
      // Shared address signal
      val addr = Output(UInt(memAddrLen.W))
    }

    val acknowledge = Output(Bool())
    val dataOut = Output(UInt(dataWidth.W))
  })

  val sIdle :: sWrite :: sRead :: sDone :: Nil = Enum(4)
  val stateReg = RegInit(sIdle)

  val acknowledge = WireDefault(false.B)
  val readReq = WireDefault(false.B)
  val writeReq = WireDefault(false.B)
  val writeData = WireDefault(0.U(dataWidth.W))
  val memAddr = WireDefault(0.U(memAddrLen.W))
  val dataOut = RegInit(0.U(dataWidth.W))

  // TODO: Share addr and data
  // FSM
  switch(stateReg) {
    is(sIdle) {
      when(io.request) {
        when(io.dataIn.memReadEn) {
          stateReg := sRead
        }.elsewhen(io.dataIn.memWriteEn) {
          stateReg := sWrite
        }
      }
    }
    is(sRead) {
      readReq := true.B
      memAddr := io.dataIn.rs1
      when(io.mem.readAck) {
        dataOut := io.mem.readData
        stateReg := sDone
      }
    }
    is(sWrite) {
      writeReq := true.B
      memAddr := io.dataIn.rs1
      writeData := io.dataIn.rs2
      when(io.mem.writeAck) {
        dataOut := 0.U
        stateReg := sDone
      }
    }
    is(sDone) {
      acknowledge := true.B
      when(!io.request) {
        stateReg := sIdle
      }
    }
  }

  io.acknowledge := acknowledge
  io.dataOut := dataOut

  io.mem.readReq := readReq
  io.mem.writeReq := writeReq
  io.mem.writeData := writeData
  io.mem.addr := memAddr
}
