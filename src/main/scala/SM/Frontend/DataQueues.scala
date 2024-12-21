package SM.Frontend

import chisel3._
import chisel.lib.fifo.DoubleBufferFifo

class DataQueues(queueCount: Int, queueDepth: Int, dataLen: Int) extends Module {
  val io = IO(new Bundle{
    val dataIn = Input(UInt(dataLen.W))
    val inQueueSel = Input(UInt(queueCount.W))
    val outQueueSel = Input(UInt(queueCount.W))
    val outDataSel = Input(UInt(queueCount.W))

    val dataOut = Output(UInt(dataLen.W))
  })

  val queues = Array.fill(queueCount)(Module(new DoubleBufferFifo(UInt(dataLen.W), queueDepth)))
  val outBits = VecInit(queues.toSeq.map(_.io.deq.bits))
  val empty = VecInit(queues.toSeq.map(_.io.deq.valid))
  val dataOut = WireDefault(0.U(dataLen.W))

  // Opcode queues
  for (i <- 0 until queueCount) {
    queues(i).io.enq.bits := io.dataIn
    queues(i).io.enq.valid := io.inQueueSel(i)
    queues(i).io.deq.ready := io.outQueueSel(i)
  }

  // If the selected queue is empty, then return 0
  when(empty(io.outDataSel)) {
    dataOut := outBits(io.outDataSel)
  }.otherwise {
    dataOut := 0.U
  }

  io.dataOut := dataOut
}