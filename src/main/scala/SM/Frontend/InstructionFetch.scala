package SM.Frontend

import Constants.Opcodes
import SM.Frontend.IF._
import SM.Frontend.Ipdom.WarpStacks
import chisel3._
import chisel3.util._

import scala.math.pow

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
    }

    val instrMem = new Bundle {
      val addr = Output(UInt(32.W))
      val data = Input(UInt(32.W))
    }

    val ifPredReg = new Bundle{
      val dataR = Input(UInt(warpSize.W))
      val addrR = Output(UInt((warpAddrLen + 2).W))
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

  val warpStacks = Module(new WarpStacks(warpCount, warpSize))
  val brnCtrl = Module(new BranchCtrlUnit(warpSize))

  // Registers to hold values while the instruction is being fetched from the instruction memory
  val instrPcReg = RegInit(0.U(32.W))
  val warpReg = RegInit(0.U(warpAddrLen.W))
  val fetchReg = RegInit(false.B)
  val threadMaskReg = RegInit(0.U(warpSize.W))

  warpStacks.io.warp := io.scheduler.warp

  val isRetInstr = WireDefault(false.B)
  val shouldFetchNextInstr = io.warpTable.done === 0.U && !io.scheduler.stall && !(isRetInstr && warpReg === io.scheduler.warp)
  val memFetchPc = Mux(brnCtrl.io.jump, brnCtrl.io.jumpAddr, warpStacks.io.tosPc)

  warpStacks.io.updateTosPc := shouldFetchNextInstr
  warpStacks.io.newTosPc := memFetchPc + 1.U

  // Get the instruction from the instruction memory
  io.instrMem.addr := memFetchPc

  // Update the registers
  instrPcReg := memFetchPc
  warpReg := io.scheduler.warp
  fetchReg := shouldFetchNextInstr
  threadMaskReg := warpStacks.io.tosMask

  val instr = io.instrMem.data
  isRetInstr := instr(4, 0) === Opcodes.RET.asUInt(5.W)

  brnCtrl.io.instr := instr
  brnCtrl.io.pcCurr := instrPcReg
  brnCtrl.io.nzpPred := io.ifPredReg.dataR
  // Read address for the predicate register file
  io.ifPredReg.addrR := io.scheduler.warp ## instr(31, 30)

  // Outputs of the instruction fetch stage
  io.instrF.pc := instrPcReg
  io.instrF.warp := warpReg
  io.instrF.valid := fetchReg
  io.instrF.instr := Mux(fetchReg, instr, 0.U)
  io.instrF.threadMask := threadMaskReg
  io.instrF.setThreadDone := Mux(fetchReg, isRetInstr, false.B)
  io.instrF.setThreadDoneID := warpReg
}
