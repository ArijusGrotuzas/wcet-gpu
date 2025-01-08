lds x4, s3 // Load warp width to register 4
lds x2, s1 // Load warp ID to register 2
lds x1, s0 // load thread ID to register 1
lds x5, s4 // load block width to register 5
lds x6, s2 // load block ID to register 3
mad x7, x4, x2, x1 // x7 = (warpWidth * warpID) + threadID (local thread ID)
addi x9, x0, 5
addi x10, x0, 10
mad x8, x5, x6, x7 // x8 = (blockWidth * blockID) + x7 (global thread ID)
nop
add x11, x9, x10
nop
nop
st x8, x11
ld x12, x8
ret
