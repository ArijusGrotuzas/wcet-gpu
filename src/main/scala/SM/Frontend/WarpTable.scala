package SM.Frontend

import chisel3._

class WarpTable(warpCount: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    val setValid = Input(Bool())
    val valid = Input(UInt(warpCount.W))
    val reset = Input(Bool())

    val warp = Input(UInt(addrLen.W))
    val newPc = Input(UInt(32.W))

    val setDone = Input(Bool())
    val setPending = Input(Bool())
    val setInactive = Input(Bool())
    val setNotPending = Input(Bool())
    val warpWb = Input(UInt(addrLen.W))

    val doneOut = Output(UInt(warpCount.W))
    val activeOut = Output(UInt(warpCount.W))
    val pendingOut = Output(UInt(warpCount.W))
    val validOut = Output(UInt(warpCount.W))
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

  when (io.setDone) {
    doneReg(io.warp) := true.B
  }

  when((io.warp =/= io.warpWb) && !(io.setPending && io.setNotPending)) {
    when (io.setPending) {
      pendingReg(io.warp) := true.B
    }

    when (io.setNotPending) {
      pendingReg(io.warpWb) := ~pendingReg(io.warpWb)
    }
  }

  when (io.setInactive) {
    activeReg(io.warp) := ~activeReg(io.warp)
  }

  pcReg(io.warp) := io.newPc

  // Rest the table entries
  when(io.reset) {
    pcReg := VecInit(Seq.fill(warpCount)(0.U))
    doneReg := VecInit(Seq.fill(warpCount)(false.B))
    activeReg := VecInit(Seq.fill(warpCount)(true.B))
    pendingReg := VecInit(Seq.fill(warpCount)(false.B))
    validReg := VecInit(Seq.fill(warpCount)(false.B))
  }

  io.doneOut := doneReg.asUInt
  io.activeOut := activeReg.asUInt
  io.pendingOut := pendingReg.asUInt
  io.validOut := validReg.asUInt
  io.pcOut := pcReg(io.warp)
}
