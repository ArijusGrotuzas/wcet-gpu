package SM.Frontend.Old

import chisel3._

class ThreeStageOperandFetch extends Module {
  val io = IO(new Bundle{
    val reg1Addr = Input(UInt(7.W))
    val reg2Addr = Input(UInt(7.W))
    val reg3Addr = Input(UInt(7.W))

    val we = Input(Bool())
    val writeAddr = Input(UInt(7.W))
    val writeData  = Input(UInt(256.W))

    val op1 = Output(UInt(256.W))
    val op2 = Output(UInt(256.W))
    val op3 = Output(UInt(256.W))
  })
  val vrf = Module(new VectorRegisterFileOld(32, 256, 7))

  val reg1Addr = io.reg1Addr
  val reg2Addr = RegNext(io.reg2Addr)
  val reg3Addr = RegNext(RegNext(io.reg3Addr))

  vrf.io.readAddr1 := reg1Addr
  vrf.io.readAddr2 := reg2Addr
  vrf.io.readAddr3 := reg3Addr

  vrf.io.we := io.we
  vrf.io.writeAddr := io.writeAddr
  vrf.io.writeData := io.writeData
  vrf.io.writeMask := 0.U

  val op1 = RegNext(RegNext(vrf.io.readData1))
  val op2 = RegNext(vrf.io.readData2)
  val op3 = vrf.io.readData3

  io.op1 := op1
  io.op2 := op2
  io.op3 := op3
}
