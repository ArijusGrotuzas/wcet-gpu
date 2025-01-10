package SM.Frontend

import Constants.Opcodes
import chisel3._
import chisel3.util._

class InstructionFetch(warpCount: Int) extends Module {
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

    val issIfCtrl = new Bundle {
      val jump = Input(Bool())
      val jumpAddr = Input(UInt(32.W))
    }

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

  val warpTable = Module(new WarpTable(4, 2))

  val setDone = WireDefault(false.B)
  val instr = WireDefault(0.U(32.W))
  val outInstr = WireDefault(0.U(32.W))
  val opcode = WireDefault(0.U(5.W))

  // Register update signals
  val warpTablePcNext = WireDefault(0.U(32.W))
  val fetchRegNext = WireDefault(false.B)

  // Registers
  val warpReg = RegInit(0.U(warpAddrLen.W))
  val fetchReg = RegInit(false.B)
  val validReg = RegInit(false.B)
  val pcReg = RegInit(0.U(32.W))

  // ------------ First half of the pipeline ------------
  fetchRegNext := warpTable.io.done(io.scheduler.warp) === 0.U && !io.scheduler.reset && !io.scheduler.stall && !io.scheduler.setValid && (opcode =/= "b11111".U)
  fetchReg := fetchRegNext

  // Mux for setting the next PC of a warp
  when(io.issIfCtrl.jump) {
    warpTablePcNext := io.issIfCtrl.jumpAddr
  }.otherwise {
    when(fetchRegNext) {
      warpTablePcNext := warpTable.io.pc(io.scheduler.warp) + 1.U
    }
  }

  pcReg := warpTable.io.pc(io.scheduler.warp)
  warpReg := io.scheduler.warp
  validReg := !io.scheduler.stall

  warpTable.io.reset := io.scheduler.reset
  warpTable.io.validCtrl.set := io.scheduler.setValid
  warpTable.io.validCtrl.data := io.scheduler.setValidWarps
  warpTable.io.pcCtrl.set := fetchRegNext
  warpTable.io.pcCtrl.idx := io.scheduler.warp
  warpTable.io.pcCtrl.data := warpTablePcNext
  warpTable.io.doneCtrl.set := setDone
  warpTable.io.doneCtrl.idx := warpReg
  warpTable.io.setPendingCtrl.set := io.setPending
  warpTable.io.setPendingCtrl.idx := io.scheduler.warp
  warpTable.io.activeCtrl.set := io.wbIfCtrl.setInactive
  warpTable.io.activeCtrl.idx := io.wbIfCtrl.warp
  warpTable.io.setNotPendingCtrl.set := io.memIfCtrl.setNotPending
  warpTable.io.setNotPendingCtrl.idx := io.memIfCtrl.warp

  // Get the instruction from the instruction memory
  io.instrMem.addr := warpTable.io.pc(io.scheduler.warp)
  instr := io.instrMem.data

  // ------------ Second half of the pipeline ------------
  when(fetchReg) {
    outInstr := instr
  }.otherwise {
    outInstr := 0.U
  }

  opcode := instr(4, 0)

  // If the instruction opcode is RET, set the warp as done
  when(opcode === Opcodes.RET.asUInt(5.W)) {
    setDone := true.B
  }

  io.instrF.valid := validReg
  io.instrF.warp := warpReg
  io.instrF.instr := outInstr
  io.instrF.pc := pcReg

  io.warpTableStatus.valid := warpTable.io.valid
  io.warpTableStatus.active := warpTable.io.active
  io.warpTableStatus.pending := warpTable.io.pending
}
