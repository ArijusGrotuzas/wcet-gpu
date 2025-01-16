// SAXPY

// TODO: Add a large output offset for the output buffer to allow multiple warps to compute the result
lds x1, s0              // thread id
lds x2, s1              // warp id
lds x3, s3              // warp width
lds x5, s4              // block width
lds x6, s2              // block id

mad x4, x3, x2, x1      // x4 = (warpWidth * warpID) + threadID = (local thread ID)

addi x9, x0, 0          // x base address
addi x10, x0, 8         // y base address

mad x7, x5, x6, x4      // x7 = (blockWidth * blockID) + localThreadId = (global thread ID)

addi x11, x0, 16        // c base address
addi x17, x0, 5         // load constant a = 5

add x12, x9, x7         // addr(X[i]) = baseX + i
add x13, x10, x7        // addr(Y[i]) = baseY + i

nop
ld x18, x12             // load X[i] from global memory

nop
ld x19, x13             // load Y[i] from global memory

nop
nop

add x20, x18, x19  // X[i] + Y[i]

nop
nop

mul x20, x17, x20        // a * (X[i] + Y[i])

nop
nop

st x20, x11             // store C[i] to global memory

ret
