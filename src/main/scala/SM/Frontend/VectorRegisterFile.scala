package SM.Frontend

import chisel3._
import chisel3.util._

class VectorRegisterFile(bankDepth: Int, bankWidth: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    // Inputs
    val we = Input(Bool())
    val writeAddr = Input(UInt(addrLen.W))
    val writeMask = Input(UInt(4.W))
    val writeData = Input(UInt(bankWidth.W))
    val readAddr1 = Input(UInt(addrLen.W))
    val readAddr2 = Input(UInt(addrLen.W))
    val readAddr3 = Input(UInt(addrLen.W))
    // Outputs
    val readData1 = Output(UInt(bankWidth.W))
    val readData2 = Output(UInt(bankWidth.W))
    val readData3 = Output(UInt(bankWidth.W))
  })

  private def bankAddrSel(bankIdx: UInt, addr1: UInt, addr2: UInt, addr3: UInt) = {
    val oneHotSel = WireDefault(0.U(4.W))
    oneHotSel := Cat(addr1(addrLen - 1, addrLen - 2) === bankIdx, addr2(addrLen - 1, addrLen - 2) === bankIdx, addr3(addrLen - 1, addrLen - 2) === bankIdx)

    val bankAddr = WireDefault(0.U((addrLen - 2).W))
    switch (oneHotSel) {
      is ("b100".U) { bankAddr := io.readAddr1(addrLen - 3, 0)}
      is ("b010".U) { bankAddr := io.readAddr2(addrLen - 3, 0)}
      is ("b001".U) { bankAddr := io.readAddr3(addrLen - 3, 0)}
    }

    bankAddr
  }

  // Create ram banks
  val bank1 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))
  val bank2 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))
  val bank3 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))
  val bank4 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))

  val readData1 = WireDefault(0.U(bankWidth.W))
  val readData2 = WireDefault(0.U(bankWidth.W))
  val readData3 = WireDefault(0.U(bankWidth.W))

  val writeBankSel = WireDefault(0.U(2.W))

  // Write enable signals
  val b1We = WireDefault(false.B)
  val b2We = WireDefault(false.B)
  val b3We = WireDefault(false.B)
  val b4We = WireDefault(false.B)

  val outOpSel1 = RegInit(0.U(2.W))
  val outOpSel2 = RegInit(0.U(2.W))
  val outOpSel3 = RegInit(0.U(2.W))

  // Bank read multiplexers
  bank1.io.readAddr := bankAddrSel(0.U, io.readAddr1, io.readAddr2, io.readAddr3)
  bank2.io.readAddr := bankAddrSel(1.U, io.readAddr1, io.readAddr2, io.readAddr3)
  bank3.io.readAddr := bankAddrSel(2.U, io.readAddr1, io.readAddr2, io.readAddr3)
  bank4.io.readAddr := bankAddrSel(3.U, io.readAddr1, io.readAddr2, io.readAddr3)

  outOpSel1 := io.readAddr1(addrLen - 1, addrLen - 2)
  outOpSel2 := io.readAddr2(addrLen - 1, addrLen - 2)
  outOpSel3 := io.readAddr3(addrLen - 1, addrLen - 2)

  // Output data multiplexers
  switch(outOpSel1) {
    is(0.U) { readData1 := bank1.io.readData }
    is(1.U) { readData1 := bank2.io.readData }
    is(2.U) { readData1 := bank3.io.readData }
    is(3.U) { readData1 := bank4.io.readData }
  }

  switch(outOpSel2) {
    is(0.U) { readData2 := bank1.io.readData }
    is(1.U) { readData2 := bank2.io.readData }
    is(2.U) { readData2 := bank3.io.readData }
    is(3.U) { readData2 := bank4.io.readData }
  }

  switch(outOpSel3) {
    is(0.U) { readData3 := bank1.io.readData }
    is(1.U) { readData3 := bank2.io.readData }
    is(2.U) { readData3 := bank3.io.readData }
    is(3.U) { readData3 := bank4.io.readData }
  }

  // Bank write enable selector
  writeBankSel := io.writeAddr(addrLen - 1, addrLen - 2)
  when (io.we) {
    switch(writeBankSel) {
      is(0.U) { b1We := true.B }
      is(1.U) { b2We := true.B }
      is(2.U) { b3We := true.B }
      is(3.U) { b4We := true.B }
    }
  }

  bank1.io.we := b1We
  bank2.io.we := b2We
  bank3.io.we := b3We
  bank4.io.we := b4We

  bank1.io.writeAddr := io.writeAddr(addrLen - 3, 0)
  bank2.io.writeAddr := io.writeAddr(addrLen - 3, 0)
  bank3.io.writeAddr := io.writeAddr(addrLen - 3, 0)
  bank4.io.writeAddr := io.writeAddr(addrLen - 3, 0)

//  bank1.io.writeMask := io.writeMask
//  bank2.io.writeMask := io.writeMask
//  bank3.io.writeMask := io.writeMask
//  bank4.io.writeMask := io.writeMask

  bank1.io.writeData := io.writeData
  bank2.io.writeData := io.writeData
  bank3.io.writeData := io.writeData
  bank4.io.writeData := io.writeData

  io.readData1 := readData1
  io.readData2 := readData2
  io.readData3 := readData3
}