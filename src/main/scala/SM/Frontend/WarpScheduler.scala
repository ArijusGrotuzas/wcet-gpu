package SM.Frontend

import chisel3._
import chisel3.util._

class WarpScheduler(blockCount: Int, warpCount: Int) extends Module {
  val blockAddrLen = log2Up(blockCount)
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val start = new Bundle {
      val valid = Input(Bool())
      val data = Input(UInt((blockAddrLen + warpCount).W))
      val ready = Output(Bool())
    }

    val warpTableStatus = new Bundle {
      val valid = Input(UInt(warpCount.W))
      val active = Input(UInt(warpCount.W))
      val pending = Input(UInt(warpCount.W))
    }

    val headInstrType = Input(UInt(warpCount.W))
    val memStall = Input(Bool())

    val scheduler = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val stall = Output(Bool())
      val reset = Output(Bool())
      val setValid = Output(Bool())
      val setValidWarps = Output(UInt(warpCount.W))
    }

    val aluInitCtrl = new Bundle {
      val setBlockIdx = Output(Bool())
      val blockIdx = Output(UInt(blockAddrLen.W))
    }
  })

  val sIdle :: sDone :: s0 :: s1 :: s2 :: s3 :: Nil = Enum(6)
  val stateReg = RegInit(sIdle)

  val availableWarps = WireDefault(0.U(warpCount.W))
  val warp = WireDefault(0.U(warpAddrLen.W))
  val ready = WireDefault(false.B)
  val stall = WireDefault(false.B)
  val allDone = WireDefault(false.B)
  val rst = WireDefault(false.B)
  val setValid = WireDefault(false.B)
  val setValidWarps = WireDefault(0.U(warpCount.W))
  val setBlockIdx = WireDefault(false.B)
  val blockIdx = WireDefault(0.U(blockAddrLen.W))

  // Calculate which warps can be scheduled
  when(io.memStall) {
    // If the memory pipeline is stalled, remove warps that have memory instructions in the head of their instruction queue
    availableWarps := ((io.warpTableStatus.active & (~io.warpTableStatus.pending).asUInt) & io.warpTableStatus.valid) & (~io.headInstrType).asUInt
  }.otherwise(
    availableWarps := (io.warpTableStatus.active & (~io.warpTableStatus.pending).asUInt) & io.warpTableStatus.valid
  )

  // If there are no available warps, stall the pipeline
  when(availableWarps.orR === 0.B) {
    stall := true.B
  }

  allDone := !(io.warpTableStatus.valid & io.warpTableStatus.active).orR

  // Scheduler FSM
  switch(stateReg) {
    is(sIdle) {
      stall := true.B
      ready := true.B

      when(io.start.valid === true.B) {
        setValid := true.B
        setBlockIdx := io.start.data(true.B)
        setValidWarps := io.start.data(warpCount - 1, 0)
        blockIdx := io.start.data((blockAddrLen + warpCount) - 1, warpCount)
        stateReg := s0
      }
    }
    is(s0) {
      when(availableWarps(0) === 1.U) {
        warp := 0.U
        stateReg := s0
      }.elsewhen(availableWarps(1) === 1.U) {
        warp := 1.U
        stateReg := s1
      }.elsewhen(availableWarps(2) === 1.U) {
        warp := 2.U
        stateReg := s2
      }.elsewhen(availableWarps(3) === 1.U) {
        warp := 3.U
        stateReg := s3
      }.elsewhen(allDone) {
        stateReg := sDone
      }
    }
    is(s1) {
      when(availableWarps(1) === 1.U) {
        warp := 1.U
        stateReg := s1
      }.elsewhen(availableWarps(2) === 1.U) {
        warp := 2.U
        stateReg := s2
      }.elsewhen(availableWarps(3) === 1.U) {
        warp := 3.U
        stateReg := s3
      }.elsewhen(availableWarps(0) === 1.U) {
        warp := 0.U
        stateReg := s0
      }.elsewhen(allDone) {
        stateReg := sDone
      }
    }
    is(s2) {
      when(availableWarps(2) === 1.U) {
        warp := 2.U
        stateReg := s2
      }.elsewhen(availableWarps(3) === 1.U) {
        warp := 3.U
        stateReg := s3
      }.elsewhen(availableWarps(0) === 1.U) {
        warp := 0.U
        stateReg := s0
      }.elsewhen(availableWarps(1) === 1.U) {
        warp := 1.U
        stateReg := s1
      }.elsewhen(allDone) {
        stateReg := sDone
      }
    }
    is(s3) {
      when(availableWarps(3) === 1.U) {
        warp := 3.U
        stateReg := s3
      }.elsewhen(availableWarps(0) === 1.U) {
        warp := 0.U
        stateReg := s0
      }.elsewhen(availableWarps(1) === 1.U) {
        warp := 1.U
        stateReg := s1
      }.elsewhen(availableWarps(2) === 1.U) {
        warp := 2.U
        stateReg := s2
      }.elsewhen(allDone) {
        stateReg := sDone
      }
    }
    is(sDone) {
      stall := true.B
      rst := true.B

      when(io.start.valid === false.B) {
        stateReg := sIdle
      }
    }
  }

  io.start.ready := ready
  io.scheduler.warp := warp
  io.scheduler.stall := stall
  io.scheduler.reset := rst
  io.scheduler.setValid := setValid
  io.scheduler.setValidWarps := setValidWarps
  io.aluInitCtrl.setBlockIdx := setBlockIdx
  io.aluInitCtrl.blockIdx := blockIdx
}
