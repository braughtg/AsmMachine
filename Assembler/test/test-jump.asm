	JUMP TEST

TEST2:	LOAD R2 #2
	LOAD R15 #TEST3
	JUMP R15
	LOAD R2 #0xFF
	HALT

TEST4:	LOAD R4 #4
	HALT
	
TEST:	LOAD R1 #1
	JUMP TEST2
	LOAD R1 #0xFF
	HALT

TEST3:	LOAD R3 #3
	LOAD R15 #TEST4
	JUMP R15
	LOAD R3 #0xFF
	HALT

		