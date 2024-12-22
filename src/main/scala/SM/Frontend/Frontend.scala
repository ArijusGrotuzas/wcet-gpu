package SM.Frontend

import chisel3._

class Frontend(warpCount: Int) extends Module {
  val io = IO(new Bundle{
    val loadInstr = new Bundle{
      val en = Input(Bool())
      val instr = Input(UInt(32.W))
      val addr = Input(UInt(32.W))
    }

    val start = new Bundle{
      val ready = Output(Bool())
      val valid = Input(Bool())
      val data = Input(UInt(warpCount.W))
    }

    val wb = new Bundle{
      val setNotPending = Input(Bool())
      val setInactive = Input(Bool())
      val warp = Input(UInt(2.W))
    }

    val memStall = Input(Bool())
    val aluStall = Input(Bool())

    val front = new Bundle{
      // val pc = Output(UInt(32.W))
      val warp = Output(UInt(warpCount.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val imm = Output(UInt(22.W))
    }
  })

  val instrF = Module(new InstructionFetch(4, 2))
  val instrD = Module(new InstructionDecode(2))
  val instrIss = Module(new InstructionIssue(4, 2))
  val warpScheduler = Module(new WarpScheduler(4, 2))

  warpScheduler.io.start <> io.start
  warpScheduler.io.warpTable <> instrF.io.warpTable
  warpScheduler.io.memStall := io.memStall
  warpScheduler.io.aluStall := io.aluStall
  warpScheduler.io.headInstrType := 0.U //instrIss.io.headInstrType
  warpScheduler.io.scheduler <> instrF.io.scheduler

  instrF.io.loadInstr <> io.loadInstr
  instrF.io.setPending := instrIss.io.setPending
  instrF.io.wb.setInactive := io.wb.setInactive
  instrF.io.wb <> io.wb

  instrD.io.instrF.valid := RegNext(instrF.io.instrF.valid, false.B)
  instrD.io.instrF.pc := RegNext(instrF.io.instrF.pc, 0.U)
  instrD.io.instrF.instr := RegNext(instrF.io.instrF.instr, 0.U)
  instrD.io.instrF.warp := RegNext(instrF.io.instrF.warp, 0.U)

  instrIss.io.id.valid := RegNext(instrD.io.id.valid, false.B)
  instrIss.io.id.pc := RegNext(instrD.io.id.pc, 0.U)
  instrIss.io.id.warp := RegNext(instrD.io.id.warp, 0.U)
  instrIss.io.id.opcode := RegNext(instrD.io.id.opcode, 0.U)
  instrIss.io.id.dest := RegNext(instrD.io.id.dest, 0.U)
  instrIss.io.id.rs1 := RegNext(instrD.io.id.rs1, 0.U)
  instrIss.io.id.rs2 := RegNext(instrD.io.id.rs2, 0.U)
  instrIss.io.id.rs3 := RegNext(instrD.io.id.rs3, 0.U)
  instrIss.io.id.imm := RegNext(instrD.io.id.imm, 0.U)
  instrIss.io.scheduler.warp := warpScheduler.io.scheduler.warp
  instrIss.io.scheduler.stall := warpScheduler.io.scheduler.stall

  io.front.warp := RegNext(instrIss.io.iss.warp, 0.U)
  io.front.opcode := RegNext(instrIss.io.iss.opcode, 0.U)
  io.front.dest := RegNext(instrIss.io.iss.dest, 0.U)
  io.front.rs1 := RegNext(instrIss.io.iss.rs1, 0.U)
  io.front.rs2 := RegNext(instrIss.io.iss.rs2, 0.U)
  io.front.rs3 := RegNext(instrIss.io.iss.rs3, 0.U)
  io.front.imm := RegNext(instrIss.io.iss.imm, 0.U)
}
