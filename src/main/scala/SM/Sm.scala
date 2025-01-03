package SM

import SM.Backend.Back
import SM.Frontend.Front
import chisel3._

class Sm(warpCount: Int, warpSize: Int) extends Module {
  val io = IO(new Bundle {
    val instrMem = new Bundle {
      val addr = Output(UInt(32.W))
      val data = Input(UInt(32.W))
    }

    val start = new Bundle {
      val valid = Input(Bool())
      val data = Input(UInt(warpCount.W))
      val ready = Output(Bool())
    }

    val wbOutTest = Output(UInt((warpSize * 32).W))
  })

  val frontend = Module(new Front(warpCount))
  val backend = Module(new Back(warpCount, warpSize))

  frontend.io.instrMem <> io.instrMem
  frontend.io.start <> io.start
  frontend.io.front <> backend.io.front
  frontend.io.wb <> backend.io.wb
  frontend.io.funcUnits <> backend.io.funcUnits
  frontend.io.nzpUpdate <> backend.io.nzpUpdate

  io.wbOutTest := backend.io.wbOutTest
}
