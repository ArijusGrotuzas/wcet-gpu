lds x1, s0              // thread id
lds x2, s1              // warp id
lds x3, s3              // warp width
lds x5, s4              // block width
addi x9, x0, 8          // i = 0
lds x6, s2              // block id

mad x4, x3, x2, x1      // x4 = (warpWidth * warpID) + threadID = (local thread ID)

@p1 cmp %p x4, x9       // i > j
nop
nop
nop
nop
prepare END

@p1 split ELSE
addi x13, x0, 1
@p0 br ENDIF

ELSE:
    addi x13, x0, 2

ENDIF:
    join

END:
    st x13, x0
    ret