package SM.Frontend

import Constants.Opcodes
import SM.Frontend.IF._
import chisel3._
import chisel3.util._

class InstructionFetch(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val scheduler = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val stall = Input(Bool())
      val reset = Input(Bool())
      val setValid = Input(Bool())
      val setValidWarps = Input(UInt(warpCount.W))
    }

    val setPending = Input(Bool())

    val wbIfCtrl = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val setInactive = Input(Bool())
    }

    val memIfCtrl = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val setNotPending = Input(Bool())
    }

    val instrF = new Bundle {
      val pc = Output(UInt(32.W))
      val warp = Output(UInt(warpAddrLen.W))
      val valid = Output(Bool())
      val instr = Output(UInt(32.W))
      val threadMask = Output(UInt(warpSize.W))
    }

    val warpTableStatus = new Bundle {
      val valid = Output(UInt(warpCount.W))
      val active = Output(UInt(warpCount.W))
      val pending = Output(UInt(warpCount.W))
    }

    val instrMem = new Bundle {
      val addr = Output(UInt(32.W))
      val data = Input(UInt(32.W))
    }

    val ifPredReg = new Bundle{
      val dataR = Input(UInt(warpSize.W))
      val addrR = Output(UInt((warpAddrLen + 2).W))
    }
  })

  val brnCtrl = Module(new BranchCtrlUnit(warpSize))
  val warpTable = Module(new WarpTable(warpCount, warpSize))

  // Registers to hold values while the instruction is being fetched from the instruction memory
  val pcReg = RegInit(0.U(32.W))
  val warpReg = RegInit(0.U(warpAddrLen.W))
  val fetchReg = RegInit(false.B)
  val validReg = RegInit(false.B)

  val instr = io.instrMem.data
  val opcode = instr(4, 0)
  val instrPc = Mux(brnCtrl.io.jump, brnCtrl.io.jumpAddr, warpTable.io.pc(io.scheduler.warp))
  val shouldFetchNxtInstr = warpTable.io.done(io.scheduler.warp) === 0.U && !io.scheduler.reset && !io.scheduler.stall && !io.scheduler.setValid
  val activeThreadMask = warpTable.io.threadMasks(warpReg)

  // Update the registers
  pcReg := instrPc
  warpReg := io.scheduler.warp
  fetchReg := shouldFetchNxtInstr
  validReg := !io.scheduler.stall

  brnCtrl.io.instr := instr
  brnCtrl.io.pcCurr := pcReg
  brnCtrl.io.nzpPred := io.ifPredReg.dataR

  warpTable.io.reset := io.scheduler.reset
  warpTable.io.validCtrl.set := io.scheduler.setValid
  warpTable.io.validCtrl.data := io.scheduler.setValidWarps
  warpTable.io.pcCtrl.set := shouldFetchNxtInstr
  warpTable.io.pcCtrl.idx := io.scheduler.warp
  warpTable.io.pcCtrl.data := Mux(shouldFetchNxtInstr, instrPc + 1.U, instrPc)
  warpTable.io.doneCtrl.set := Mux(fetchReg, opcode === Opcodes.RET.asUInt(5.W), false.B)
  warpTable.io.doneCtrl.idx := warpReg
  warpTable.io.setPendingCtrl.set := io.setPending
  warpTable.io.setPendingCtrl.idx := io.scheduler.warp
  warpTable.io.activeCtrl.set := io.wbIfCtrl.setInactive
  warpTable.io.activeCtrl.idx := io.wbIfCtrl.warp
  warpTable.io.setNotPendingCtrl.set := io.memIfCtrl.setNotPending
  warpTable.io.setNotPendingCtrl.idx := io.memIfCtrl.warp

  // Outputs of the instruction fetch stage
  io.instrF.valid := validReg
  io.instrF.warp := warpReg
  io.instrF.instr := Mux(fetchReg, instr, 0.U)
  io.instrF.pc := pcReg
  io.instrF.threadMask := activeThreadMask

  // Get the instruction from the instruction memory
  io.instrMem.addr := instrPc

  // Read address for the predicate register file
  io.ifPredReg.addrR := io.scheduler.warp ## instr(31, 30)

  // Warp table outputs to the scheduler
  io.warpTableStatus.valid := warpTable.io.valid
  io.warpTableStatus.active := warpTable.io.active
  io.warpTableStatus.pending := warpTable.io.pending
}
