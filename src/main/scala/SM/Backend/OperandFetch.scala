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
      val pred = Input(UInt(2.W))
    }

    val ofPredReg = new Bundle {
      val addrR = Output(UInt((warpAddrLen + 2).W))
      val dataR = Input(UInt(warpSize.W))
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
      val pred = Output(UInt(2.W))
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

  // Registers to hold values while the operands are fetched from VRF
  val currThreadMask = RegNext(io.iss.threadMask, 0.U(warpSize.W))
  val currWarp = RegNext(io.iss.warp, 0.U(warpAddrLen.W))
  val currOpcode = RegNext(io.iss.opcode, 0.U(5.W))
  val currDest = RegNext(io.iss.dest, 0.U(5.W))
  val currImm = RegNext(io.iss.imm, 0.S(32.W))
  val currSrs = RegNext(io.iss.srs, 0.U(3.W))
  val currPred = RegNext(io.iss.pred, 0.U(2.W))

  // Select the one of the functional units based on the opcode
  val memOrAluSel = currOpcode =/= Opcodes.LD.asUInt(5.W) && currOpcode =/= Opcodes.ST.asUInt(5.W)

  // When predicate is enabled, combine the active thread mask with the evaluated predicate mask
  val combinedThreadMask = currThreadMask & io.ofPredReg.dataR

  vrf.io.we := io.wb.we
  vrf.io.writeAddr := Cat(io.wb.warp, io.wb.writeAddr)
  vrf.io.writeMask := io.wb.writeMask
  vrf.io.writeData := io.wb.writeData

  vrf.io.readAddr1 := Cat(io.iss.warp, io.iss.rs1)
  vrf.io.readAddr2 := Cat(io.iss.warp, io.iss.rs2)
  vrf.io.readAddr3 := Cat(io.iss.warp, io.iss.rs3)

  // To alu pipeline
  io.aluOf.threadMask := Mux(memOrAluSel, combinedThreadMask, 0.U)
  io.aluOf.warp := Mux(memOrAluSel, currWarp, 0.U)
  io.aluOf.opcode := Mux(memOrAluSel, currOpcode, 0.U)
  io.aluOf.dest := Mux(memOrAluSel, currDest, 0.U)
  io.aluOf.rs1 := Mux(memOrAluSel, vrf.io.readData1, 0.U)
  io.aluOf.rs2 := Mux(memOrAluSel, vrf.io.readData2, 0.U)
  io.aluOf.rs3 := Mux(memOrAluSel, vrf.io.readData3, 0.U)
  io.aluOf.srs := Mux(memOrAluSel, currSrs, 0.U)
  io.aluOf.imm := Mux(memOrAluSel, currImm, 0.S)
  io.aluOf.pred := Mux(memOrAluSel, currPred, 0.U)

  // To mem pipeline
  io.memOf.threadMask := Mux(!memOrAluSel, combinedThreadMask, 0.U)
  io.memOf.valid := !memOrAluSel
  io.memOf.warp := Mux(!memOrAluSel, currWarp, 0.U)
  io.memOf.opcode := Mux(!memOrAluSel, currOpcode, 0.U)
  io.memOf.dest := Mux(!memOrAluSel, currDest, 0.U)
  io.memOf.rs1 := Mux(!memOrAluSel, vrf.io.readData1, 0.U)
  io.memOf.rs2 := Mux(!memOrAluSel, vrf.io.readData2, 0.U)

  io.ofContainsMemInstr := !memOrAluSel
  io.ofPredReg.addrR := currWarp ## currPred
}
