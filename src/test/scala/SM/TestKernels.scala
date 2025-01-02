package SM

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
    "h0000001F"  // (RET)
  )

  val kernel2 = Array(
    "h00050049", // (ADDI, x2, x0, 10): j = 10
    "h00028069", // (ADDI, x3, x0, 5): a = 5
    "h00009089", // (ADDI, x4, x4, 1): i += 1
    "h00000000", // (NOP)
    "h000194A3", // (ADD, x5, x5, x3): b += a
    "h00011006", // (CMP, x4, x2) i < j
    "h00000000", // (NOP)
    "hFFFFE822", // (BRNZP, NZP=100, -6) TODO: Fix this to be -5 instead
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h00000000", // (NOP)
    "h0000001F" // (RET)
  )

  // TODO: Change encoding
  val kernel3 = Array(
    "11000000000010111101010110001101", // (LUI, 12, 21, 23, X)
    "00000000000111011001100110101001", // (ADDI, 13, 6, 27, 1)
    "00000000001000011001010111011111", // (RET, 14, 5, 3, 2)
  )
}
