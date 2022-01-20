X:  .word 880       * Declare X
     LOAD R1 X      * R1 = X
     ADD R2 R1 #5   * R2 = R1 + 5
     STORE R2 X     * X = R2
     HALT           * Stop the Machine.
