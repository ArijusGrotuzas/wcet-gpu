package SM.Backend.Vrf

import chisel3._
import chisel3.util._
import SM.Frontend.DualPortedRam

class VectorRegisterFile(warpCount: Int, bankWidth: Int) extends Module {
  val bankDepth = warpCount * 8
  val addrLen = log2Up(bankDepth) + 2
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

  // TODO: If attempting to read 0 register, return 0
  def bankRouter(arbiterSel: UInt, readAddr1: UInt, readAddr2: UInt, readAddr3: UInt, we: UInt, writeData: UInt, writeAddr: UInt): UInt = {
    val bank = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))
    val bankReadAddr = WireDefault(0.U((addrLen - 2).W))

    switch(arbiterSel) {
      is(0.U) {
        bankReadAddr := readAddr1(addrLen - 1, 2)
      }
      is(1.U) {
        bankReadAddr := readAddr2(addrLen - 1, 2)
      }
      is(2.U) {
        bankReadAddr := readAddr3(addrLen - 1, 2)
      }
      is(3.U) {
        bankReadAddr := 0.U
      }
    }

    bank.io.readAddr := bankReadAddr

    bank.io.we := we
    bank.io.writeAddr := writeAddr(addrLen - 1, 2)
    bank.io.writeData := writeData

    bank.io.readData
  }

  def readDataMux(outOpSel: UInt, bank1Data: UInt, bank2Data: UInt, bank3Data: UInt, bank4Data: UInt): UInt = {
    val readData = WireDefault(0.U(bankWidth.W))

    switch(outOpSel) {
      is(0.U) {
        readData := bank1Data
      }
      is(1.U) {
        readData := bank2Data
      }
      is(2.U) {
        readData := bank3Data
      }
      is(3.U) {
        readData := bank4Data
      }
    }

    readData
  }

  // Create fixed-priority access arbiter
  val requestArbiter = Module(new Arbiter3To4)

  val outOpSel1 = RegInit(0.U(2.W))
  val outOpSel2 = RegInit(0.U(2.W))
  val outOpSel3 = RegInit(0.U(2.W))

  val writeBankSel = WireDefault(0.U(2.W))
  val b1We = WireDefault(false.B)
  val b2We = WireDefault(false.B)
  val b3We = WireDefault(false.B)
  val b4We = WireDefault(false.B)

  // Bank write enable selector
  writeBankSel := io.writeAddr(1, 0)
  when (io.we) {
    switch(writeBankSel) {
      is(0.U) { b1We := true.B }
      is(1.U) { b2We := true.B }
      is(2.U) { b3We := true.B }
      is(3.U) { b4We := true.B }
    }
  }

  // Route access requests to arbiter
  requestArbiter.io.in_sel(0) := io.readAddr1(1, 0)
  requestArbiter.io.in_sel(1) := io.readAddr2(1, 0)
  requestArbiter.io.in_sel(2) := io.readAddr3(1, 0)

  // Prevent writing to the x0 register
  val writeToZeroReg = io.writeAddr(4, 2) === 0.U

  val bank1ReadData = bankRouter(requestArbiter.io.out_sel(0), io.readAddr1, io.readAddr2, io.readAddr3, Mux(writeToZeroReg, false.B, b1We), io.writeData, io.writeAddr)
  val bank2ReadData = bankRouter(requestArbiter.io.out_sel(1), io.readAddr1, io.readAddr2, io.readAddr3, b2We, io.writeData, io.writeAddr)
  val bank3ReadData = bankRouter(requestArbiter.io.out_sel(2), io.readAddr1, io.readAddr2, io.readAddr3, b3We, io.writeData, io.writeAddr)
  val bank4ReadData = bankRouter(requestArbiter.io.out_sel(3), io.readAddr1, io.readAddr2, io.readAddr3, b4We, io.writeData, io.writeAddr)

  // Update the output data multiplexer select signal registers
  outOpSel1 := io.readAddr1(1, 0)
  outOpSel2 := io.readAddr2(1, 0)
  outOpSel3 := io.readAddr3(1, 0)

  // Output data registers
  io.readData1 := readDataMux(outOpSel1, bank1ReadData, bank2ReadData, bank3ReadData, bank4ReadData)
  io.readData2 := readDataMux(outOpSel2, bank1ReadData, bank2ReadData, bank3ReadData, bank4ReadData)
  io.readData3 := readDataMux(outOpSel3, bank1ReadData, bank2ReadData, bank3ReadData, bank4ReadData)
}
