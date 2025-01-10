package SM

import chisel3._
import chisel3.util._

class LsuArbiter(lsuCount: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    val lsu = new Bundle {
      // Read signals
      val readAck = Output(UInt(lsuCount.W))
      val readReq = Input(UInt(lsuCount.W))
      val readData = Output(UInt((32 * lsuCount).W))
      // Write Signals
      val writeAck = Output(UInt(lsuCount.W))
      val writeReq = Input(UInt(lsuCount.W))
      val writeData = Input(UInt((32 * lsuCount).W))
      // Shared address signal
      val addr = Input(UInt((addrLen * lsuCount).W))
    }

    val dataMem = new Bundle {
      val dataR = Input(UInt(32.W))
      val we = Output(Bool())
      val dataW = Output(UInt(32.W))
      val addr = Output(UInt(addrLen.W))
    }
  })

  private val readAddrSel = false.B
  private val writeAddrSel = true.B

  // Create vectors for multiplexing the data
  val addrIn = VecInit((0 until lsuCount).map(i => io.lsu.addr(((i + 1) * addrLen) - 1, i * addrLen)))
  val writeDataArr = VecInit((0 until lsuCount).map(i => io.lsu.writeData(((i + 1) * 32) - 1, i * 32)))
  val readDataArr = VecInit(Seq.fill(lsuCount)(0.U(32.W)))

  // FSM state register
  val sIdle :: sReadArb :: sWriteArb :: sReadDone :: sWriteDone :: Nil = Enum(5)
  val stateReg = RegInit(sIdle)

  val prevReadGrant = RegInit(0.U(log2Up(lsuCount).W))
  val prevWriteGrant = RegInit(0.U(log2Up(lsuCount).W))

  // Default signal values
  val memWe = WireDefault(false.B)
  val memDataW = WireDefault(0.U(32.W))
  val memAddr = WireDefault(0.U(addrLen.W))
  val readGrant = WireDefault(0.U(log2Up(lsuCount).W))
  val writeGrant = WireDefault(0.U(log2Up(lsuCount).W))
  val addrSel = WireDefault(false.B)
  val readAck = WireDefault(0.U(lsuCount.W))
  val writeAck = WireDefault(0.U(lsuCount.W))

  switch(stateReg) {
    is(sIdle) {
      when(io.lsu.readReq.orR) {
        stateReg := sReadArb
      }.elsewhen(io.lsu.writeReq.orR) {
        stateReg := sWriteArb
      }
    }
    is(sReadArb) {
      when(io.lsu.readReq.orR) {
        addrSel := readAddrSel
        prevReadGrant := readGrant
        stateReg := sReadDone
      }.otherwise {
        stateReg := sIdle
      }
    }
    is(sWriteArb) {
      when(io.lsu.writeReq.orR) {
        memWe := true.B
        addrSel := writeAddrSel
        prevWriteGrant := writeGrant
        stateReg := sWriteDone
      }.otherwise {
        stateReg := sIdle
      }
    }
    is(sReadDone) {
      readAck := 1.U << prevReadGrant
      // Transition sReadArb when req goes low from the specific lsu
      when(io.lsu.readReq(prevReadGrant) === 0.U) {
        stateReg := sReadArb
      }
    }
    is(sWriteDone) {
      writeAck := 1.U << prevWriteGrant
      // Transition sWriteArb when req goes low from the specific lsu
      when(io.lsu.writeReq(prevWriteGrant) === 0.U) {
        stateReg := sWriteArb
      }
    }
  }

  // Grant signals for LSU requests generated from priority encoders for selecting
  readGrant := PriorityEncoder(io.lsu.readReq)
  writeGrant := PriorityEncoder(io.lsu.writeReq)

  // Multiplex the data to and from LSUs
  memAddr := addrIn(Mux(addrSel, writeGrant, readGrant))
  memDataW := writeDataArr(writeGrant)
  readDataArr(readGrant) := io.dataMem.dataR

  io.dataMem.we := memWe
  io.dataMem.dataW := memDataW
  io.dataMem.addr := memAddr

  io.lsu.readAck := readAck
  io.lsu.writeAck := writeAck
  io.lsu.readData := readDataArr.asUInt
}
