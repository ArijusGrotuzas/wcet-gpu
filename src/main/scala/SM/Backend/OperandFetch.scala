package SM.Backend

import Constants.Opcodes
import SM.Backend.Vrf.VectorRegisterFile
import chisel3._
import chisel3.util._

class OperandFetch(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val wb = new Bundle {
      val we = Input(Bool())
      val warp = Input(UInt(warpAddrLen.W))
      val writeAddr = Input(UInt(5.W))
      val writeMask = Input(UInt(warpSize.W))
      val writeData = Input(UInt((32 * warpSize).W))
    }

    val iss = new Bundle {
      val threadMask = Input(UInt(warpSize.W))
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val srs = Input(UInt(3.W))
      val imm = Input(SInt(32.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
    }

    val aluOf = new Bundle {
      val threadMask = Output(UInt(warpSize.W))
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt((32 * warpSize).W))
      val rs2 = Output(UInt((32 * warpSize).W))
      val rs3 = Output(UInt((32 * warpSize).W))
      val imm = Output(SInt(32.W))
      val srs = Output(UInt(3.W))
    }

    val memOf = new Bundle {
      val threadMask = Output(UInt(warpSize.W))
      val valid = Output(Bool())
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt((32 * warpSize).W))
      val rs2 = Output(UInt((32 * warpSize).W))
    }

    val ofContainsMemInstr = Output(Bool())
  })

  val vrf = Module(new VectorRegisterFile(warpCount, warpSize, 32 * warpSize))
  val memOrAluSel = WireDefault(true.B)

  // Registers to hold values while the operands are fetched from VRF
  val warp = RegInit(0.U(warpAddrLen.W))
  val opcode = RegInit(0.U(5.W))
  val dest = RegInit(0.U(5.W))
  val imm = RegInit(0.S(32.W))
  val srs = RegInit(0.U(3.W))

  warp := io.iss.warp
  opcode := io.iss.opcode
  dest := io.iss.dest
  imm := io.iss.imm
  srs := io.iss.srs

  // Select the one of the functional units based on the opcode
  when(opcode === Opcodes.LD.asUInt(5.W) || opcode === Opcodes.ST.asUInt(5.W)) {
    memOrAluSel := false.B
  }

  vrf.io.we := io.wb.we
  vrf.io.writeAddr := Cat(io.wb.warp, io.wb.writeAddr)
  vrf.io.writeMask := io.wb.writeMask
  vrf.io.writeData := io.wb.writeData

  vrf.io.readAddr1 := Cat(io.iss.warp, io.iss.rs1)
  vrf.io.readAddr2 := Cat(io.iss.warp, io.iss.rs2)
  vrf.io.readAddr3 := Cat(io.iss.warp, io.iss.rs3)

  // To alu pipeline
  io.aluOf.threadMask := Mux(memOrAluSel, io.iss.threadMask, 0.U)
  io.aluOf.warp := Mux(memOrAluSel, warp, 0.U)
  io.aluOf.opcode := Mux(memOrAluSel, opcode, 0.U)
  io.aluOf.dest := Mux(memOrAluSel, dest, 0.U)
  io.aluOf.rs1 := Mux(memOrAluSel, vrf.io.readData1, 0.U)
  io.aluOf.rs2 := Mux(memOrAluSel, vrf.io.readData2, 0.U)
  io.aluOf.rs3 := Mux(memOrAluSel, vrf.io.readData3, 0.U)
  io.aluOf.srs := Mux(memOrAluSel, srs, 0.U)
  io.aluOf.imm := Mux(memOrAluSel, imm, 0.S)

  // To mem pipeline
  io.memOf.threadMask := Mux(!memOrAluSel, io.iss.threadMask, 0.U)
  io.memOf.valid := !memOrAluSel
  io.memOf.warp := Mux(!memOrAluSel, warp, 0.U)
  io.memOf.opcode := Mux(!memOrAluSel, opcode, 0.U)
  io.memOf.dest := Mux(!memOrAluSel, dest, 0.U)
  io.memOf.rs1 := Mux(!memOrAluSel, vrf.io.readData1, 0.U)
  io.memOf.rs2 := Mux(!memOrAluSel, vrf.io.readData2, 0.U)

  io.ofContainsMemInstr := !memOrAluSel
}
