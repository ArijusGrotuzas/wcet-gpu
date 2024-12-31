package SM.Frontend

import chisel3._

class WarpTable(warpCount: Int, addrLen: Int) extends Module {
  val io = IO(new Bundle {
    val reset = Input(Bool())
    
    val validCtrl = new Bundle{
      val set = Input(Bool())
      val data = Input(UInt(warpCount.W)) 
    }
    
    val pcCtrl = new Bundle {
      val update = Input(Bool())
      val idx = Input(UInt(warpCount.W))
      val data = Input(UInt(32.W))
    }
    
    val activeCtrl = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(warpCount.W))
    }
    
    val pendingCtrlIssue = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(warpCount.W))
    }

    val pendingCtrlWb = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(warpCount.W))
    }
    
    val doneCtrl = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(warpCount.W))
    }
    
    val done = Output(UInt(warpCount.W))
    val active = Output(UInt(warpCount.W))
    val pending = Output(UInt(warpCount.W))
    val valid = Output(UInt(warpCount.W))
    val pc = Output(Vec(warpCount, UInt(32.W)))
  })

  val doneReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val activeReg = RegInit(VecInit(Seq.fill(warpCount)(true.B)))
  val pendingReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val validReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val pcReg = RegInit(VecInit(Seq.fill(warpCount)(0.U(32.W))))

  when(io.validCtrl.set) {
    for (i <- 0 until warpCount) {
      validReg(i) := io.validCtrl.data(i)
    }
  }

  when (io.doneCtrl.set) {
    doneReg(io.doneCtrl.idx) := true.B
  }

  when((io.pendingCtrlWb.idx =/= io.pendingCtrlIssue.idx) || !(io.pendingCtrlWb.set && io.pendingCtrlIssue.set)) {
    when (io.pendingCtrlIssue.set) {
      pendingReg(io.pendingCtrlIssue.idx) := true.B
    }

    when (io.pendingCtrlWb.set) {
      pendingReg(io.pendingCtrlWb.idx) := false.B
    }
  }

  when (io.activeCtrl.set) {
    activeReg(io.activeCtrl.idx) := false.B
  }
  
  when(io.pcCtrl.update) {
    pcReg(io.pcCtrl.idx) := io.pcCtrl.data
  }

  // Rest the table entries
  when(io.reset) {
    pcReg := VecInit(Seq.fill(warpCount)(0.U))
    doneReg := VecInit(Seq.fill(warpCount)(false.B))
    activeReg := VecInit(Seq.fill(warpCount)(true.B))
    pendingReg := VecInit(Seq.fill(warpCount)(false.B))
    validReg := VecInit(Seq.fill(warpCount)(false.B))
  }

  io.done := doneReg.asUInt
  io.active := activeReg.asUInt
  io.pending := pendingReg.asUInt
  io.valid := validReg.asUInt
  io.pc := pcReg
}
