package SM.Backend

import SM.Backend.VRF.VectorRegisterFile
import chisel3._

class OperandFetch(warpCount: Int, warpAddrLen: Int) extends Module {
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

    val aluOf = IO(new Bundle{
      val warp = Output(UInt(2.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt((32 * warpCount).W))
      val rs2 = Output(UInt((32 * warpCount).W))
      val rs3 = Output(UInt((32 * warpCount).W))
      val imm = Output(UInt(22.W))
    })

    val memOf = IO(new Bundle{
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
  val warp = RegInit(0.U(warpAddrLen.W))
  val opcode = RegInit(0.U(5.W))
  val dest = RegInit(0.U(5.W))
  val rs1 = RegInit(0.U(5.W))
  val rs2 = RegInit(0.U(5.W))
  val rs3 = RegInit(0.U(5.W))
  val imm = RegInit(0.U(22.W))
  val pipeSel = WireDefault(true.B)

  when(io.iss.opcode === "b00001".U || io.iss.opcode === "b00010".U) {
    pipeSel := false.B
  }

  vrf.io.we := io.wb.we
  vrf.io.writeAddr := io.wb.writeAddr
  vrf.io.writeMask := io.wb.writeMask
  vrf.io.writeData := io.wb.writeData

  vrf.readAddr1 := io.iss.rs1
  vrf.readAddr2 := io.iss.rs2
  vrf.readAddr3 := io.iss.rs3

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
  io.memOf.rs3 := Mux(!pipeSel, vrf.io.readData3, 0.U)
  io.memOf.imm := Mux(!pipeSel, io.iss.imm, 0.U)
}
