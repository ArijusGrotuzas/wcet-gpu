package SM.Frontend

import Constants.Opcodes
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

    val nzpUpdateCtrl = new Bundle {
      val en = Input(Bool())
      val nzp = Input(UInt((3 * warpSize).W))
      val warp = Input(UInt(warpAddrLen.W))
    }

    val setPending = Input(Bool())

//    val issIfCtrl = new Bundle {
//      val jump = Input(Bool())
//      val jumpAddr = Input(UInt(32.W))
//    }

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
  })

  val brnCtrl = Module(new BranchCtrlUnit())
  val warpTable = Module(new WarpTable(warpCount))
  val predRegFile = Module(new PredicateRegister(warpCount, warpSize))

  // Registers to hold values while the instruction is being fetched from the instruction memory
  val pcReg = RegInit(0.U(32.W))
  val warpReg = RegInit(0.U(warpAddrLen.W))
  val fetchReg = RegInit(false.B)
  val validReg = RegInit(false.B)

  val instr = io.instrMem.data
  val opcode = instr(4, 0)
  val instrPc = Mux(brnCtrl.io.jump, brnCtrl.io.jumpAddr, warpTable.io.pc(io.scheduler.warp))
  val predRegFileData = predRegFile.io.dataR
  val shouldFetchNxtInstr = warpTable.io.done(io.scheduler.warp) === 0.U && !io.scheduler.reset && !io.scheduler.stall && !io.scheduler.setValid && (opcode =/= "b11111".U)

  // Update the registers
  pcReg := instrPc
  warpReg := io.scheduler.warp
  fetchReg := shouldFetchNxtInstr
  validReg := !io.scheduler.stall

  brnCtrl.io.instr := instr
  brnCtrl.io.pcCurr := pcReg
  brnCtrl.io.nzpPred := predRegFileData(2, 0)

  predRegFile.io.we := io.nzpUpdateCtrl.en
  predRegFile.io.addrR := io.scheduler.warp
  predRegFile.io.addrW := io.nzpUpdateCtrl.warp
  predRegFile.io.dataW := io.nzpUpdateCtrl.nzp

  warpTable.io.reset := io.scheduler.reset
  warpTable.io.validCtrl.set := io.scheduler.setValid
  warpTable.io.validCtrl.data := io.scheduler.setValidWarps
  warpTable.io.pcCtrl.set := shouldFetchNxtInstr
  warpTable.io.pcCtrl.idx := io.scheduler.warp
  warpTable.io.pcCtrl.data := Mux(shouldFetchNxtInstr, instrPc + 1.U, instrPc)
  warpTable.io.doneCtrl.set := (opcode === Opcodes.RET.asUInt(5.W))
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

  // Get the instruction from the instruction memory
  io.instrMem.addr := instrPc

  // Warp table outputs to the scheduler
  io.warpTableStatus.valid := warpTable.io.valid
  io.warpTableStatus.active := warpTable.io.active
  io.warpTableStatus.pending := warpTable.io.pending
}
