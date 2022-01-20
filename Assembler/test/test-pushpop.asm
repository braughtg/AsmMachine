X:	.word 99
Y:	.word 100
Z:	.space 50
	
*ERROR:	.stacksize 32
*	.stacksize 99
	.stacksize 44

A:	.word 22
B:	.word 33
	
	.break
	
	LOAD R1 #1
	LOAD R2 #2
	LOAD R3 #3
	LOAD R4 #4

	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4

	.break

	POP R5
	POP R6
	POP R7
	POP R8

	.break

	JUMP SR
	.break
	NOP
	.break
	NOP
	.break
	
SR:	LOAD R9 #999
	BPOS R9 SR2

	.break
	NOP
	.break
	NOP
	.break

SR2:	LOAD R10 X
	LOAD R11 Y

	LOAD R12 #-5
	.break
	
SR3:	ADD R12 R12 #1
	BNEG R12 SR3

	HALT
