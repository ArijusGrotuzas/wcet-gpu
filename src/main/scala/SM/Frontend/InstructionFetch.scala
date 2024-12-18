package SM.Frontend

import chisel3._

class InstructionFetch(warpCount: Int) extends Module {
  val io = IO(new Bundle {
    val setValid = Input(Bool())
    val valid = Input(UInt(warpCount.W))
    val reset = Input(Bool())
    val start = Input(Bool())

    val memStall = Input(Bool())
    val aluStall = Input(Bool())
    val headInstrType = Input(UInt(warpCount.W))
    val issueSetPending = Input(Bool())
    val issueSetInactive = Input(Bool())
    val wbSetNotPending = Input(Bool())
    val wbWarp = Input(UInt(2.W))

    val warp = Output(UInt(2.W))
    val instr = Output(UInt(32.W))
    val ready = Output(Bool())
    val stall = Output(Bool())
  })

  val warpTable = Module(new WarpTable(4, 2))
  val warpScheduler = Module(new WarpScheduler()) // TODO: Configure the scheduler to support any number of warps
  val instructionCache = Module(new InstructionCache(32, 1024, 5))

  val instr = WireDefault(0.U(32.W))
  val warp = WireDefault(0.U(2.W))
  val ready = WireDefault(false.B)
  val stall = WireDefault(false.B)
  val newPc = WireDefault(0.U(32.W))
  val done = WireDefault(false.B)

  // ----------------- Instruction Cache Assignments -----------------
  // TODO: Set signals for loading instruction memory
  instructionCache.io.wEn := false.B
  instructionCache.io.addr := warpTable.io.pcOut
  instructionCache.io.loadInstr := 0.U

  // ----------------- Warp Table Assignments -----------------
  warpTable.io.setValid := io.setValid
  warpTable.io.valid := io.valid
  warpTable.io.reset := io.reset

  warpTable.io.warp := warp
  warpTable.io.newPc := newPc

  warpTable.io.setDone := done
  warpTable.io.setPending := io.issueSetPending
  warpTable.io.setInactive := io.issueSetInactive
  warpTable.io.setNotPending := io.wbSetNotPending
  warpTable.io.warpWb := io.wbWarp

  // ----------------- Warp Scheduler Assignments -----------------
  warpScheduler.io.valid := warpTable.io.validOut
  warpScheduler.io.active := warpTable.io.activeOut
  warpScheduler.io.pending := warpTable.io.pendingOut
  warpScheduler.io.headInstrType := io.headInstrType
  warpScheduler.io.memStall := io.memStall
  warpScheduler.io.aluStall := io.aluStall
  warpScheduler.io.start := io.start

  warp := warpScheduler.io.warpId
  stall := warpScheduler.io.stall
  ready := warpScheduler.io.ready

  // If the warp is done, we just insert NOPs
  when(warpTable.io.doneOut(warp) === 0.U || !stall) {
    instr := instructionCache.io.instr
    newPc := warpTable.io.pcOut + 1.U
  } .otherwise{
    instr := 0.U
    newPc := warpTable.io.pcOut
  }

  // If the instruction opcode is RET, set the warp as done
  when(io.instr(4, 0) === "b1111".U) {
    done := true.B
  }

  io.warp := warp
  io.instr := instr
  io.ready := ready
  io.stall := stall
}
