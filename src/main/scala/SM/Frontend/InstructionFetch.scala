package SM.Frontend

import Constants.Opcodes
import SM.Frontend.IF._
import chisel3._
import chisel3.util._

// TODO: Add IPDOM stack
class InstructionFetch(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val scheduler = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val stall = Input(Bool())
    }

    val warpTable = new Bundle {
      val done = Input(UInt(warpCount.W))
      val threadMask = Input(UInt(warpSize.W))
      val pc = Input(UInt(32.W))
    }

    val instrMem = new Bundle {
      val addr = Output(UInt(32.W))
      val data = Input(UInt(32.W))
    }

    val ifPredReg = new Bundle{
      val dataR = Input(UInt(warpSize.W))
      val addrR = Output(UInt((warpAddrLen + 2).W))
    }

    val tempPcCtrl = new Bundle {
      val set = Output(UInt(32.W))
      val idx = Output(UInt(warpAddrLen.W))
      val data = Output(UInt(32.W))
    }

    val instrF = new Bundle {
      val pc = Output(UInt(32.W))
      val warp = Output(UInt(warpAddrLen.W))
      val valid = Output(Bool())
      val instr = Output(UInt(32.W))
      val threadMask = Output(UInt(warpSize.W))
      val setThreadDone = Output(Bool())
      val setThreadDoneID = Output(UInt(warpAddrLen.W))
    }
  })

  val brnCtrl = Module(new BranchCtrlUnit(warpSize))

  // Registers to hold values while the instruction is being fetched from the instruction memory
  val instrPcReg = RegInit(0.U(32.W))
  val warpReg = RegInit(0.U(warpAddrLen.W))
  val fetchReg = RegInit(false.B)
  val threadMaskReg = RegInit(0.U(warpSize.W))

  val isRetInstr = WireDefault(false.B)

  val memFetchPc = Mux(brnCtrl.io.jump, brnCtrl.io.jumpAddr, io.warpTable.pc)
  val shouldFetchNextInstr = io.warpTable.done === 0.U && !io.scheduler.stall && !isRetInstr

  // Update the registers
  instrPcReg := memFetchPc
  warpReg := io.scheduler.warp
  fetchReg := shouldFetchNextInstr
  threadMaskReg := io.warpTable.threadMask

  val instr = io.instrMem.data
  isRetInstr := instr(4, 0) === Opcodes.RET.asUInt(5.W)

  brnCtrl.io.instr := instr
  brnCtrl.io.pcCurr := instrPcReg
  brnCtrl.io.nzpPred := io.ifPredReg.dataR

  // Outputs of the instruction fetch stage
  io.instrF.valid := fetchReg
  io.instrF.warp := warpReg
  io.instrF.instr := Mux(fetchReg, instr, 0.U)
  io.instrF.pc := instrPcReg
  io.instrF.threadMask := threadMaskReg
  io.instrF.setThreadDone := Mux(fetchReg, isRetInstr, false.B)
  io.instrF.setThreadDoneID := warpReg

  io.tempPcCtrl.set := shouldFetchNextInstr
  io.tempPcCtrl.idx := io.scheduler.warp
  io.tempPcCtrl.data := Mux(shouldFetchNextInstr, memFetchPc + 1.U, memFetchPc)

  // Get the instruction from the instruction memory
  io.instrMem.addr := memFetchPc

  // Read address for the predicate register file
  io.ifPredReg.addrR := io.scheduler.warp ## instr(31, 30)
}
