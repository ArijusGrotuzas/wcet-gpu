object TestKernels {
  val kernel1 = Array(
    "h0024682D", // (LUI, x1, 2330)
    "h00AB984D", // (LUI, x2, 10982)
    "h00000000", // (NOP)
    "h022B0469", // (ADDI, x3, x1, 1110)
    "h01778889", // (ADDI, x4, x2, 751)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h000190A3", // (ADD, x5, x3, x4)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h000290C7", // (SUB, x6, x4, x5)
    "h00020CEB", // (AND, x7, x3, x4)
    "h2000910F", // (OR, x8, x4, x5)
    "h00000000", // (NOP)
    "h0000001F" // (RET)
  )

  val kernel2 = Array(
    "h00050049", // (ADDI, x2, x0, 10): j = 10
    "h00028069", // (ADDI, x3, x0, 5): a = 5
    "h00009089", // (ADDI, x4, x4, 1): i += 1
    "h00000000", // (NOP)
    "h000194A3", // (ADD, x5, x5, x3): b += a
    "h00011006", // (CMP, x4, x2) i < j
    "h00000000", // (NOP)
    "h3FFEC22", // (BRNZP, NZP=100, -6)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h0000001F" // (RET)
  )

  val kernel3 = Array(
    "hC00BD58D", // (LUI, 12, 21, 23, X)
    "h01D99A9", // (ADDI, 13, 6, 27, 1)
    "h2195DF", // (RET, 14, 5, 3, 2)
  )
}
