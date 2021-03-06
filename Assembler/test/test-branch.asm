X:	.word 0 1 -1 2 3

	LOAD R0 #X
	LOAD R1 R0

	* Taken Forward Branches...
	BZERO R1 BZ
	LOAD R2 #0xFFFFFFFF
BZ:	LOAD R1 R0 +4
	BNZERO R1 BNZ
	LOAD R2 #0xFFFFFFFF
BNZ:	BPOS R1 BP
	LOAD R2 #0xFFFFFFFF		
BP:	BODD R1 BO
	LOAD R2 #0xFFFFFFFF
BO:	LOAD R1 R0 +8
	BNEG R1 BN
	LOAD R2 #0xFFFFFFFF
BN:	LOAD R1 R0 +12
	BEVEN R1 BE
	LOAD R2 #0xFFFFFFFF

	* Taken Backwards Branches...

BN2:	LOAD R1 R0 +12
	BEVEN R1 NT
	LOAD R2 #0xFFFFFFFF
	HALT
	
BO2:	LOAD R1 R0 +8
	BNEG R1 BN2
	LOAD R2 #0xFFFFFFFF
	HALT

BP2:	BODD R1 BO2
	LOAD R2 #0xFFFFFFFF
	HALT

BNZ2:	BPOS R1 BP2
	LOAD R2 #0xFFFFFFFF	
	HALT

BZ2:	LOAD R1 R0 +4
	BNZERO R1 BNZ2
	LOAD R2 #0xFFFFFFFF	
	HALT
	
BE:	.break
	LOAD R1 R0
	BZERO R1 BZ2
	HALT

	* Not taken branches
NT:	.break
	LOAD R1 R0
	BNZERO R1 NT
	BODD R1 NT
	BPOS R1 NT
	BNEG R1 NT
	
	LOAD R1 R0 +4
	BEVEN R1 NT
	BZERO R1 NT
	BNEG R1 NT

	LOAD R1 R0 +8
	BZERO R1 NT
	BPOS R1 NT
	BEVEN R1 NT
		
	HALT		




