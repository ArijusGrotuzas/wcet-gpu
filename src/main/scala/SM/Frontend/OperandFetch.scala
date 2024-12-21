package SM.Frontend

import chisel3._
import chisel3.util._
import SM.Frontend.VRF.VectorRegisterFile

class OperandFetch(warpCount: Int ) extends Module {
  val io = IO(new Bundle {
    val wb = new Bundle {
      val we = Input(Bool())
      val writeAddr = Input(UInt(5.W))
      val writeMask = Input(UInt(4.W))
      val writeData = Input(UInt(32.W))
    }

    val iss = new Bundle {
//      val pc = Input(UInt(32.W))
      val warp = Input(UInt(2.W))
      val opcode = Input(UInt(5.W))
      val dest = Input(UInt(5.W))
      val rs1 = Input(UInt(5.W))
      val rs2 = Input(UInt(5.W))
      val rs3 = Input(UInt(5.W))
      val imm = Input(UInt(22.W))
    }

    val of = IO(new Bundle{
      val warp = Output(UInt(2.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt((32 * warpCount).W))
      val rs2 = Output(UInt((32 * warpCount).W))
      val rs3 = Output(UInt((32 * warpCount).W))
      val imm = Output(UInt(22.W))
    })
  })

  val vrf = Module(new VectorRegisterFile(warpCount * 8, 32 * warpCount, 5))

  vrf.io.we := io.wb.we
  vrf.io.writeAddr := io.wb.writeAddr
  vrf.io.writeMask := io.wb.writeMask
  vrf.io.writeData := io.wb.writeData

  vrf.readAddr1 := io.iss.rs1
  vrf.readAddr2 := io.iss.rs2
  vrf.readAddr3 := io.iss.rs3

  io.of.warp := io.iss.warp
  io.of.opcode := io.iss.opcode
  io.of.dest := io.iss.dest
  io.of.rs1 := vrf.io.readData1
  io.of.rs2 := vrf.io.readData2
  io.of.rs3 := vrf.io.readData3
  io.of.imm := io.iss.imm
}
