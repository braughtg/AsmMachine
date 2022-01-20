import java.util.*;
import java.io.*;

/**
 * MainMemory.java
 *   The main memory of the computer.  This is
 *   represented as a Vector of strings.  Each
 *   string contains a 32 bit binary number.
 *   A vector is used to allow as large a memory
 *   as possible w/o wasting space if it is 
 *   not needed.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 8/29/2000
 */

class MainMemory {
    private Vector mem;
    private Vector asm;
    private Vector ml;
    private boolean error;

    /**
     * Construct a MainMemory object from the data in the 
     * specified file.  All blank lines and lines beginning
     * with an * are discarded.  All other lines must contain
     * exactly a 32 bit binary number.
     *
     * @param filename the name of the file containing the
     *                 image of the memory contents.
     */
    public MainMemory(String filename) {

	BufferedReader reader;
	mem = new Vector();
	asm = new Vector();
	ml = new Vector();
	error = false;

	try {
	    reader = new BufferedReader (new FileReader(filename));

	    int lineno = 0;

	    String line;
	    String lastAsmComment = "";

	    while ((line = reader.readLine()) != null) {
		
		lineno++;

		// If the line is not a comment and not
		// empty then check its length and contents
		// and put it into the vector.
		if (line.startsWith("**")) {
		    ml.add(line.substring(3,line.length()));
		}
		else if (line.startsWith("*")) {
		    lastAsmComment = line.substring(2,line.length());
		}
		else {
		    try {
			Long.parseLong(line,2);
			
			// Its a binary number so stuff it into
			// the vector.  Put the bytes into memory
			// in little-endian format.
			mem.add(line.substring(24,32));
			mem.add(line.substring(16,24));
			mem.add(line.substring(8,16));
			mem.add(line.substring(0,8));

			asm.add(lastAsmComment);
		    }
		    catch (NumberFormatException e) {
			// Its not a binary number...
			System.err.println("Line " + lineno + ": " +
					   " Non-binary data.");
			error = true;
		    }
		}
	    }
	}
	catch (FileNotFoundException e) {
	    System.err.println("Error: Unable to open " + 
			       filename + ".");
	    error = true;
	}
	catch (IOException e) {
	    System.err.println("Error reading " + filename + ".");
	    error = true;
	}

	mem.trimToSize();
	asm.trimToSize();
    }
    
    public boolean getError() {
	return error;
    }

    /**
     * Read the contents of a memory cell.
     *
     * @param addr the memory address to read.  This is a
     *             32 bit binary number in a String.
     * @return the data stored in the requested memory 
     *         address.
     */
    public String read(String addr) {
	int iAddr;
	try {
	    iAddr = Integer.parseInt(addr,2);
	    String val = (String)mem.elementAt(iAddr+3) +
		(String)mem.elementAt(iAddr+2) +
		(String)mem.elementAt(iAddr+1) +
		(String)mem.elementAt(iAddr);

	    return val;       
	}
	catch (NumberFormatException e) {
	    System.out.println("Error reading memory address: " +
			       addr + ".");
	    return null;
	}
    }

    /**
     * Write the contents of a memory cell.
     *
     * @param addr the address to write the data into.  This
     *             is a 32 bit binary number in a String.
     * @param val the data value to write into the memory
     *            address.  This is also a 32 bit binary value
     *            in a string.
     */
    public void write(String addr, String val) {
	int iAddr;
	try {
	    iAddr = Integer.parseInt(addr,2);
	    if (iAddr+3 >= mem.size()) {
		System.out.println("Attempt to write beyond memory.");
	    }
	    else {
		mem.setElementAt(val.substring(24,32),iAddr);
		mem.setElementAt(val.substring(16,24),iAddr+1);
		mem.setElementAt(val.substring(8,16),iAddr+2);
		mem.setElementAt(val.substring(0,8),iAddr+3);
	    }
	}
	catch (NumberFormatException e) {
	    System.out.println("Error writing memory address: " +
			       addr + ".");
	}
    }

    public String getAsmComment(String addr) {
	int iAddr;
	try {
	    iAddr = Integer.parseInt(addr,2);
	    if (iAddr/4 >= asm.size()) {
		return null;
	    }
	    else {
		return (String)asm.elementAt(iAddr/4);
	    }
	}
	catch (NumberFormatException e) {
	    System.out.println("Error getting comment for address: " +
			       addr + ".");
	}
	return null;
    }

    public String getMLComment(String addr) {
	int iAddr;
	try {
	    iAddr = Integer.parseInt(addr,2);
	    if (iAddr/4 >= ml.size()) {
		return null;
	    }
	    else {
		return (String)ml.elementAt(iAddr/4);
	    }
	}
	catch (NumberFormatException e) {
	    System.out.println("Error getting comment for address: " +
			       addr + ".");
	}
	return null;
    }

    /** 
     * Get the vector that is holding the memory contents.
     */
    public Vector getMem() {
	return mem;
    }

    /**
     * Test method.
     */
    public static void main (String[] args) {

	MainMemory mem = new MainMemory("test.exe");
	System.out.println(mem.read("00"));
	System.out.println(mem.read("01"));
	
	System.out.println(mem.read("100"));
	mem.write("100","10101010101010101010101010101010");
	System.out.println(mem.read("100"));
    }
}
