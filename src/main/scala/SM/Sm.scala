package SM

import SM.Backend.Backend
import SM.Frontend.Frontend
import chisel3._

class Sm(warpCount: Int, warpSize: Int, warpAddrLen: Int) extends Module {
  val io = IO(new Bundle {
    val loadInstr = new Bundle {
      val en = Input(Bool())
      val instr = Input(UInt(32.W))
      val addr = Input(UInt(32.W))
    }

    val start = new Bundle {
      val ready = Output(Bool())
      val valid = Input(Bool())
      val data = Input(UInt(warpCount.W))
    }
  })

  val frontend = Module(new Frontend(warpCount, warpAddrLen))
  val backend = Module(new Backend(warpCount, warpSize, warpAddrLen))

  frontend.io.loadInstr <> io.loadInstr
  frontend.io.start <> io.start
  frontend.io.front <> backend.io.front
  frontend.io.wb <> backend.io.wb
  frontend.io.funcUnits <> backend.io.funcUnits
}
