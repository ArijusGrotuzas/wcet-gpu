lds x1, s0              // thread id
lds x2, s1              // warp id
lds x3, s3              // warp width
lds x5, s4              // block width
lds x6, s2              // block id

addi x9, x0, 8          // i = 8
mad x4, x3, x2, x1      // x4 = (warpWidth * warpID) + threadID = (local thread ID)
nop
nop

@p1 cmp %p x4, x9       // i > j
nop
nop
nop
nop
prepare END

@p1 split ELSE
addi x13, x0, 1
nop
br ENDIF

ELSE:
    addi x13, x0, 2
    nop

ENDIF:
    join

END:
    st x4, x13
    ret