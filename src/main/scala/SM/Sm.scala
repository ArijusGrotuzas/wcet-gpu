package SM

import chisel3._
import chisel3.util._
import SM.Backend.Back
import SM.Frontend.Front

class Sm(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  private val blockAddrLen = log2Up(blockCount)
  val io = IO(new Bundle {
    val instrMem = new Bundle {
      val addr = Output(UInt(32.W))
      val data = Input(UInt(32.W))
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

  frontend.io.instrMem <> io.instrMem
  frontend.io.start <> io.start
  frontend.io.front <> backend.io.front
  frontend.io.wb <> backend.io.wb
  frontend.io.funcUnits <> backend.io.funcUnits
  frontend.io.nzpUpdate <> backend.io.nzpUpdate
  frontend.io.aluInitCtrl <> backend.io.aluInitCtrl

  io.wbOutTest := backend.io.wbOutTest
}
