package SM.Frontend

import chisel3._
import chisel3.util._

object SchedulerState extends ChiselEnum {
  val idle, done, s0, s1, s2, s3 = Value
}

class WarpScheduler(warpCount: Int, warpAddrLen: Int) extends Module {
  val io = IO(new Bundle {
    val start = new Bundle {
      val ready = Output(Bool())
      val valid = Input(Bool())
      val data = Input(UInt(warpCount.W))
    }

    val warpTable = new Bundle {
      val valid = Input(UInt(warpCount.W))
      val active = Input(UInt(warpCount.W))
      val pending = Input(UInt(warpCount.W))
    }

    val headInstrType = Input(UInt(warpCount.W))
    val memStall = Input(Bool())
    val aluStall = Input(Bool())

    val scheduler = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val stall = Output(Bool())
      val reset = Output(Bool())
      val setValid = Output(Bool())
      val validWarps = Output(UInt(warpCount.W))
    }
  })

  import SchedulerState._

  val availableWarps = WireDefault(0.U(warpCount.W))
  val warp = WireDefault(0.U(warpAddrLen.W))
  val ready = WireDefault(false.B)
  val stall = WireDefault(false.B)
  val allDone = WireDefault(false.B)
  val rst = WireDefault(false.B)
  val setValid = WireDefault(false.B)
  val validWarps = WireDefault(0.U(warpCount.W))

  val stateReg = RegInit(idle)

  // Calculate which warps can be scheduled
  when(io.memStall) {
    // If the memory pipeline is stalled, remove warps that have memory instructions in the head of their instruction queue
    availableWarps := ((io.warpTable.active & (~io.warpTable.pending).asUInt) & io.warpTable.valid) & (~io.headInstrType).asUInt
  }.otherwise(
    availableWarps := (io.warpTable.active & (~io.warpTable.pending).asUInt) & io.warpTable.valid
  )

  // If there are no available warps, stall the pipeline
  when(availableWarps.orR === 0.B) {
    stall := true.B
  }

  allDone := !(io.warpTable.valid & io.warpTable.active).orR

  // Scheduler FSM
  switch(stateReg) {
    is(idle) {
      stall := true.B
      ready := true.B

      when(io.start.valid === true.B) {
        setValid := true.B
        validWarps := io.start.data
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
        stateReg := done
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
        stateReg := done
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
        stateReg := done
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
        stateReg := done
      }
    }
    is(done) {
      stall := true.B
      rst := true.B
      stateReg := idle
    }
  }

  io.scheduler.warp := warp
  io.scheduler.stall := stall
  io.scheduler.reset := rst
  io.scheduler.setValid := setValid
  io.scheduler.validWarps := validWarps
  io.start.ready := ready
}
