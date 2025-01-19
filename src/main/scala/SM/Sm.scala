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
    val done = Output(Bool())
  })

  val frontend = Module(new Front(blockCount, warpCount, warpSize))
  val backend = Module(new Back(blockCount, warpCount, warpSize))
  val lsuArbiter = Module(new LsuArbiter(warpSize, 32))
  val predicateRegister = Module(new PredicateRegisterFile(warpCount, warpSize))

  // Access to the predicate register file
  predicateRegister.io.we := backend.io.predUpdateCtrl.en
  predicateRegister.io.dataW := backend.io.predUpdateCtrl.pred
  predicateRegister.io.addrW := backend.io.predUpdateCtrl.addr
  // First read port dedicated to instruction fetch stage
  predicateRegister.io.addr1R := frontend.io.ifPredReg.addrR
  frontend.io.ifPredReg.dataR := predicateRegister.io.data1R
  // Second read port dedicated to operand fetch stage
  predicateRegister.io.addr2R := backend.io.ofPredReg.addrR
  backend.io.ofPredReg.dataR := predicateRegister.io.data2R

  // Control for starting the SM
  frontend.io.start <> io.start

  // Access to instruction memory
  frontend.io.instrMem <> io.instrMem

  // Connect frontend output to backend
  frontend.io.front <> backend.io.front

  // Control signal connections between frontend and backend
  frontend.io.wbIfCtrl <> backend.io.wbIfCtrl
  frontend.io.memIfCtrl <> backend.io.memIfCtrl
  frontend.io.aluInitCtrl <> backend.io.aluInitCtrl
  frontend.io.memStall := backend.io.memStall
  frontend.io.wbStall := backend.io.wbStall

  // Connection to data memory
  lsuArbiter.io.lsu <> backend.io.lsu
  io.dataMem <> lsuArbiter.io.dataMem

  // Test signal
  io.wbOutTest := backend.io.wbOutTest
  io.done := frontend.io.done
}
