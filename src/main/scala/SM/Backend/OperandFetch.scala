package SM.Backend

import SM.Backend.VRF.VectorRegisterFile
import SM.Isa
import chisel3._
import chisel3.util._

class OperandFetch(warpCount: Int, warpSize: Int, warpAddrLen: Int) extends Module {
  val io = IO(new Bundle {
    val wb = new Bundle {
      val we = Input(Bool())
      val warp = Input(UInt(warpAddrLen.W))
      val writeAddr = Input(UInt(5.W))
      val writeMask = Input(UInt(4.W))
      val writeData = Input(UInt((32 * warpSize).W))
    }

    val iss = new Bundle {
      //      val pc = Input(UInt(32.W))
      val warp = Input(UInt(warpAddrLen.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val imm = Input(UInt(22.W))
    }

    val aluOf = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt((32 * warpSize).W))
      val rs2 = Output(UInt((32 * warpSize).W))
      val rs3 = Output(UInt((32 * warpSize).W))
      val imm = Output(UInt(22.W))
    }

    val memOf = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt((32 * warpSize).W))
      val rs2 = Output(UInt((32 * warpSize).W))
      val imm = Output(UInt(22.W))
    }
  })

  val vrf = Module(new VectorRegisterFile(warpCount * 8, 32 * warpSize, warpAddrLen + 5))

  val warp = RegInit(0.U(warpAddrLen.W))
  val opcode = RegInit(0.U(5.W))
  val dest = RegInit(0.U(5.W))
  val rs1 = RegInit(0.U(5.W))
  val rs2 = RegInit(0.U(5.W))
  val rs3 = RegInit(0.U(5.W))
  val imm = RegInit(0.U(22.W))
  val pipeSel = WireDefault(true.B)

  warp := io.iss.warp
  opcode := io.iss.opcode
  dest := io.iss.dest
  rs1 := io.iss.rs1
  rs2 := io.iss.rs2
  rs3 := io.iss.rs3
  imm := io.iss.imm

  when(io.iss.opcode === Isa.LD || io.iss.opcode === Isa.ST) {
    pipeSel := false.B
  }

  vrf.io.we := io.wb.we
  vrf.io.writeAddr := Cat(io.wb.warp, io.wb.writeAddr)
  vrf.io.writeMask := io.wb.writeMask
  vrf.io.writeData := io.wb.writeData

  vrf.io.readAddr1 := Cat(io.iss.warp, io.iss.rs1)
  vrf.io.readAddr2 := Cat(io.iss.warp, io.iss.rs2)
  vrf.io.readAddr3 := Cat(io.iss.warp, io.iss.rs3)

  // To alu pipeline
  io.aluOf.warp := Mux(pipeSel, warp, 0.U)
  io.aluOf.opcode := Mux(pipeSel, io.iss.opcode, 0.U)
  io.aluOf.dest := Mux(pipeSel, io.iss.dest, 0.U)
  io.aluOf.rs1 := Mux(pipeSel, vrf.io.readData1, 0.U)
  io.aluOf.rs2 := Mux(pipeSel, vrf.io.readData2, 0.U)
  io.aluOf.rs3 := Mux(pipeSel, vrf.io.readData3, 0.U)
  io.aluOf.imm := Mux(pipeSel, io.iss.imm, 0.U)

  // To mem pipeline
  io.memOf.warp := Mux(!pipeSel, warp, 0.U)
  io.memOf.opcode := Mux(!pipeSel, io.iss.opcode, 0.U)
  io.memOf.dest := Mux(!pipeSel, io.iss.dest, 0.U)
  io.memOf.rs1 := Mux(!pipeSel, vrf.io.readData1, 0.U)
  io.memOf.rs2 := Mux(!pipeSel, vrf.io.readData2, 0.U)
  io.memOf.imm := Mux(!pipeSel, io.iss.imm, 0.U)
}
