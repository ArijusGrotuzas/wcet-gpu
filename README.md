# Time-predictable GPU

This project contains an RTL description of a time-predictable GPU core for real-time systems.
A GPU core is often referred to as a streaming multiprocessor (SM).
The SM is described in Chisel hardware description language.

## Microarchitecture

The SM is an in-order SIMD processor with an 8-stage pipeline.
The pipeline consists of 6 logical pipeline stages: IF, ID, ISS, OF, EX, and WB.

A high-level overview of the SM is shown below:

![ISA](/images/SM_Overview.png)

The blocks marked in red are not implemented in the current version of the SM.

## Assembler

The project includes an assembler that converts assembly code to machine code of SM.
For instance, the following assembly code:

```asm
addi x2, x0, 3              // j = 3
addi x3, x0, 5              // a = 5
nop

LOOP:
    @p1 cmp %nz x4, x2      // i =< j
    @p1 addi x4, x4, 1      // i += 1
    @p1 add x5, x5, x3      // b += a
    @p1 nop
    @p1 nop
    @p1 br LOOP

ret
```

is converted to the following machine code:

```hex
0001804D
0002806D
00000000
400110C6
4000908D
400194A3
40000000
40000000
7FFFFF62
0000001F
```

## ISA

The SM supports the following instructions:

| Mnemonic | Opcode | Description                               |
|----------|--------|-------------------------------------------|
| NOP      | 00000  | No operation                              |
| RET      | 11111  | Return from execution                     |
| LD       | 00001  | Load from memory                          |
| ST       | 00101  | Store to memory                           |
| LDS      | 10001  | Load data from special register file      |
| ADDI     | 01001  | Add immediate to operand                  |
| LUI      | 01101  | Load upper 20 bits                        |
| SRL      | 10001  | Shift right logical by the second operand |
| SLL      | 10101  | Shift left logical by the second operand  |
| ADD      | 00011  | Add two operands                          |
| SUB      | 00111  | Subtract two operands                     |
| AND      | 01011  | Bitwise AND                               |
| OR       | 01111  | Bitwise OR                                |
| MUL      | 10011  | Multiply two operands                     |
| MAD      | 10111  | Multiply-Accumulate                       |
| BR       | 00010  | Branch                                    |
| CMP      | 00110  | Compare two operands                      |

## Instruction encoding

The instruction encoding is as follows:

![ISA](/images/isa.png)
