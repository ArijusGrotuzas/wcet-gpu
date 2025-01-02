package SM.Backend.Vrf

import chisel3._
import chisel3.util._
import SM.Frontend.DualPortedRam

// TODO: Create functions for generating hardware to make the code cleaner
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

  // Create ram banks
  val bank1 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))
  val bank2 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))
  val bank3 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))
  val bank4 = Module(new DualPortedRam(bankDepth, bankWidth, addrLen - 2))

  // Create fixed-priority access arbiter
  val requestArbiter = Module(new Arbiter3To4)

  // Read signals
  val readAddr1 = WireDefault(0.U((addrLen - 2).W))
  val readAddr2 = WireDefault(0.U((addrLen - 2).W))
  val readAddr3 = WireDefault(0.U((addrLen - 2).W))
  val readAddr4 = WireDefault(0.U((addrLen - 2).W))

  val readData1 = WireDefault(0.U(bankWidth.W))
  val readData2 = WireDefault(0.U(bankWidth.W))
  val readData3 = WireDefault(0.U(bankWidth.W))

  val outOpSel1 = RegInit(0.U(2.W))
  val outOpSel2 = RegInit(0.U(2.W))
  val outOpSel3 = RegInit(0.U(2.W))

  // Write signals
  val b1We = WireDefault(false.B)
  val b2We = WireDefault(false.B)
  val b3We = WireDefault(false.B)
  val b4We = WireDefault(false.B)

  val writeBankSel = WireDefault(0.U(2.W))

  // ------------------ Read data ------------------

  requestArbiter.io.in_sel(0) := io.readAddr1(1, 0)
  requestArbiter.io.in_sel(1) := io.readAddr2(1, 0)
  requestArbiter.io.in_sel(2) := io.readAddr3(1, 0)

  switch(requestArbiter.io.out_sel(0)) {
    is(0.U) { readAddr1 := io.readAddr1(addrLen - 1, 2) }
    is(1.U) { readAddr1 := io.readAddr2(addrLen - 1, 2) }
    is(2.U) { readAddr1 := io.readAddr3(addrLen - 1, 2) }
    is(3.U) { readAddr1 := 0.U }
  }

  switch(requestArbiter.io.out_sel(1)) {
    is(0.U) { readAddr2 := io.readAddr1(addrLen - 1, 2) }
    is(1.U) { readAddr2 := io.readAddr2(addrLen - 1, 2) }
    is(2.U) { readAddr2 := io.readAddr3(addrLen - 1, 2) }
    is(3.U) { readAddr2 := 0.U }
  }

  switch(requestArbiter.io.out_sel(2)) {
    is(0.U) { readAddr3 := io.readAddr1(addrLen - 1, 2) }
    is(1.U) { readAddr3 := io.readAddr2(addrLen - 1, 2) }
    is(2.U) { readAddr3 := io.readAddr3(addrLen - 1, 2) }
    is(3.U) { readAddr3 := 0.U }
  }

  switch(requestArbiter.io.out_sel(3)) {
    is(0.U) { readAddr4 := io.readAddr1(addrLen - 1, 2) }
    is(1.U) { readAddr4 := io.readAddr2(addrLen - 1, 2) }
    is(2.U) { readAddr4 := io.readAddr3(addrLen - 1, 2) }
    is(3.U) { readAddr4 := 0.U }
  }

  bank1.io.readAddr := readAddr1
  bank2.io.readAddr := readAddr2
  bank3.io.readAddr := readAddr3
  bank4.io.readAddr := readAddr4

  // Output data multiplexers
  outOpSel1 := io.readAddr1(1, 0)
  outOpSel2 := io.readAddr2(1, 0)
  outOpSel3 := io.readAddr3(1, 0)

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

  io.readData1 := readData1
  io.readData2 := readData2
  io.readData3 := readData3

  // ------------------ Write data ------------------

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

  bank1.io.we := b1We
  bank2.io.we := b2We
  bank3.io.we := b3We
  bank4.io.we := b4We

  bank1.io.writeAddr := io.writeAddr(addrLen - 1, 2)
  bank2.io.writeAddr := io.writeAddr(addrLen - 1, 2)
  bank3.io.writeAddr := io.writeAddr(addrLen - 1, 2)
  bank4.io.writeAddr := io.writeAddr(addrLen - 1, 2)

  bank1.io.writeData := io.writeData
  bank2.io.writeData := io.writeData
  bank3.io.writeData := io.writeData
  bank4.io.writeData := io.writeData
}
