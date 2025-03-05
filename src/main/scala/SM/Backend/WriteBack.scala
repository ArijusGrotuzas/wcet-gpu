package SM.Backend

import chisel3._
import chisel3.util._

/** The write-back stage forwards the result of the ALU pipeline if there are no
 * memory results ready. In other words, the write-back stage prioritizes storing
 * the results from memory operations over the results from ALU operations. If both
 * alu and memory results are ready at the same time, the write-back will push the
 * alu result to a queue and forward the memory result to the register. The next
 * time there are no memory results ready, the write-back will look first if the alu queue
 * contains any entries and if so, it will store the results from the queue to the register file, otherwise
 * it will store the results coming directly from the alu pipeline.
 */
class WriteBack(warpCount: Int, warpSize: Int) extends Module {
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val alu = new Bundle {
      val we = Input(Bool())
      val threadMask = Input(UInt(warpSize.W))
      val warp = Input(UInt(warpAddrLen.W))
      val done = Input(Bool())
      val dest = Input(UInt(5.W))
      val out = Input(UInt((warpSize * 32).W))
    }

    val mem = new Bundle {
      val we = Input(Bool())
      val threadMask = Input(UInt(warpSize.W))
      val warp = Input(UInt(warpAddrLen.W))
      val dest = Input(UInt(5.W))
      val out = Input(UInt((warpSize * 32).W))
    }

    val wbOf = new Bundle {
      val we = Output(Bool())
      val warp = Output(UInt(warpAddrLen.W))
      val writeAddr = Output(UInt(5.W))
      val writeMask = Output(UInt(warpSize.W))
      val writeData = Output(UInt((warpSize * 32).W))
    }

    val wbIfCtrl = new Bundle {
      val warp = Output(UInt(warpAddrLen.W))
      val setInactive = Output(Bool())
    }

    val wbStall = Output(Bool())
    val outTest = Output(UInt((warpSize * 32).W))
  })

  val sForwardAlu :: sDelayAlu :: Nil = Enum(2)
  val aluDelay = RegInit(sForwardAlu)

  val setInactive = WireDefault(false.B)
  val setNotPending = WireDefault(false.B)

  val aluThreadMaskDelay = RegNext(io.alu.threadMask)
  val aluWarpDelay = RegNext(io.alu.warp)
  val aluWeDelay = RegNext(io.alu.we)
  val aluDestDelay = RegNext(io.alu.dest)
  val aluOutDelay = RegNext(io.alu.out)
  val aluDoneDelay = RegNext(io.alu.done)

  val outWe = WireDefault(false.B)
  val outWriteMask = WireDefault(0.U(warpSize.W))
  val outAddr = WireDefault(0.U(5.W))
  val outWarp = WireDefault(0.U(warpAddrLen.W))
  val outData = WireDefault(0.U((warpSize * 32).W))
  val outInactive = WireDefault(false.B)
  val stallScheduler = WireDefault(false.B)

  // Update the alu output delay state register
  switch(aluDelay) {
    is(sForwardAlu) {
      when(io.mem.we) {
        aluDelay := sDelayAlu
      }
    }
    is(sDelayAlu) {
      when(!io.alu.we) {
        aluDelay := sForwardAlu
      }
    }
  }

  when(io.mem.we) { // Send the memory result to the register file
    outWe := io.mem.we
    outWriteMask := io.mem.threadMask
    outAddr := io.mem.dest
    outData := io.mem.out
    outWarp := io.mem.warp
    stallScheduler := true.B
  }.otherwise { // Send the ALU result to the register file
    when(aluDelay === sDelayAlu) { // Take the delayed values of the ALU
      outWe := aluWeDelay
      outWriteMask := aluThreadMaskDelay
      outAddr := aluDestDelay
      outData := aluOutDelay
      outWarp := aluWarpDelay
      outInactive := aluDoneDelay
    }.otherwise {
      outWe := io.alu.we
      outWriteMask := io.alu.threadMask
      outAddr := io.alu.dest
      outData := io.alu.out
      outWarp := io.alu.warp
      outInactive := io.alu.done
    }
  }

  io.wbOf.we := outWe
  io.wbOf.warp := outWarp
  io.wbOf.writeAddr := outAddr
  io.wbOf.writeData := outData
  io.wbOf.writeMask := outWriteMask
  io.wbIfCtrl.warp := outWarp
  io.wbIfCtrl.setInactive := outInactive
  io.wbStall := stallScheduler
  // Test Output
  io.outTest := outData
}
