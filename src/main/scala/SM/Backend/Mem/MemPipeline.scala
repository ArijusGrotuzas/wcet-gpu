package SM.Backend.Mem

import chisel3._
import chisel3.util._

class MemPipeline(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val of = new Bundle {
      val valid = Input(Bool())
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt((32 * warpSize).W))
      val rs2 = Input(UInt((32 * warpSize).W))
    }

    val mem = new Bundle {
      val we = Output(Bool())
      val warp = Output(UInt(warpAddrLen.W))
      val dest = Output(UInt(5.W))
      val out = Output(UInt((32 * warpSize).W))
    }

    val dataMem = new Bundle {
      // Read signals
      val readAck = Input(UInt(warpSize.W))
      val readReq = Output(UInt(warpSize.W))
      val readData = Input(UInt((32 * warpSize).W))
      // Write Signals
      val writeAck = Input(UInt(warpSize.W))
      val writeReq = Output(UInt(warpSize.W))
      val writeData = Output(UInt((32 * warpSize).W))
      // Shared address signal
      val addr = Output(UInt((32 * warpSize).W))
    }

    val memStall = Output(Bool())
  })

  val memCtrl = Module(new MemControl())
  memCtrl.io.valid := io.of.valid
  memCtrl.io.opcode := io.of.opcode

  val allLsuDone = WireDefault(false.B)
  val lsuAcks = VecInit(Seq.fill(warpSize)(false.B))
  val lsuOut = VecInit(Seq.fill(warpSize)(0.U(32.W)))
  val lsuMemAddr = VecInit(Seq.fill(warpSize)(0.U(32.W)))
  val lsuMemWriteData = VecInit(Seq.fill(warpSize)(0.U(32.W)))
  val lsuMemReadReq = VecInit(Seq.fill(warpSize)(false.B))
  val lsuMemWriteReq = VecInit(Seq.fill(warpSize)(false.B))

  for(i <- 0 until warpSize) {
    val lsu = Module(new Lsu(1024, 32))

    val rs1 = io.of.rs1(((i + 1) * 32) - 1, i * 32)
    val rs2 = io.of.rs2(((i + 1) * 32) - 1, i * 32)
    val lsuMemReadData = io.dataMem.readData(((i + 1) * 32) - 1, i * 32)

    lsu.io.request := memCtrl.io.request
    lsu.io.dataIn.memReadEn := memCtrl.io.memReadEn
    lsu.io.dataIn.memWriteEn := memCtrl.io.memWriteEn
    lsu.io.dataIn.rs1 := rs1
    lsu.io.dataIn.rs2 := rs2
    lsu.io.mem.readAck := io.dataMem.readAck(i)
    lsu.io.mem.writeAck := io.dataMem.writeAck(i)
    lsu.io.mem.readData := lsuMemReadData

    lsuOut(i) := lsu.io.dataOut
    lsuAcks(i) := lsu.io.acknowledge
    lsuMemAddr(i) := lsu.io.mem.addr
    lsuMemWriteData(i) := lsu.io.mem.writeData
    lsuMemReadReq(i) := lsu.io.mem.readReq
    lsuMemWriteReq(i) := lsu.io.mem.writeReq
  }

  // Check if all the LSUs are done
  allLsuDone := lsuAcks.asUInt.orR
  memCtrl.io.allLsuDone := allLsuDone

  io.mem.warp := io.of.warp
  io.mem.we := (allLsuDone && memCtrl.io.memReadEn)
  io.mem.dest := io.of.dest
  io.mem.out := lsuOut.asUInt
  io.memStall := memCtrl.io.memStall

  io.dataMem.readReq := lsuMemReadReq.asUInt
  io.dataMem.writeReq := lsuMemWriteReq.asUInt
  io.dataMem.writeData := lsuMemWriteData.asUInt
  io.dataMem.addr := lsuMemAddr.asUInt
}