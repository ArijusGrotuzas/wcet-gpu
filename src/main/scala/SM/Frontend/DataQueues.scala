package SM.Frontend

import chisel3._
import chisel.lib.fifo._

class DataQueues[T <: Data](gen: T, queueCount: Int, queueDepth: Int) extends Module {
  val io = IO(new Bundle{
    val dataIn = Input(gen)
    val inQueueSel = Input(UInt(queueCount.W))
    val outQueueSel = Input(UInt(queueCount.W))
    val outDataSel = Input(UInt(queueCount.W))

    val dataOut = Output(gen)
  })

  val queues = Array.fill(queueCount)(Module(new RegFifo(gen, queueDepth)))
  val outBits = VecInit(queues.toSeq.map(_.io.deq.bits))
  val notEmpty = VecInit(queues.toSeq.map(_.io.deq.valid))
  val dataOut = Wire(gen)

  // Opcode queues
  for (i <- 0 until queueCount) {
    queues(i).io.enq.bits := io.dataIn
    queues(i).io.enq.valid := io.inQueueSel(i)
    queues(i).io.deq.ready := io.outQueueSel(i)
  }

  // If the selected queue is empty, then return 0
  when(notEmpty(io.outDataSel)) {
    dataOut := outBits(io.outDataSel)
  }.otherwise {
    dataOut := 0.U.asTypeOf(gen)
  }

  io.dataOut := dataOut
}