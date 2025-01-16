package SM.Frontend

import chisel3._
import chisel3.util._
import scala.math.pow

/**
 * Warp table contains entries for each warp, where each entry holds the following information:
 * - Done: Whether the warp has fetched a ret instruction
 * - Active: Whether the warp still has some fetched and not yet finished instructions or un-fetched instructions
 * - Pending: Whether the warp is pending, i.e. if variable latency instruction has been issued and this warp is waiting for its completion
 * - Valid: Whether the warp was set valid by a block scheduler
 * - PC: Current program counter of the warp
 * - Thread masks: Active thread masks of the warp
 *
 * @param warpCount Number of warp entries the table should hold
 * @param warpSize Number of threads in a warp, used to initialize the thread masks
 */
class WarpTable(warpCount: Int, warpSize: Int) extends Module {
  val addrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val reset = Input(Bool())
    
    val validCtrl = new Bundle{
      val set = Input(Bool())
      val data = Input(UInt(warpCount.W)) 
    }
    
    val pcCtrl = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(addrLen.W))
      val data = Input(UInt(32.W))
    }
    
    val activeCtrl = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(addrLen.W))
    }
    
    val setPendingCtrl = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(addrLen.W))
    }

    val setNotPendingCtrl = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(addrLen.W))
    }
    
    val doneCtrl = new Bundle {
      val set = Input(Bool())
      val idx = Input(UInt(addrLen.W))
    }
    
    val done = Output(UInt(warpCount.W))
    val active = Output(UInt(warpCount.W))
    val pending = Output(UInt(warpCount.W))
    val valid = Output(UInt(warpCount.W))
    val pc = Output(Vec(warpCount, UInt(32.W)))
    val threadMasks = Output(Vec(warpCount, UInt(warpSize.W)))
  })

  val doneReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val activeReg = RegInit(VecInit(Seq.fill(warpCount)(true.B)))
  val pendingReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val validReg = RegInit(VecInit(Seq.fill(warpCount)(false.B)))
  val pcReg = RegInit(VecInit(Seq.fill(warpCount)(0.U(32.W))))
  val threadMasks = RegInit(VecInit(Seq.fill(warpCount)((pow(2.toDouble, warpSize.toDouble).toInt - 1).U(warpSize.W))))

  when(io.validCtrl.set) {
    for (i <- 0 until warpCount) {
      validReg(i) := io.validCtrl.data(i)
    }
  }

  when (io.doneCtrl.set) {
    doneReg(io.doneCtrl.idx) := true.B
  }

  // If attempting to set the same warp as pending and not pending, do not update the table
  // This technically should not happen, but this is just a safety measure
  when((io.setNotPendingCtrl.idx =/= io.setPendingCtrl.idx) || !(io.setNotPendingCtrl.set && io.setPendingCtrl.set)) {
    when (io.setPendingCtrl.set) {
      pendingReg(io.setPendingCtrl.idx) := true.B
    }

    when (io.setNotPendingCtrl.set) {
      pendingReg(io.setNotPendingCtrl.idx) := false.B
    }
  }

  when (io.activeCtrl.set) {
    activeReg(io.activeCtrl.idx) := false.B
  }
  
  when(io.pcCtrl.set) {
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
  io.threadMasks := threadMasks
}
