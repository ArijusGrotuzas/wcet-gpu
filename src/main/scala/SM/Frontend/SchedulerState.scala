package SM.Frontend

import chisel3._
import chisel3.util._

object SchedulerState extends ChiselEnum {
  val idle, s0, s1, s2, s3 = Value
}
