// Integer AXPY

lds x1, s0              // thread id
lds x2, s1              // warp id
lds x3, s3              // warp width
lds x5, s4              // block width
lds x6, s2              // block id

mad x4, x3, x2, x1      // x4 = (warpWidth * warpID) + threadID = (local thread ID)

addi x9, x0, 0          // x base address
addi x10, x0, 256       // y base address

mad x7, x5, x6, x4      // x7 = (blockWidth * blockID) + localThreadId = (global thread ID)

addi x8, x0, 512        // output base address
addi x17, x0, 5         // load constant a = 5

add x12, x9, x7         // addr(X[i]) = baseX + i
add x13, x10, x7        // addr(Y[i]) = baseY + i
add x21, x8, x7         // addr(output[i]) = baseOutput + i

ld x18, x12             // load X[i] from global memory
ld x19, x13             // load Y[i] from global memory

mad x20, x17, x18, x19  // a * X[i] + Y[i]
nop
nop

st x21, x20             // store output[i] to global memory
ret
