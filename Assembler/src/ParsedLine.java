/**
 * ParsedLine.java Parse a line returned from the AsmFileReader into an object
 * so that the elements of the line can be easily accessed.
 * 
 * @author Grant William Braught
 * @author Dickinson College
 * @version 8/21/2000
 */

import java.util.*;

class ParsedLine {

    private int lineno;

    private String label;

    private String inst;

    private int addrMode;

    private String directive;

    private String[] operand;

    private int dataCount;

    private int[] data;

    private int expFactor;

    private boolean error;

    private String rawLine;

    /**
     * Construct a parsed line object from the raw line.
     * 
     * @param rawLine a vector of strings that contain the tokens from the .asm
     *            file line.
     */
    public ParsedLine(Vector rawLine, String srcFile) {

        operand = new String[4];
        data = new int[8];
        dataCount = 0;
        error = false;

        lineno = ((Integer) (rawLine.elementAt(0))).intValue();

        // Handle any labels on the line.
        if (hasLabel(rawLine)) {
            label = (String) (rawLine.elementAt(1));
            label = label.substring(0, label.length() - 1).toUpperCase();

            // Get rid of the label and the :
            rawLine.remove(1);
        }

        // Handle the assembler directive if the line has one.
        if (hasDirective(rawLine)) {
            try {
                directive = (String) (rawLine.elementAt(1));
                // Get rid of the . and the directive.
                rawLine.remove(1);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Line " + lineno + ": "
                        + "Error parsing assembler directive in file " + 
                        srcFile + ".");
                error = true;
                //System.exit(-1);
            }

            // Should probably parse based on the particular directive
            // here!

            if (directive.equalsIgnoreCase(".BREAK") && rawLine.size() != 1) {
                System.out
                        .println(".break directive may not have operands on line "
                                + lineno + " in file " + srcFile + ".");
                error = true;
            }
            else if (directive.equalsIgnoreCase(".INCLUDE")) {
                if (rawLine.size() == 3) {
                    operand[0] = (String)rawLine.elementAt(1);
                    rawLine.remove(1);
                    operand[1] = (String)rawLine.elementAt(1);
                    rawLine.remove(1);
                }
                else {
                    System.out.println(".include directive must be followed " 
                            + "by both a filename and a namespace on line " + lineno
                            + " in file " + srcFile + ".");
                    error = true;
                }
            }
            else if (directive.equalsIgnoreCase(".FILECHANGE")) {
                operand[0] = (String)rawLine.elementAt(1);
                rawLine.remove(1);
            }
            else {
                // Handle getting all of the data from the directive line.
                for (int i = 1; i < rawLine.size(); i++) {
                    try {
                        // Casting down to int doesn't loose any bits!
                        data[i - 1] = (int) getNumValue((String) (rawLine
                                .elementAt(i)));
                        dataCount++;
                    }
                    catch (NumberFormatException e) {
                        System.out.println("Line " + lineno + ": "
                                + "Error parsing data in file " + srcFile + ".");
                        error = true;
                        //System.exit(-1);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Line " + lineno + ": "
                                + "Data is limited to 8 items per line" +
                                " in file " + srcFile + ".");
                        error = true;
                        //System.out.println(e);
                        //System.exit(-1);
                    }
                }
            }
        }
        else {
            // At this point the only thing that can be left is
            // an instruction...
            Instructions.parse(rawLine, this, srcFile);
        }
    }

    public void setInst(String inst) {
        this.inst = inst;
    }

    public void setAddrMode(int mode) {
        addrMode = mode;
    }

    public void setOperand(int num, String op) {
        operand[num] = op;
    }

    public void setExpFactor(int fact) {
        expFactor = fact;
    }

    public void setError() {
        error = true;
    }

    public int getLineNum() {
        return lineno;
    }

    public String getOperand(int num) {
        return operand[num];
    }

    public int getAddrMode() {
        return addrMode;
    }

    public String getInst() {
        return inst;
    }

    public boolean getError() {
        return error;
    }

    public String getLabel() {
        return label;
    }

    public int getExpFactor() {
        return expFactor;
    }

    public String getDirective() {
        return directive;
    }

    public int getDataCount() {
        return dataCount;
    }

    public int getData(int i) {
        return data[i];
    }

    public String getRawLine() {
        return rawLine;
    }

    public String getPrintedInstruction() {
        String str = "";

        if (getLabel() != null) {
            str = str + getLabel() + ": ";
        }

        if (getDirective() != null) {
            str = str + getDirective() + " ";
        }
        else {
            str = str + getInst() + " ";
        }

        for (int i = 0; i < operand.length; i++) {
            if (operand[i] != null) {
                str = str + operand[i] + " ";
            }
        }

        for (int i = 0; i < dataCount; i++) {
            str = str + data[i] + ",";
        }

        // Drop trailing , from the list of data values.
        if (str.endsWith(",")) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    public void addLabelPrefix(String prefix) {
        if (label != null) {
            label = prefix + "_" + label;
        }

        for (int i = 0; i < operand.length; i++) {
            if (operand[i] != null) {
                             
                if (isLabel(operand[i]) && !isSTDLabel(operand[i]) && !isRegister(operand[i])) {
                    operand[i] = prefix + "_" + operand[i];
                }
                else if (isImmediateLabel(operand[i])) {
                    operand[i] = "#" + prefix + "_" + operand[i].substring(1);
                }
            }
        }
    }

    private boolean isSTDLabel(String s) {
        return s.equalsIgnoreCase("STDIN") || s.equalsIgnoreCase("STDOUT");
    }
    
    private boolean isLabel(String s) {
        return Character.isLetter(s.charAt(0))
                && (Character.isLetter(s.charAt(s.length() - 1)) ||
                        Character.isDigit(s.charAt(s.length() -1)));
    }
    
    private boolean isRegister(String s) {
        if (s.length() == 2) {
            return (s.charAt(0) == 'R' || s.charAt(0) == 'r') &&
            		Character.isDigit(s.charAt(1));
        }
        else if (s.length() == 3) {
            return (s.charAt(0) == 'R' || s.charAt(0) == 'r') &&
            		s.charAt(1) == '1' &&
            		Character.isDigit(s.charAt(2));
        }
        else {
            return false;
        }
    }

    private boolean isImmediateLabel(String s) {
        return s.charAt(0) == '#' && isLabel(s.substring(1));
    }

    /**
     * Generate a string representation of the parsed line.
     * 
     * @return String representation of the parsed line.
     */
    public String toString() {
        String str = "";
        str = "   Line Num: " + lineno + "\n" + "      Label: " + label + "\n"
                + "Instruction: " + inst + "\n" + "  Operand 1: " + operand[0]
                + "\n" + "  Operand 2: " + operand[1] + "\n" + "  Operand 3: "
                + operand[2] + "\n" + "  Directive: " + directive + "\n"
                + "       Data: ";

        for (int i = 0; i < dataCount; i++) {
            str = str + data[i] + ",";
        }

        str = str + "\n";

        return str;
    }

    /**
     * Return the integer value contained in the string. The string can be in
     * dec, hex or binary.
     */
    public static long getNumValue(String numStr) throws NumberFormatException {
        int base;

        if (numStr.charAt(0) == 'b' || numStr.charAt(0) == 'B') {
            base = 2;
            numStr = numStr.substring(1, numStr.length());
        }
        else if (numStr.startsWith("0x") || numStr.startsWith("0X")) {
            base = 16;
            numStr = numStr.substring(2, numStr.length());
        }
        else {
            base = 10;
            numStr = Integer.toString((int) Double.parseDouble(numStr));
        }

        // This is a bit of a hack because the Integer class
        // didn't like parsing 32bit hex numbers (eg. DEADBEEF).
        // Also using int's makes anyting with a 1 in the MSB
        // appear negative. This made testing for > MAX_OFFSET
        // a pain.
        return Long.parseLong(numStr, base);
    }

    /**
     * Return true if the line has a label.
     */
    private boolean hasLabel(Vector rawLine) {
        try {
            String tok = (String) (rawLine.elementAt(1));
            return (tok.charAt(tok.length() - 1) == ':');
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Return true if the line has an assembler directive.
     */
    private boolean hasDirective(Vector rawLine) {
        try {
            String tok = (String) (rawLine.elementAt(1));
            return tok.startsWith(".");
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /*
     * private boolean hasInst(Vector rawLine) { try { String tok =
     * (String)(rawLine.elementAt(1)); return Instructions.isInstruction(tok); }
     * catch (ArrayIndexOutOfBoundsException e) { return false; } }
     */

    /**
     * Test method.
     */
    public static void main(String[] args) {
        AsmFileReader r = new AsmFileReader("test.asm");
        ParsedLine pl;

        Vector line;

        line = r.nextLine();
        while (line != null) {
            pl = new ParsedLine(line, r.getFilename());
            System.out.println(pl);
            line = r.nextLine();
        }
    }
}

