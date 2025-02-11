// Compute fibonacci sequence

lds x1, s0                  // thread id
lds x2, s1                  // warp id
lds x3, s3                  // warp width
lds x5, s4                  // block width
lds x6, s2                  // block id

mad x4, x3, x2, x1          // x4 = (warpWidth * warpID) + threadID = (local thread ID)

addi x9, x0, 0              // i = 0
addi x10, x0, 512           // output array base address

mad x7, x5, x6, x4          // x7 = (blockWidth * blockID) + localThreadId = (global thread ID)

addi x13, x0, 0             // a = 0
addi x14, x0, 1             // b = 1
add x16, x10, x7            // addr(output[i]) = baseOutput + i

LOOP:
    @p1 cmp %nz x9, x7      // i =< j
    @p1 add x15, x13, x14   // ith_val = a + b
    @p1 addi x9, x9, 1      // i += 1
    @p1 addi x13, x14, 0    // a = b
    @p1 addi x14, x15, 0    // b = ith_val
    @p1 nop
    @p1 br LOOP

st x16, x14                 // output[i] = b
ret