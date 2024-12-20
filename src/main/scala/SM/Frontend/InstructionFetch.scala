package SM.Frontend

import chisel3._

class InstructionFetch(warpCount: Int) extends Module {
  val io = IO(new Bundle {
    val reset = Input(Bool())
    val setValid = Input(Bool())
    val stall = Input(Bool())
    val loadInstr = Input(Bool())

    val setValidData = Input(UInt(warpCount.W))
    val loadInstrData = Input(UInt(32.W))
    val loadInstrAddr = Input(UInt(32.W))

    val fetchWarp = Input(UInt(2.W))
    val issueSetPending = Input(Bool())
    val issueSetInactive = Input(Bool())
    val wbSetNotPending = Input(Bool())
    val wbWarp = Input(UInt(2.W))

    val warpDec = Output(UInt(2.W))
    val instrDec = Output(UInt(32.W))
    val valid = Output(UInt(4.W))
    val active = Output(UInt(4.W))
    val pending = Output(UInt(4.W))
  })

  val warpTable = Module(new WarpTable(4, 2))
  val instructionCache = Module(new InstructionCache(32, 1024, 5))

  val pcNext = WireDefault(0.U(32.W))
  val warp = RegInit(0.U(2.W))
  val setDone = WireDefault(false.B)
  val fetch = RegInit(false.B)
  val fetchNext = WireDefault(false.B)
  val instr = WireDefault(0.U(32.W))
  val instrAddr = WireDefault(0.U(32.W))
  val fetchInstr = WireDefault(0.U(32.W))
  val opcode = WireDefault(0.U(5.W))

  // ------------ First half of the pipeline ------------
  fetchNext := warpTable.io.done(io.fetchWarp) === 0.U && !io.reset && !io.stall && !io.setValid && !io.loadInstr && (opcode =/= "b11111".U)
  fetch := fetchNext

  pcNext := warpTable.io.pc(io.fetchWarp) + 1.U
  warp := io.fetchWarp

  warpTable.io.reset := io.reset
  warpTable.io.validCtrl.set := io.setValid
  warpTable.io.validCtrl.data := io.setValidData
  warpTable.io.pcCtrl.update := fetchNext
  warpTable.io.pcCtrl.idx := io.fetchWarp
  warpTable.io.pcCtrl.data := pcNext
  warpTable.io.doneCtrl.set := setDone
  warpTable.io.doneCtrl.idx := warp
  warpTable.io.pendingCtrlIssue.set := io.issueSetPending
  warpTable.io.pendingCtrlIssue.idx := io.fetchWarp
  warpTable.io.activeCtrl.set := io.issueSetInactive
  warpTable.io.activeCtrl.idx := io.fetchWarp
  warpTable.io.pendingCtrlWb.set := io.wbSetNotPending
  warpTable.io.pendingCtrlWb.idx := io.wbWarp

  when(io.loadInstr) {
    instrAddr := io.loadInstrAddr
  } .otherwise(
    instrAddr := warpTable.io.pc(io.fetchWarp)
  )

  // ------------ Second half of the pipeline ------------
  instructionCache.io.loadInstr := io.loadInstr
  instructionCache.io.instrAddr := instrAddr
  instructionCache.io.loadInstrData := io.loadInstrData
  instr := instructionCache.io.instr

  when(fetch) {
    fetchInstr := instr
  } .otherwise {
    fetchInstr := 0.U
  }

  opcode := instr(4, 0)

  // If the instruction opcode is RET, set the warp as done
  when(opcode === "b11111".U) {
    setDone := true.B
  }

  io.warpDec := warp
  io.instrDec := fetchInstr
  io.valid := warpTable.io.valid
  io.active := warpTable.io.active
  io.pending := warpTable.io.pending
}
