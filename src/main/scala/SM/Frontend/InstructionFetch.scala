package SM.Frontend

import Constants.Opcodes
import SM.Frontend.IF._
import SM.Frontend.Ipdom.WarpStacks
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

  val warpStacks = Module(new WarpStacks(warpCount, warpSize, 16))
  val brnCtrl = Module(new BranchCtrlUnit(warpSize))

  // Registers to hold values while the instruction is being fetched from the instruction memory
  val instrPcReg = RegInit(0.U(32.W))
  val instrWarpReg = RegInit(0.U(warpAddrLen.W))
  val instrFetchReg = RegInit(false.B)
  val warpMaskReg = RegInit(0.U(warpSize.W))

  val isRetInstr = WireDefault(false.B)
  val shouldFetchNextInstr = !brnCtrl.io.prepare &&
    !brnCtrl.io.split &&
    !brnCtrl.io.join &&
    io.warpTable.done === 0.U &&
    !io.scheduler.stall //&&
//    !(isRetInstr && instrWarpReg === io.scheduler.warp)
  val memFetchPc = Mux(brnCtrl.io.jump, brnCtrl.io.jumpAddr, warpStacks.io.tosPc)

  warpStacks.io.warp := io.scheduler.warp
  warpStacks.io.updateTosPc := shouldFetchNextInstr
  warpStacks.io.newTosPc := memFetchPc + 1.U
  warpStacks.io.prepare := brnCtrl.io.prepare
  warpStacks.io.prepareAddr := brnCtrl.io.prepareAddr
  warpStacks.io.split := brnCtrl.io.split
  warpStacks.io.splitAddr := brnCtrl.io.splitAddr
  warpStacks.io.splitMask := brnCtrl.io.splitMask
  warpStacks.io.join := brnCtrl.io.join

  // Get the instruction from the instruction memory
  io.instrMem.addr := memFetchPc
  val instr = io.instrMem.data

  isRetInstr := instr(4, 0) === Opcodes.RET.asUInt(5.W)

  // Update the registers
  instrPcReg := memFetchPc
  instrWarpReg := io.scheduler.warp
  instrFetchReg := shouldFetchNextInstr
  warpMaskReg := warpStacks.io.tosMask

  // Read address for the predicate register file
  io.ifPredReg.addrR := io.scheduler.warp ## instr(31, 30)

  brnCtrl.io.instr := instr
  brnCtrl.io.pc := instrPcReg
  brnCtrl.io.pred := io.ifPredReg.dataR

  // Outputs of the instruction fetch stage
  io.instrF.pc := instrPcReg
  io.instrF.warp := instrWarpReg
  io.instrF.valid := instrFetchReg
  io.instrF.instr := Mux(instrFetchReg, instr, 0.U)
  io.instrF.threadMask := warpMaskReg
  io.instrF.setThreadDone := Mux(instrFetchReg, isRetInstr, false.B)
  io.instrF.setThreadDoneID := instrWarpReg
}
