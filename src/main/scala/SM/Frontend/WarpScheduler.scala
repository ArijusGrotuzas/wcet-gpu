package SM.Frontend

import chisel3._
import chisel3.util._

/**
 * Loose round-robin warp scheduler. The scheduler will output which warp should an instruction be fetched and issued for
 *
 * @param blockCount Number of blocks the GPU can support
 * @param warpCount Number of warps the SM can support
 */
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

  val sIdle :: sScheduling :: sDone :: Nil = Enum(3)
  val previousWarp = RegInit(0.U(warpAddrLen.W))
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

  // Signals for selecting correct warps
  val encoderValid = WireDefault(false.B)
  val encoderSel = WireDefault(false.B)
  val upperMaskedAvailWarps = availableWarps & (~((1.U << previousWarp).asUInt - 1.U)).asUInt

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
        stateReg := sScheduling
      }
    }
    is(sScheduling) {
      when(upperMaskedAvailWarps.orR) {
        encoderValid := true.B
        encoderSel := true.B
        previousWarp := warp
      } .elsewhen(availableWarps.orR) {
        encoderValid := true.B
        encoderSel := false.B
        previousWarp := warp
      } .elsewhen(allDone) {
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

  warp := Mux(encoderValid, PriorityEncoder(Mux(encoderSel, upperMaskedAvailWarps, availableWarps)), 0.U)

  io.start.ready := ready
  io.scheduler.warp := warp
  io.scheduler.stall := stall
  io.scheduler.reset := rst
  io.scheduler.setValid := setValid
  io.scheduler.setValidWarps := setValidWarps
  io.aluInitCtrl.setBlockIdx := setBlockIdx
  io.aluInitCtrl.blockIdx := blockIdx
}
