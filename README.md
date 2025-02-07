# Time-predictable GPU

## Microarchitecture

![ISA](/images/SM_Overview.png)

## Assembler

## ISA

| Mnemonic | Opcode | Description                          |
|----------|--------|--------------------------------------|
| NOP      | 00000  | No operation                         |
| RET      | 11111  | Return from execution                |
| LD       | 00001  | Load from memory                     |
| ST       | 00101  | Store to memory                      |
| LDS      | 10001  | Load data from special register file |
| ADDI     | 01001  | Add immediate to operand             |
| LUI      | 01101  | Load upper 20 bits                   |
| SRLI     | 10001  | Shift right logical immediate        |
| SLLI     | 10101  | Shift left logical immediate         |
| ADD      | 00011  | Add two operands                     |
| SUB      | 00111  | Subtract two operands                |
| AND      | 01011  | Bitwise AND                          |
| OR       | 01111  | Bitwise OR                           |
| MUL      | 10011  | Multiply two operands                |
| MAD      | 10111  | Multiply-Accumulate                  |
| BR       | 00010  | Branch                               |
| CMP      | 00110  | Compare two operands                 |

## Instruction encoding

![ISA](/images/isa.png)
