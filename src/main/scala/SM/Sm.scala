package SM

import SM.Backend.Back
import SM.Frontend.Front
import chisel3._
import chisel3.util._

class Sm(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  private val blockAddrLen = log2Up(blockCount)
  val io = IO(new Bundle {
    val instrMem = new Bundle {
      val addr = Output(UInt(32.W))
      val data = Input(UInt(32.W))
    }

    val dataMem = new Bundle {
      val dataR = Input(UInt(32.W))
      val we = Output(Bool())
      val dataW = Output(UInt(32.W))
      val addr = Output(UInt(32.W))
    }

    val start = new Bundle {
      val valid = Input(Bool())
      val data = Input(UInt((blockAddrLen + warpCount).W))
      val ready = Output(Bool())
    }

    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  val frontend = Module(new Front(blockCount, warpCount))
  val backend = Module(new Back(blockCount, warpCount, warpSize))
  val lsuArbiter = Module(new LsuArbiter(warpSize, 32))

  // Control for starting the SM
  frontend.io.start <> io.start

  // Access to instruction memory
  frontend.io.instrMem <> io.instrMem

  // Connect frontend output to backend
  frontend.io.front <> backend.io.front

  // Control signal connections between frontend and backend
  frontend.io.wbIfCtrl <> backend.io.wbIfCtrl
  frontend.io.memIfCtrl <> backend.io.memIfCtrl
  frontend.io.nzpUpdate <> backend.io.nzpUpdate
  frontend.io.aluInitCtrl <> backend.io.aluInitCtrl
  frontend.io.memStall := backend.io.memStall

  // Connection to data memory
  lsuArbiter.io.lsu <> backend.io.lsu
  io.dataMem <> lsuArbiter.io.dataMem

  // Test signal
  io.wbOutTest := backend.io.wbOutTest
}
