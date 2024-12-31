package SM.Frontend

import SM.Opcodes
import chisel3._

class InstructionFetch(warpCount: Int, warpAddrLen: Int) extends Module {
  val io = IO(new Bundle {
    val loadInstr = new Bundle {
      val en = Input(Bool())
      val instr = Input(UInt(32.W))
      val addr = Input(UInt(32.W))
    }

    val scheduler = new Bundle {
      val stall = Input(Bool())
      val warp = Input(UInt(warpAddrLen.W))
      val reset = Input(Bool())
      val setValid = Input(Bool())
      val validWarps = Input(UInt(warpCount.W))
    }

    val setPending = Input(Bool())

    val wb = new Bundle {
      val setNotPending = Input(Bool())
      val setInactive = Input(Bool())
      val warp = Input(UInt(warpAddrLen.W))
    }

    val instrF = new Bundle {
      val valid = Output(Bool())
      val pc = Output(UInt(32.W))
      val instr = Output(UInt(32.W))
      val warp = Output(UInt(warpAddrLen.W))
    }

    val warpTable = new Bundle {
      val valid = Output(UInt(warpCount.W))
      val active = Output(UInt(warpCount.W))
      val pending = Output(UInt(warpCount.W))
    }
  })

  val warpTable = Module(new WarpTable(4, 2))
  val instructionCache = Module(new InstructionCache(32, 1024, 5))
  val setDone = WireDefault(false.B)

  val instr = WireDefault(0.U(32.W))
  val instrAddr = WireDefault(0.U(32.W))
  val fetchInstr = WireDefault(0.U(32.W))
  val opcode = WireDefault(0.U(5.W))

  // Register update signals
  val pcNext = WireDefault(0.U(32.W))
  val fetchNext = WireDefault(false.B)

  // Registers
  val warp = RegInit(0.U(warpAddrLen.W))
  val fetch = RegInit(false.B)
  val valid = RegInit(false.B)
  val pc = RegInit(0.U(32.W))

  // ------------ First half of the pipeline ------------
  fetchNext := warpTable.io.done(io.scheduler.warp) === 0.U && !io.scheduler.reset && !io.scheduler.stall && !io.scheduler.setValid && !io.loadInstr.en && (opcode =/= "b11111".U)
  fetch := fetchNext

  pcNext := warpTable.io.pc(io.scheduler.warp) + 1.U
  pc := pcNext
  warp := io.scheduler.warp
  valid := !io.scheduler.stall

  warpTable.io.reset := io.scheduler.reset
  warpTable.io.validCtrl.set := io.scheduler.setValid
  warpTable.io.validCtrl.data := io.scheduler.validWarps
  warpTable.io.pcCtrl.update := fetchNext
  warpTable.io.pcCtrl.idx := io.scheduler.warp
  warpTable.io.pcCtrl.data := pcNext
  warpTable.io.doneCtrl.set := setDone
  warpTable.io.doneCtrl.idx := warp
  warpTable.io.pendingCtrlIssue.set := io.setPending
  warpTable.io.pendingCtrlIssue.idx := io.scheduler.warp
  warpTable.io.activeCtrl.set := io.wb.setInactive
  warpTable.io.activeCtrl.idx := io.wb.warp
  warpTable.io.pendingCtrlWb.set := io.wb.setNotPending
  warpTable.io.pendingCtrlWb.idx := io.wb.warp

  when(io.loadInstr.en) {
    instrAddr := io.loadInstr.addr
  }.otherwise(
    instrAddr := warpTable.io.pc(io.scheduler.warp)
  )

  // ------------ Second half of the pipeline ------------
  instructionCache.io.loadInstr := io.loadInstr.en
  instructionCache.io.instrAddr := instrAddr
  instructionCache.io.loadInstrData := io.loadInstr.instr
  instr := instructionCache.io.instr

  when(fetch) {
    fetchInstr := instr
  }.otherwise {
    fetchInstr := 0.U
  }

  opcode := instr(4, 0)

  // If the instruction opcode is RET, set the warp as done
  when(opcode === Opcodes.RET) {
    setDone := true.B
  }

  io.instrF.valid := valid
  io.instrF.warp := warp
  io.instrF.instr := fetchInstr
  io.instrF.pc := pc
  io.warpTable.valid := warpTable.io.valid
  io.warpTable.active := warpTable.io.active
  io.warpTable.pending := warpTable.io.pending
}
