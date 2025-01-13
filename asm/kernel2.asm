addi x2, x0, 3 // j = 3
addi x3, x0, 5 // a = 5
nop

LOOP:
    @p1 cmp %n x4, x2 // sign(i - j)
    @p1 addi x4, x4, 1 // i += 1
    @p1 add x5, x5, x3 // b += a
    nop
    nop
    @p1 br LOOP
ret