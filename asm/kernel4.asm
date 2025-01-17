// Tests the ability of the SM to load and store data

lds x1, s0              // thread id
lds x2, s1              // warp id
lds x3, s3              // warp width
lds x5, s4              // block width
lds x6, s2              // block id

mad x4, x3, x2, x1      // x4 = (warpWidth * warpID) + threadID = (local thread ID)
nop
nop

mad x7, x5, x6, x4      // x7 = (blockWidth * blockID) + localThreadId = (global thread ID)
nop
nop

add x12, x0, x7
nop
nop

st x7, x12
ld x13, x7

ret
