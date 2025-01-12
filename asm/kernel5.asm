lds x1, s0 // load thread ID to register 1
addi x2, x0, 4 // load 4 to register 2
nop
nop
cmp x1, x2 // if thread ID < 4
nop
nop
nop
nop
split %n // split either n z or p
addi x3, x0, 5 // j = 10
join
ret