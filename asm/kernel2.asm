addi x2, x0, 10 // j = 10
addi x3, x0, 5 // a = 5

LOOP:
    addi, x4, x4, 1 // i += 1
    nop
    add x5, x5, x3 // b += a
    cmp x4, x2 // sign(i - j)
    nop
    nop
    nop
    nop
    brnzp %n LOOP
ret