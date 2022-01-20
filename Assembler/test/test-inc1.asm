	LOAD R0 #1
	LOAD R1 #2

	CALL INC2_LABEL1
 	CALL INC3_LABEL1
	
	HALT

.include test-inc2.asm INC2
.include test-inc3.asm INC3

