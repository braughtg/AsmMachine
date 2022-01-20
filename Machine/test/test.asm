M:	.word 8
P:	.word 12
	
	LOAD R1 STDIN     * Read the first value from STDIN.
	LOAD R2 STDIN	  * Read the second value from STDIN.

	BLT R1 R2 R2MAX   * R1 < R2 so R2 is MAX
	
*	STORE R1 STDOUT   * R1 >= R2 so R1 is MAX
	STORE R1 M
	
	JUMP END

R2MAX:	STORE R2 M *STORE R2 STDOUT  
	
END:	HALT
