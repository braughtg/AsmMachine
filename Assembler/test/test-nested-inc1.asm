* Test a nested include.

	LOAD R0 #1
	
	CALL INC1_START
	
	HALT

.include test-nested-inc2.asm INC1