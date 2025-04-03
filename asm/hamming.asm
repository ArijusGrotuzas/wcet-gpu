// Compute Hamming weight

// -------- C1 --------

lds x1, s0                  // thread id
lds x2, s1                  // warp id
lds x3, s3                  // warp width
lds x5, s4                  // block width
lds x6, s2                  // block id

mad x4, x3, x2, x1          // x4 = (warpWidth * warpID) + threadID = (local thread ID)

addi x9, x0, 0              // number array base address
addi x10, x0, 512           // output array base address

mad x7, x5, x6, x4          // x7 = (blockWidth * blockID) + localThreadId = (global thread ID)
addi x15, x0, 1             // load 1 for AND operation
addi x11, x0, 0             // count = 0

add x12, x9, x7             // addr(number[i]) = baseNumber + i
add x13, x10, x7            // addr(output[i]) = baseOutput + i
nop

// -------- A1 --------

ld x14, x12                 // load number[i] from global memory

// -------- C2 --------

LOOP:
    @p1 cmp %nz x14, x0     // number[i] >= 0
    @p1 and x16, x14, x15   // number[i] & 1
    @p1 nop
    @p1 srli x14, x14, 1    // number[i] >>= 1
    @p1 add x11, x11, x16   // count += number[i] & 1
    @p1 nop
    @p1 br LOOP

// -------- A2 --------
st x13, x11                 // store count to global memory

ret
