object TestKernels {
  // Tests the ability to perform basic arithmetic operations
  val kernel1 = Array(
    "h0024682D", // (LUI, x1, 2330)
    "h00AB984D", // (LUI, x2, 10982)
    "h00000000", // (NOP)
    "h022B0469", // (ADDI, x3, x1, 1110)
    "h01778889", // (ADDI, x4, x2, 751)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00020CA3", // (ADD, x5, x3, x4)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h000290C7", // (SUB, x6, x4, x5)
    "h00020CEB", // (AND, x7, x3, x4)
    "h0002910F", // (OR, x8, x4, x5)
    "h00000000", // (NOP)
    "h0000001F" // (RET)
  )

  // Tests the ability to perform warp wide conditional jumps, for example, for for-loops
  val kernel2 = Array(
    "h00050049", // (ADDI, x2, x0, 10): j = 10
    "h00028069", // (ADDI, x3, x0, 5): a = 5
    "h00009089", // (ADDI, x4, x4, 1): i += 1
    "h00000000", // (NOP)
    "h000194A3", // (ADD, x5, x5, x3): b += a
    "h00011006", // (CMP, x4, x2) i < j
    "h00000000", // (NOP)
    "hFFFC1362", // (BRNZP, NZP=100, -5)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h0000001F" // (RET)
  )

  // Tests calculation of threads global ID
  val kernel3 = Array(
    "h00000C91", // (LDS, x4, s3): Load warp width to register 4
    "h00000451", // (LDS, x2, s1): Load warp ID to register 2
    "h00000031", // (LDS, x1, s0): Load thread ID to register 1
    "h000010B1", // (LDS, x5, s4): Load block width to register 5
    "h000008D1", // (LDS, x6, s2): Load block ID to register 3
    "h001110F7", // (MAD, x7, x4, x2, x1): x7 = (warpWidth * warpID) + threadID
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00731517", // (MAD, x8, x5, x6, x7): x8 = (blockWidth * blockId) + x7
    "h0000001F" // (RET)
  )

  // Tests loading and storing from data memory
  val kernel4 = Array(
    "h00000C91",
    "h00000451",
    "h00000031",
    "h000010B1",
    "h000008D1",
    "h001110F7",
    "h00028129",
    "h00050149",
    "h00731517",
    "h00000000",
    "h00052563",
    "h00000000",
    "h00000000",
    "h0005A005",
    "h00002181",
    "h0000001F"
  )
}
