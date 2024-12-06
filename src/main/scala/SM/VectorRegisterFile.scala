package SM

import chisel3._
import chisel3.util._

class VectorRegisterFile(bankDepth: Int, bankWidth: Int) extends Module {
  val io = IO(new Bundle {
    // Inputs
    val we = Input(Bool())
    val writeAddr = Input(UInt(10.W))
    val writeMask = Input(UInt(4.W))
    val writeData = Input(UInt(bankWidth.W))

    val readAddr1 = Input(UInt(10.W))
    val readAddr2 = Input(UInt(10.W))
    val readAddr3 = Input(UInt(10.W))
    // Outputs
    val readData1 = Output(UInt(bankWidth.W))
    val readData2 = Output(UInt(bankWidth.W))
    val readData3 = Output(UInt(bankWidth.W))
  })

  private def bankAddrSel(bankIdx: UInt, addr1: UInt, addr2: UInt, addr3: UInt) = {
    val oneHotSel = WireDefault(0.U(4.W))
    oneHotSel := Cat(addr1(9, 8) === bankIdx, addr2(9, 8) === bankIdx, addr3(9, 8) === bankIdx)

    val bankAddr = WireDefault(0.U(10.W))
    switch (oneHotSel) {
      is ("b100".U) { bankAddr := io.readAddr1}
      is ("b010".U) { bankAddr := io.readAddr2}
      is ("b001".U) { bankAddr := io.readAddr3}
    }

    bankAddr
  }
  val writeBank = WireDefault(0.U(10.W))

  // Creates ram banks
  val bank1 = Module(new DualPortedRam(bankDepth, bankWidth))
  val bank2 = Module(new DualPortedRam(bankDepth, bankWidth))
  val bank3 = Module(new DualPortedRam(bankDepth, bankWidth))
  val bank4 = Module(new DualPortedRam(bankDepth, bankWidth))

  // First bank read
  bank1.io.readAddr := bankAddrSel(0.U, io.readAddr1, io.readAddr2, io.readAddr3)

  // Second bank read
  bank2.io.readAddr := bankAddrSel(1.U, io.readAddr1, io.readAddr2, io.readAddr3)

  // Third bank read
  bank3.io.readAddr := bankAddrSel(2.U, io.readAddr1, io.readAddr2, io.readAddr3)

  // Fourth bank read
  bank4.io.readAddr := bankAddrSel(3.U, io.readAddr1, io.readAddr2, io.readAddr3)

  // Write data
  val b1We = WireDefault(false.B)
  val b2We = WireDefault(false.B)
  val b3We = WireDefault(false.B)
  val b4We = WireDefault(false.B)

  when (io.we) {
    writeBank := io.writeAddr(9, 8)
//    printf(cf"writeBank = $writeBank")
    switch(io.writeAddr(9, 8)) {
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

  bank1.io.writeAddr := io.writeAddr
  bank2.io.writeAddr := io.writeAddr
  bank3.io.writeAddr := io.writeAddr
  bank4.io.writeAddr := io.writeAddr

//  bank1.io.writeMask := io.writeMask
//  bank2.io.writeMask := io.writeMask
//  bank3.io.writeMask := io.writeMask
//  bank4.io.writeMask := io.writeMask

  bank1.io.writeData := io.writeData
  bank2.io.writeData := io.writeData
  bank3.io.writeData := io.writeData
  bank4.io.writeData := io.writeData

  io.readData1 := bank1.io.readData
  io.readData2 := bank2.io.readData
  io.readData3 := bank3.io.readData
}