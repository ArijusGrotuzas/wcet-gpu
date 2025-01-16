// Tests the ability of the SM to perform basic arithmetic and immediate operations

lui x1, 2330
lui x2, 10982
nop
addi x3, x1, 1110
addi x4, x2, 751
nop
nop
add x5, x3, x4
nop
nop
sub x6, x4, x5
and x7, x3, x4
or x8, x4, x5
nop
ret