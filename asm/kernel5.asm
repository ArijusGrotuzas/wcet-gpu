// Tests the ability of the SM to handle diverging loop paths

lds x1, s0                  // load thread ID to register 1
addi x2, x0, 10             // j = 10
nop

add x3, x0, x1              // i = threadID
nop
nop

LOOP:
    @p1 cmp %nz x3, x2      // threadID < j
    @p1 addi x3, x3, 1      // threadID += 1
    @p1 addi x5, x5, 1      // x5 += 1
    @p1 nop
    @p1 nop
    @p1 nop
    @p1 br LOOP

ret