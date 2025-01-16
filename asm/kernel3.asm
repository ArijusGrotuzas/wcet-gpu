// Tests the ability of the SM to compute the threads global index

lds x4, s3          // Load warp width to register 4
lds x2, s1          // Load warp ID to register 2
lds x1, s0          // load thread ID to register 1
lds x5, s4          // load block width to register 5
lds x6, s2          // load block ID to register 3

mad x7, x4, x2, x1  // x7 = (warpWidth * warpID) + threadID

nop
nop

mad x8, x5, x6, x7  // x8 = (blockWidth * blockID) + x7

ret

