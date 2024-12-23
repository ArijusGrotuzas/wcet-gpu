package SM.Backend.VRF

import chisel3._

class Arbiter3To4 extends Module {
  val io = IO(new Bundle {
    val in_sel = Input(Vec(3, UInt(2.W))) // 3 input channels with 2-bit selectors
    val out_sel = Output(Vec(4, UInt(2.W))) // 4 output channels: select signals (which input)
  })

  val out_sel = VecInit(Seq.fill(4)(3.U(2.W)))

  // Fixed priority arbitrator
  for (out <- 0 until 4) {
    when(io.in_sel(0) === out.U) {
      out_sel(out) := 0.U // Input 0 requests this output
    }.elsewhen(io.in_sel(1) === out.U) {
      out_sel(out) := 1.U // Input 1 requests this output
    }.elsewhen(io.in_sel(2) === out.U) {
      out_sel(out) := 2.U // Input 2 requests this output
    }.otherwise {
      out_sel(out) := 3.U // No input requests this output
    }
  }

  io.out_sel := out_sel
}