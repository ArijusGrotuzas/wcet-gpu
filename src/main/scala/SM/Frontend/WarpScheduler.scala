package SM.Frontend

import chisel3._
import chisel3.util._
import SM.Frontend.SchedulerState._

// TODO: Use the read/valid interface
class WarpScheduler extends Module {
  val io = IO(new Bundle {
    val start = Input(Bool())
    val valid = Input(UInt(4.W))
    val active = Input(UInt(4.W))
    val pending = Input(UInt(4.W))
    val headInstrType = Input(UInt(4.W))
    val memStall = Input(Bool())
    val aluStall = Input(Bool())

    val warp = Output(UInt(2.W))
    val stall = Output(Bool())
    val ready = Output(Bool())
  })

  val availableWarps = WireDefault(0.U(4.W))
  val warp = WireDefault(0.U(2.W))
  val ready = WireDefault(false.B)
  val stall = WireDefault(false.B)
  val stateReg = RegInit(idle)
  val allDone = WireDefault(false.B)

  // Calculate which warps can be scheduled
  when(io.memStall) {
    // If the memory pipeline is stalled, remove warps that have memory instructions in the head of their instruction queue
    availableWarps := ((io.active & (~io.pending).asUInt) & io.valid) & (~io.headInstrType).asUInt
  } .otherwise(
    availableWarps := (io.active & (~io.pending).asUInt) & io.valid
  )

  // If there are no available warps, stall the pipeline
  when(availableWarps.orR === 0.B) {
    stall := true.B
  }

  allDone := io.active.orR

  // Scheduler FSM
  switch(stateReg) {
    is(idle) {
      stall := true.B
      ready := true.B

      when(io.start === true.B) {
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
        stateReg := idle
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
        stateReg := idle
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
        stateReg := idle
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
        stateReg := idle
      }
    }
  }

  io.warp := warp
  io.stall := stall
  io.ready := ready
}
