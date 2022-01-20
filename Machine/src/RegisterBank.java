/**
 * RegisterBank.java
 *   A register bank containing 16 registers.  This
 *   register bank stores values only as 32 bit binary
 *   numbers represented as Strings.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 8/29/2000
 */

class RegisterBank {

    String[] registers;

    public RegisterBank() {
	registers = new String[16];
	for (int i=0; i<=15; i++) {
	    registers[i] = "00000000000000000000000000000000";
	}
    }

    /**
     * Read the contents of a register.
     *
     * @param addr the register address to read.  This is a
     *             4 bit binary number in a String.
     * @return the data stored in the requested register.
     */
    public String read(String addr) {
	int iAddr;
	try {
	    iAddr = Integer.parseInt(addr,2);
	    if (iAddr >= 0 && iAddr <= 15) {
		return registers[iAddr];
	    }
	    else {
		System.out.println("Attempt to read to nonexistent " +
				   "register #" + iAddr);
		return null;
	    }
	}
	catch (NumberFormatException e) {
	    System.out.println("Error reading register: " +
			       addr + ".");
	    return null;
	}
    }

    /**
     * Write the contents of a register.
     *
     * @param addr the register to write the data into.  This
     *             is a 4 bit binary number in a String.
     * @param val the data value to write into the register.
     *            This is also a 32 bit binary value
     *            in a string.
     */
    public void write(String addr, String val) {
	int iAddr;
	try {
	    iAddr = Integer.parseInt(addr,2);
	    if (iAddr > 15) {
		System.out.println("Attempt to write to nonexistent register.");
	    }
	    else {
		registers[iAddr] = val;
	    }
	}
	catch (NumberFormatException e) {
	    System.out.println("Error writing register: " +
			       addr + ".");
	}
    }

    public void reset() {
	for (int i=0; i<=15; i++) {
	    registers[i] = "00000000000000000000000000000000";
	}
    }

    public static void main (String[] args) {

	RegisterBank rb = new RegisterBank();

	for (int i=1; i<=15; i++) {
	    System.out.println(i + ": " + 
			       rb.read(Integer.toBinaryString(i)));	    
	}

	for (int i=1; i<=15; i++) {
	    String bin = Integer.toBinaryString(i);
	    while (bin.length() < 32) {
		bin = "0" + bin;
	    }
	    rb.write(Integer.toBinaryString(i),bin);	    
	}

	for (int i=1; i<=15; i++) {
	    System.out.println(i + ": " + 
			       rb.read(Integer.toBinaryString(i)));	    
	}
    }
}
