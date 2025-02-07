// Program Under Analysis

lds x1, s0                  // thread id
lds x2, s1                  // warp id
lds x3, s3                  // warp width
lds x5, s4                  // block width
lds x6, s2                  // block id

mad x4, x3, x2, x1          // x4 = (warpWidth * warpID) + threadID = (local thread ID)

// BB1
addi x9, x0, 0              // int i = 0;
addi x10, x0, 20
addi x11, x0, 30

mad x7, x5, x6, x4          // x7 = (blockWidth * blockID) + localThreadId = (global thread ID)

addi x13, x0, 14            // res[4] base address
add x16, x13, x7            // res[threadIdx];
ld x12, x7                  // data[threadIdx];

@p1 cmp %p x12, x10         // data[threadIdx] > 20;
@p3 cmp %n x12, x10         // data[threadIdx] < 20;
@p2 cmp %p x12, x11         // data[threadIdx] > 30;

// BB2
@p1 addi x14, x0, 5         // int a = 5;

// BB3
@p2 add x15, x12, x14       // a + data[threadIdx];
@p2 st x16, x15             // res[threadIdx] = a + data[threadIdx];

// BB4
@p1 add x9, x0, x14         // i = a;

// BB5
@p3 addi x9, x0, 10         // i = 10;

// BB6
addi x9, x9, 1              // i = i + 1;

ret
