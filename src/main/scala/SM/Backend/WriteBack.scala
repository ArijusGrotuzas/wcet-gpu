package SM.Backend

import chisel3._
import chisel3.util.Enum

/** The write-back stage forwards the result of the ALU pipeline if there are no
 * memory results ready. In other words, the write-back stage prioritizes storing
 * the results from memory operations over the results from ALU operations. If both
 * alu and memory results are ready at the same time, the write-back will push the
 * alu result to a queue and forward the memory result to the register. The next
 * time there are no memory results ready, the write-back will look first if the alu queue
 * contains any entries and if so, it will store the results from the queue to the register file, otherwise
 * it will store the results coming directly from the alu pipeline.
 */
class WriteBack(warpSize: Int) extends Module {
  val io = IO(new Bundle {
    val alu = new Bundle {
      val warp = Input(UInt(2.W))
      val valid = Input(Bool())
      val done = Input(Bool())
      val dest = Input(UInt(5.W))
      val out = Input(UInt((warpSize * 32).W))
    }

    val mem = new Bundle {
      val warp = Input(UInt(2.W))
      val valid = Input(Bool())
      val pending = Input(Bool())
      val dest = Input(UInt(5.W))
      val out = Input(UInt((warpSize * 32).W))
    }

    val wbOf = new Bundle {
      val we = Output(Bool())
      val writeAddr = Output(UInt(5.W))
      val writeMask = Output(UInt(4.W))
      val writeData = Output(UInt((warpSize * 32).W))
    }

    val wbIf = new Bundle {
      val warp = Output(UInt(2.W))
      val setInactive = Output(Bool())
      val setNotPending = Output(Bool())
    }
  })
  // Alu delay state register
  val aluDelay = RegInit(false.B)

  val setInactive = WireDefault(false.B)
  val setNotPending = WireDefault(false.B)

  val aluWarpDelay = RegNext(io.alu.warp)
  val aluValidDelay = RegNext(io.alu.valid)
  val aluDestDelay = RegNext(io.alu.dest)
  val aluOutDelay = RegNext(io.alu.out)

  val outWe = WireDefault(false.B)
  val outAddr = WireDefault(0.U(5.W))
  val outWarp = WireDefault(0.U(2.W))
  val outData = WireDefault(0.U((warpSize * 32).W))

  // Update the alu output delay state register
  when(io.mem.valid && !aluDelay) {
    aluDelay := true.B
  } .elsewhen(!io.alu.valid && aluDelay) {
    aluDelay := false.B
  }

  when(io.mem.valid) { // Send the memory result to the register file
    outWe := io.mem.valid
    outAddr := io.mem.dest
    outData := io.mem.out
    outWarp := io.mem.warp
  } .otherwise { // Send the ALU result to the register file
      when(aluDelay) { // Take the delayed values of the ALU
        outWe := aluValidDelay
        outAddr := aluDestDelay
        outData := aluOutDelay
        outWarp := aluWarpDelay
      } .otherwise {
        outWe := io.alu.valid
        outAddr := io.alu.dest
        outData := io.alu.out
        outWarp := io.alu.warp
      }
  }

  when(io.mem.pending && io.mem.valid) {
    setNotPending := true.B
  }

  io.wbOf.we := outWe
  io.wbOf.writeAddr := outAddr
  io.wbOf.writeData := outData
  io.wbOf.writeMask := 0.U

  io.wbIf.warp := outWarp
  io.wbIf.setNotPending := setNotPending
  io.wbIf.setInactive := io.alu.done
}
