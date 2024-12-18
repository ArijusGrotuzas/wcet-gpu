package SM.Frontend

import chisel3._

class WarpTable(warpCount: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    val setValid = Input(Bool())
    val valid = Input(UInt(warpCount.W))
    val clear = Input(Bool())

    val update = Input(Bool())
    val warp = Input(UInt(addrLen.W))
    val newPc = Input(UInt(32.W))
    val toggleDone = Input(Bool())
    val togglePending = Input(Bool())
    val toggleActive = Input(Bool())
    val togglePendingWb = Input(Bool())
    val warpWb = Input(UInt(addrLen.W))

    val doneOut = Output(Vec(warpCount, Bool()))
    val activeOut = Output(Vec(warpCount, Bool()))
    val pendingOut = Output(Vec(warpCount, Bool()))
    val validOut = Output(Vec(warpCount, Bool()))
    val pcOut = Output(UInt(32.W))
  })

  val doneReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val activeReg = RegInit(VecInit(Seq.fill(warpCount)(true.B)))
  val pendingReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val validReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val pcReg = RegInit(VecInit(Seq.fill(warpCount)(0.U(32.W))))

  when(io.setValid) {
    for (i <- 0 until warpCount) {
      validReg(i) := io.valid(i)
    }
  }

  when(io.update) {
    when (io.toggleDone) {
      doneReg(io.warp) := ~doneReg(io.warp)
    }

    when (io.togglePending) {
      pendingReg(io.warp) := ~pendingReg(io.warp)
    }

    when (io.togglePendingWb) {
      pendingReg(io.warpWb) := ~pendingReg(io.warpWb)
    }

    when (io.toggleActive) {
      activeReg(io.warp) := ~activeReg(io.warp)
    }

    pcReg(io.warp) := io.newPc
  }

  // Clear the table entries
  when(io.clear) {
    pcReg := VecInit(Seq.fill(warpCount)(0.U))
    doneReg := VecInit(Seq.fill(warpCount)(false.B))
    activeReg := VecInit(Seq.fill(warpCount)(true.B))
    pendingReg := VecInit(Seq.fill(warpCount)(false.B))
    validReg := VecInit(Seq.fill(warpCount)(false.B))
  }

  io.doneOut := doneReg
  io.activeOut := activeReg
  io.pendingOut := pendingReg
  io.validOut := validReg
  io.pcOut := pcReg(io.warp)
}
