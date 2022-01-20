* First include for nested include test.

START:	LOAD R1 #2
	MOV R8 R12

	CALL INC2_LABEL1
	
	MOV R12 R8	
	RET

.include test-inc3.asm INC2