package SM.Frontend

import chisel3._
import chisel3.util._
import SM.Frontend.SchedulerState._

class WarpScheduler extends Module {
  val io = IO(new Bundle {
    val valid = Input(UInt(4.W))
    val done = Input(UInt(4.W))
    val pending = Input(UInt(4.W))
    val headInstrType = Input(UInt(4.W))
    val memStall = Input(Bool())
    val aluStall = Input(Bool())
    val start = Input(Bool())

    val warpId = Output(UInt(2.W))
    val stall = Output(Bool())
    val ready = Output(Bool())
  })

  val availableWarps = WireDefault(0.U(4.W))
  val warpId = WireDefault(0.U(2.W))
  val stall = WireDefault(false.B)

  val stateReg = RegInit(idle)
  val readyReg = RegInit(true.B)

  // Calculate which warps can be scheduled
  when(io.memStall) {
    // If the memory pipeline is stalled, remove warps that have memory instructions in the head of their instruction queue
    availableWarps := ((~(io.done | io.pending)).asUInt & io.valid) & (~io.headInstrType).asUInt
  } .otherwise(
    availableWarps := (~(io.done | io.pending)).asUInt & io.valid
  )

  // If there are no available warps, stall the pipeline
  when(availableWarps.orR === 0.B) {
    stall := true.B
  }

  // Scheduler FSM
  switch(stateReg) {
    is(idle) {
      when(io.start === true.B) {
        stateReg := s0
        readyReg := false.B
      } .otherwise(
        readyReg := true.B
      )
    }
    is(done) {
      readyReg := true.B
      stateReg := idle
    }
    is(s0) {
      when(availableWarps(0) === 1.U) {
        warpId := 0.U
        stateReg := s0
      }.elsewhen(availableWarps(1) === 1.U) {
        warpId := 1.U
        stateReg := s1
      }.elsewhen(availableWarps(2) === 1.U) {
        warpId := 2.U
        stateReg := s2
      }.elsewhen(availableWarps(3) === 1.U) {
        warpId := 3.U
        stateReg := s3
      }
    }
    is(s1) {
      when(availableWarps(1) === 1.U) {
        warpId := 1.U
        stateReg := s1
      }.elsewhen(availableWarps(2) === 1.U) {
        warpId := 2.U
        stateReg := s2
      }.elsewhen(availableWarps(3) === 1.U) {
        warpId := 3.U
        stateReg := s3
      }.elsewhen(availableWarps(0) === 1.U) {
        warpId := 0.U
        stateReg := s0
      }
    }
    is(s2) {
      when(availableWarps(2) === 1.U) {
        warpId := 2.U
        stateReg := s2
      }.elsewhen(availableWarps(3) === 1.U) {
        warpId := 3.U
        stateReg := s3
      }.elsewhen(availableWarps(0) === 1.U) {
        warpId := 0.U
        stateReg := s0
      }.elsewhen(availableWarps(1) === 1.U) {
        warpId := 1.U
        stateReg := s1
      }
    }
    is(s3) {
      when(availableWarps(3) === 1.U) {
        warpId := 3.U
        stateReg := s3
      }.elsewhen(availableWarps(0) === 1.U) {
        warpId := 0.U
        stateReg := s0
      }.elsewhen(availableWarps(1) === 1.U) {
        warpId := 1.U
        stateReg := s1
      }.elsewhen(availableWarps(2) === 1.U) {
        warpId := 2.U
        stateReg := s2
      }
    }
  }

  io.warpId := warpId
  io.stall := stall
  io.ready := readyReg
}
