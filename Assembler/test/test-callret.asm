	JUMP PROC2

		
PROC1:	LOAD R2 #2
	RET
	HALT

PROC2:	LOAD R1 #1
	CALL PROC1
	LOAD R3 #3
	CALL PROC3
	LOAD R5 #5
	HALT

PROC3:	LOAD R4 #4
	RET
	HALT