package SM.Frontend

import chisel3._
import chisel3.util._

class Front(blockCount: Int, warpCount: Int, warpSize: Int) extends Module {
  val blockAddrLen = log2Up(blockCount)
  val warpAddrLen = log2Up(warpCount)
  val io = IO(new Bundle {
    val wbIfCtrl = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val setInactive = Input(Bool())
    }

    val memIfCtrl = new Bundle {
      val warp = Input(UInt(warpAddrLen.W))
      val setNotPending = Input(Bool())
    }

    val nzpUpdateCtrl = new Bundle {
      val en = Input(Bool())
      val nzp = Input(UInt((3 * warpSize).W))
      val warp = Input(UInt(warpAddrLen.W))
    }

    val memStall = Input(Bool())

    val instrMem = new Bundle {
      val data = Input(UInt(32.W))
      val addr = Output(UInt(32.W))
    }

    val start = new Bundle {
      val valid = Input(Bool())
      val data = Input(UInt((blockAddrLen + warpCount).W))
      val ready = Output(Bool())
    }

    val front = new Bundle {
      // val pc = Output(UInt(32.W))
      val warp = Output(UInt(warpAddrLen.W))
      val opcode = Output(UInt(5.W))
      val dest = Output(UInt(5.W))
      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))
      val rs3 = Output(UInt(5.W))
      val srs = Output(UInt(3.W))
      val imm = Output(SInt(32.W))
    }

    val aluInitCtrl = new Bundle {
      val setBlockIdx = Output(Bool())
      val blockIdx = Output(UInt(blockAddrLen.W))
    }
  })

  val instrF = Module(new InstructionFetch(warpCount, warpSize))
  val instrD = Module(new InstructionDecode(warpCount))
  val instrIss = Module(new InstructionIssue(warpCount, warpSize))
  val warpScheduler = Module(new WarpScheduler(blockCount, warpCount))

  // Control signals to and from warp scheduler
  warpScheduler.io.start <> io.start
  warpScheduler.io.memStall := io.memStall
  warpScheduler.io.scheduler <> instrF.io.scheduler
  warpScheduler.io.warpTableStatus <> instrF.io.warpTableStatus
  warpScheduler.io.headInstrType := instrIss.io.headInstrType

  // Control signals to and from the instruction fetch stage
  instrF.io.setPending := instrIss.io.setPending
  instrF.io.instrMem <> io.instrMem
  instrF.io.wbIfCtrl <> io.wbIfCtrl
  instrF.io.memIfCtrl <> io.memIfCtrl
  instrF.io.nzpUpdateCtrl <> io.nzpUpdateCtrl

  // Pipeline register between IF and ID
  instrD.io.instrF.valid := RegNext(instrF.io.instrF.valid, false.B)
  instrD.io.instrF.pc := RegNext(instrF.io.instrF.pc, 0.U)
  instrD.io.instrF.instr := RegNext(instrF.io.instrF.instr, 0.U)
  instrD.io.instrF.warp := RegNext(instrF.io.instrF.warp, 0.U)

  // Pipeline register between ID and ISS
  instrIss.io.id.valid := RegNext(instrD.io.id.valid, false.B)
  instrIss.io.id.pc := RegNext(instrD.io.id.pc, 0.U)
  instrIss.io.id.warp := RegNext(instrD.io.id.warp, 0.U)
  instrIss.io.id.opcode := RegNext(instrD.io.id.opcode, 0.U)
  instrIss.io.id.dest := RegNext(instrD.io.id.dest, 0.U)
  instrIss.io.id.rs1 := RegNext(instrD.io.id.rs1, 0.U)
  instrIss.io.id.rs2 := RegNext(instrD.io.id.rs2, 0.U)
  instrIss.io.id.rs3 := RegNext(instrD.io.id.rs3, 0.U)
  instrIss.io.id.imm := RegNext(instrD.io.id.imm, 0.S)
  instrIss.io.id.srs := RegNext(instrD.io.id.srs, 0.U)
  instrIss.io.scheduler.warp := warpScheduler.io.scheduler.warp
  instrIss.io.scheduler.stall := warpScheduler.io.scheduler.stall

  // Outputs of the instruction issues stage
  io.front.warp := instrIss.io.iss.warp
  io.front.opcode := instrIss.io.iss.opcode
  io.front.dest := instrIss.io.iss.dest
  io.front.rs1 := instrIss.io.iss.rs1
  io.front.rs2 := instrIss.io.iss.rs2
  io.front.rs3 := instrIss.io.iss.rs3
  io.front.srs := instrIss.io.iss.srs
  io.front.imm := instrIss.io.iss.imm

  io.aluInitCtrl <> warpScheduler.io.aluInitCtrl
}
