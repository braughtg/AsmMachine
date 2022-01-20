/**
 * This program has evolved into a complete HACK!  
 * 
 * This whole thing should be reworked to use object files and linking to allow
 * for the use of multiple files. The current approach using the passZero method
 * is just a hack because includes were an afterthought.
 * 
 * Assembler.java This is the heart of the assembler. The main jobs of the first
 * pass of the assember are to build the symbol table and to report errors. This
 * is performed by the method passOne. The main jobs of the second pass of the
 * assember are to translate the assembly language into machine language and
 * write it out to a file. This is performed by the method passTwo.
 * 
 * @author Grant William Braught
 * @author Dickinson College
 * @version 8/25/2000
 */

import java.util.*;
import java.io.*;

class Assembler {

    /*
     * Handle all .includes by producing a single large file that contains the
     * entire program. Everytime there is a change in the file that the code is
     * from, insert a .filechange <filename> directive that will indicate the
     * file from which the code came. Handle nested includes by using recursive
     * calls to this method.
     * 
     * Prefixes are specified by .include directive and are cumulative across a
     * sequence of nested includes. E.g. if file A includes file B w/ prefix ONE
     * and file B include file C with prefix TWO then the labels in file C will
     * have the prefix ONE_TWO...
     * 
     * The first call to passZero should have an empty labelPrefix String.
     * 
     * Nested includes are caught by determining if the file being included has
     * occured earlier in the sequence of includes that lead to the inclusion of
     * the current file. If so the circular include is flagged and the assembly
     * bails out. This is handled by the includeFiles Vector which holds the
     * filenames of all of the files that have been included thus far including
     * the original main file.
     * 
     * The first call to passZero should have an empty includeFiles Vector.
     */
    private static boolean passZero(AsmFileReader progFile,
            PrintWriter tmpFile, String labelPrefix, Vector includeFiles) {

        boolean error = false;

        includeFiles.add(progFile.getFilename());
        //System.out.println("\tProcessing " + progFile.getFilename() + "...");

        // Write a .filechange directive for the file.
        tmpFile.println(".filechange " + progFile.getFilename());

        Vector line = progFile.nextLineNoSkips();
        while (line != null) {

            boolean lineIsInclude = false;

            if (AsmFileReader.isBlank(line)) {
                tmpFile.println();
            }
            else {
                // Parse the line from the assembler file...
                ParsedLine pl = new ParsedLine(line, progFile.getFilename());
                error = error || pl.getError();
                
                if (pl.getDirective() != null) {

                    // if the directive is illegal print an error and
                    // continue...
                    if (!labelPrefix.equalsIgnoreCase("")
                            && !(pl.getDirective().equalsIgnoreCase(".BREAK") || pl
                                    .getDirective()
                                    .equalsIgnoreCase(".INCLUDE"))) {
                        pl.setError();
                        System.out
                                .println("Assembler directive "
                                        + pl.getDirective()
                                        + " may not be used in an included file.  Line "
                                        + pl.getLineNum() + " in file "
                                        + progFile.getFilename());
                        error = true;
                    }
                    else if (pl.getDirective().equalsIgnoreCase(".INCLUDE")) {
                        if (includeFiles.contains(pl.getOperand(0))) {
                            pl.setError();
                            System.out
                                    .println("Include assembler directive on line "
                                            + pl.getLineNum()
                                            + " of file "
                                            + progFile.getFilename()
                                            + " creates a circular include. Assembly terminated.");
                            return true;
                        }
                        else {
                            String newFile = progFile.getFilename();
                            int x = newFile.lastIndexOf("/");
                            if (x > 0) {
                                newFile = newFile.substring(0, x + 1);
                                newFile = newFile + pl.getOperand(0);
                            }
                            else {
                                // Worry about Assembler in the same 
                                // directory as the file being assembled.
                                newFile = pl.getOperand(0);
                            }
                            
                            //System.out.println(newFile);
                            AsmFileReader newAsmFile = new AsmFileReader(
                                    newFile);
                            //System.out.println("reader created");

                            String newLabelPrefix = pl.getOperand(1);
                            if (!labelPrefix.equals("")) {
                                newLabelPrefix = labelPrefix + "_" + newLabelPrefix;
                            }

                            error = passZero(newAsmFile, tmpFile,
                                    newLabelPrefix, includeFiles)
                                    || error;

                            tmpFile.println(".filechange "
                                    + progFile.getFilename());

                            lineIsInclude = true;
                        }
                    }
                }

                // Add a method to the ParsedLine class to prepend the Label
                // prefix
                // to any labels that appear in the instruction as line labels
                // or as operands.
                if (!labelPrefix.equalsIgnoreCase("")) {
                    pl.addLabelPrefix(labelPrefix);
                }

                if (!lineIsInclude) {
                    // Add a method to the ParsedLine class to print the
                    // instruction.
                    tmpFile.println(pl.getPrintedInstruction());
                }
            }
            
            line = progFile.nextLineNoSkips();
            includeFiles.remove(progFile.getFilename());
        }

        return error;
    }

    // This needs to be updated to deal with .filechange pseudo directives
    // that indicate the original file from which the code came.

    private static int passOne(Hashtable symbolTable, AsmFileReader progFile) {
        int addr = 0;
        int errorCnt = 0;
        ParsedLine pl;
        Vector line;
        Vector pendingDirectives = new Vector();
        String curFile = "";

        Hashtable lineNumHack = new Hashtable();

        line = progFile.nextLine();
        while (line != null) {

            // Parse the line from the assembler file...
            pl = new ParsedLine(line, curFile);

            // If it is an assembler directive we'll have to save it
            // for later... so just add it to a vector and we'll
            // process it at the end of the pass.
            if (pl.getDirective() != null) {
                if (pl.getDirective().equalsIgnoreCase(".FILECHANGE")) {
                    lineNumHack
                            .put(curFile, new Integer(progFile.getLineNum()));
                    curFile = pl.getOperand(0);

                    Integer lineNum = (Integer) lineNumHack.remove(curFile);
                    if (lineNum != null) {
                        progFile.setLineNum(lineNum.intValue());
                    }
                    else {
                        progFile.setLineNum(0);
                    }
                    
                    // *** Problem here with files that are included
                    // at multiple locations.  The line numbers reported
                    // are cumulative across all inclusions of that same file.
                    // ****
                    
                }
                else if (pl.getDirective().equalsIgnoreCase(".BREAK")) {
                    if (pl.getLabel() != null) {
                        symbolTable.put(pl.getLabel(), new Integer(addr));
                    }
                    addr = addr + 4;
                }

                // *******************
                // If .stacksize Put a STACK label into the symbolTable
                // with the size of the stack as its value.... If there is
                // already a STACK label
                // then report an error! Update the address by the number of
                // instructions necessary to set R13. If there is a label
                // report an error. Do not put it into the pendingDirectives
                // list.
                else if (pl.getDirective().equalsIgnoreCase(".STACKSIZE")) {
                    if (symbolTable.containsKey("STACK")) {
                        pl.setError();
                        System.out
                                .println("Multiple .stacksize directives: A program may contain "
                                        + "only one .stacksize directive. The second directive appears "
                                        + "on line "
                                        + pl.getLineNum()
                                        + " in file " + curFile + ".");
                    }
                    else if (pl.getLabel() != null) {
                        pl.setError();
                        System.out
                                .println("Illegal Label: A .stacksize directive may not have "
                                        + "a line label on line "
                                        + pl.getLineNum()
                                        + " in file "
                                        + curFile + ".");
                    }
                    else {
                        symbolTable.put("STACK", new Integer(pl.getData(0)));
                        addr = addr + 8;// These will be LOADi & LOADUi into R13
                    }
                }
                else {
                    pendingDirectives.add(pl);
                }
            }
            else {
                // This line is not a directive so see if it has
                // a label...
                String label = pl.getLabel();

                // If it has a label add that label to the symbol
                // table and report any duplicate labels as errors...
                if (label != null) {
                    if (symbolTable.containsKey(label)) {
                        pl.setError();
                        System.out.println("Duplicate Label: The label "
                                + label + " is defined at multiple "
                                + "locations including line " + pl.getLineNum()
                                + " in file " + curFile + ".");
                    }
                    else {
                        symbolTable.put(label, new Integer(addr));
                    }
                }

                // Increment the address counter by the size
                // of the instruction on the line.
                addr = addr + 4 * pl.getExpFactor();
            }

            // Count up the errors...
            if (pl.getError()) {
                errorCnt++;
            }

            // Get the next line of the file.
            line = progFile.nextLine();
        }

        // Now process the directives and store the data
        // directly on top of the program text.
        for (int i = 0; i < pendingDirectives.size(); i++) {
            pl = (ParsedLine) pendingDirectives.elementAt(i);

            String label = pl.getLabel();
            String directive = pl.getDirective();

            if (label != null) {
                if (symbolTable.containsKey(label)) {
                    Instructions.printError(pl, "The label " + label
                            + " is defined at multiple "
                            + "locations including this line in file" + curFile
                            + ".");
                    errorCnt++;
                }
                else {
                    symbolTable.put(label, new Integer(addr));
                }
            }

            int cnt = pl.getDataCount();
            if (cnt == 0) {
                cnt = 1;
            }

            if (directive.equalsIgnoreCase(".WORD")) {
                // Add in the number of words.
                addr = addr + 4 * cnt;
            }
            else if (directive.equalsIgnoreCase(".HALF")) {
                // Add in the number of half words and round
                // up to the nearest word. The data will be
                // padded out with 0's.
                addr = addr + 2 * cnt + ((2 * cnt) % 4);
            }
            else if (directive.equalsIgnoreCase(".BYTE")) {
                // Add in the number of bytes and round up
                // to the nearest word. The data will be
                // padded out with 0's.
                addr = addr + cnt;
                if ((cnt % 4) != 0) {
                    addr = addr + (4 - (cnt % 4));
                }
            }
            else if (directive.equalsIgnoreCase(".SPACE")) {
                // Add in the number of bytes and round up
                // to the nearest word. The data will be
                // padded out with 0's.
                cnt = pl.getData(0);
                addr = addr + cnt;
                if ((cnt % 4) != 0) {
                    addr = addr + (4 - (cnt % 4));
                }
            }
            else if (directive.equalsIgnoreCase(".BREAK")) {
            }
            else if (directive.equalsIgnoreCase(".STACKSIZE")) {
            }
            else {
                Instructions.printError(pl, "Unknown assembler directive "
                        + directive + " on line " + pl.getLineNum()
                        + " in file " + curFile + ".");
                errorCnt++;
            }
        }

        // *****************************
        // STACK goes after all variables...
        // Set STACK label in symbol table to correct value and update
        // the value of addr by the size of the stack.
        if (symbolTable.containsKey("STACK")) {
            int cnt = ((Integer) (symbolTable.get("STACK"))).intValue();
            symbolTable.remove("STACK");
            symbolTable.put("STACK", new Integer(addr));
            addr = addr + cnt;
            if ((cnt % 4) != 0) {
                addr = addr + (4 - (cnt % 4));
            }
        }

        return errorCnt;
    }

    private static int passTwo(Hashtable symbolTable, AsmFileReader progFile,
            PrintWriter exeFile) {

        int errorCnt = 0;
        ParsedLine pl;
        Vector line;
        Vector pendingDirectives = new Vector();
        int addr = 0;
        int stackAddr = -1;
        int stackSpace = -1;

        String curFile = "";
        Hashtable lineNumHack = new Hashtable();

        line = progFile.nextLine();
        while (line != null) {

            // Parse the line from the assembler file...
            pl = new ParsedLine(line, curFile);

            // If it is an assembler directive we'll have to save it
            // for later... so just add it to a vector and we'll
            // process it at the end of the pass.
            if (pl.getDirective() != null) {
                if (pl.getDirective().equalsIgnoreCase(".BREAK")) {
                    exeFile.println("* break");
                    exeFile.println("** break");
                    exeFile.println("00001111000011110000111100001111");
                    addr = addr + 4;
                }
                else if (pl.getDirective().equalsIgnoreCase(".STACKSIZE")) {
                    // ****************
                    // else if directive is .stacksize write instructions to
                    // set R13 to the top of the stack and update addr.

                    stackSpace = pl.getData(0);
                    stackAddr = ((Integer) (symbolTable.get("STACK")))
                            .intValue()
                            + stackSpace - 4;
                    String stackAddrStr = binString(stackAddr, 32);

                    exeFile.println("* .stacksize");

                    // LOADi
                    exeFile
                            .println("** LOADi R13 "
                                    + stackAddrStr.substring(16, stackAddrStr
                                            .length()));
                    exeFile
                            .println("10010011"
                                    + "0000"
                                    + "1101"
                                    + stackAddrStr.substring(16, stackAddrStr
                                            .length()));

                    // LOADUi
                    exeFile.println("** LOADUi R13 "
                            + stackAddrStr.substring(0, 16));
                    exeFile.println("10011001" + "0000" + "1101"
                            + stackAddrStr.substring(0, 16));

                    addr = addr + 8;
                }
                else if (pl.getDirective().equalsIgnoreCase(".FILECHANGE")) {
                    lineNumHack
                            .put(curFile, new Integer(progFile.getLineNum()));
                    curFile = pl.getOperand(0);

                    Integer lineNum = (Integer) lineNumHack.get(curFile);
                    if (lineNum != null) {
                        progFile.setLineNum(lineNum.intValue());
                    }
                    else {
                        progFile.setLineNum(0);
                    }

                }
                else {
                    pendingDirectives.add(pl);
                }
            }
            else {
                addr = Instructions.generate(pl, curFile, exeFile, symbolTable,
                        addr);
            }

            // Count up the errors - undefined symbols.
            if (pl.getError()) {
                errorCnt++;
            }

            // Get the next line of the file.
            line = progFile.nextLine();
        }

        // Now process the directives and store the data
        // directly on top of the program text.
        // NOTE: .break and .stacksize are never added to the pendingDirectives
        // list.
        for (int i = 0; i < pendingDirectives.size(); i++) {
            pl = (ParsedLine) pendingDirectives.elementAt(i);

            String label = pl.getLabel();
            String directive = pl.getDirective();

            if (label != null) {
                exeFile.println("* " + label + " " + directive);
            }

            int cnt = pl.getDataCount();

            if (directive.equalsIgnoreCase(".WORD")) {
                if (cnt == 0) {
                    exeFile.println(binString(0, 32));
                }
                else {
                    for (int j = 1; j <= cnt; j++) {
                        exeFile.println(binString(pl.getData(j - 1), 32));
                    }
                }
            }
            else if (directive.equalsIgnoreCase(".HALF")) {
                if (cnt == 0) {
                    exeFile.println(binString(0, 32));
                }
                else {
                    for (int j = 1; j <= cnt; j++) {
                        exeFile.print(binString(pl.getData(j - 1), 16));
                        if (j % 2 == 0) {
                            exeFile.println();
                        }
                    }
                    for (int j = 0; j < (2 * cnt % 4) / 2; j++) {
                        exeFile.println(binString(0, 16));
                    }
                }
            }
            else if (directive.equalsIgnoreCase(".BYTE")) {
                if (cnt == 0) {
                    exeFile.println(binString(0, 32));
                }
                else {
                    for (int j = 1; j <= cnt; j++) {
                        exeFile.print(binString(pl.getData(j - 1), 8));
                        if (j % 4 == 0) {
                            exeFile.println();
                        }
                    }
                    if (cnt % 4 != 0) {
                        for (int j = 0; j < (4 - (cnt % 4)); j++) {
                            exeFile.print(binString(0, 8));
                        }
                        exeFile.println();
                    }
                }
            }
            else if (directive.equalsIgnoreCase(".SPACE")) {
                for (int j = 1; j <= ((pl.getData(0) + 3) / 4); j++) {
                    exeFile.println(binString(0, 32));
                }
            }
            else {
                Instructions.printError(pl, "Unknown assembler directive "
                        + directive + ".");
                errorCnt++;
                System.out.println("Crap! - This shouldn't happen!");
            }
        }

        // ***************************
        // Put in the data for the stack... initialize to zero.
        if (symbolTable.containsKey("STACK")) {
            exeFile.println("* STACK .stackspace " + stackSpace);
            for (int j = 1; j <= ((stackSpace + 3) / 4); j++) {
                exeFile.println(binString(0, 32));
            }
        }

        return errorCnt;
    }

    public static String binString(int val, int bits) {
        String bin = Integer.toBinaryString(val);

        if (bin.length() > bits) {
            // Need this to handle negative numbers!
            bin = bin.substring(bin.length() - bits, bin.length());
        }
        else {
            for (int i = bin.length(); i < bits; i++) {
                bin = "0" + bin;
            }
        }

        return bin;
    }

    /**
     * Main method for assembler. This method expects two command line arguments
     * from the user: The name of the file to assemble and the name of the
     * executable file to produce.
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java Assembler <source> <executable>");
            System.out.println();
            System.out
                    .println("          <source> : Name of the source file to assemble.");
            System.out
                    .println("       <executable>: Name of the executable file to produce.");
        }
        else if (args[1].equals(args[0])) {
            System.out.println("  <source> and <executable> files are the same.");
            System.out.println("  This would overwrite the <source> file.");
            System.out.println("  Choose a different name for the <executable>.");
        }
        else {

            System.out.println("Assembling " + args[0] + " into " + args[1]
                    + ".");

            // This assembler needs an include directive for importing
            // subroutines....
            //
            // In here just scan the files for .include directives and
            // produce a new file that holds all of the included files.
            // Then assemble that combined file.

            // Need to be aware of circular includes. That is a file that
            // includes a file that is part of the chain of includes that lead
            // up to it being included. Keep a Vector of filenames of the
            // current sequence of includes... if an include is in that Vector
            // then it is illegal.

            // Have include directive specify a prefix for the labels
            // in that file? So .include <file> <prefix>
            // e.g. .include mult.asm MULT
            // Then the call statement would be CALL MULT_MULT
            // All labels in the file will be prefixed with the specified prefix
            // ensuring that they are unique within the overall file. Could do
            // this during pass 0 and just write them that way to the tmp file.

            // Also look for other directives such as .word etc. that
            // should be disallowed in included files.

            // For error reporting use a pseudo directive .filechange <file>
            // that specifies the file from which the following code has
            // come.
            System.out.println("Pass 0: Processing included files...");
            AsmFileReader r = new AsmFileReader(args[0]);
            Vector includeFiles = new Vector();

            try {
                PrintWriter tmpFile = new PrintWriter(new FileWriter(args[0]
                        + ".full"));
                boolean error = passZero(r, tmpFile, "", includeFiles);
                tmpFile.close();

                if (error) {
                    System.out.println("Errors processing included files.");
                    System.out.println("Executable not produced");
                    System.exit(-1);
                }
            }
            catch (IOException e) {
                System.out.println("Unable to create temporary file.");
                System.out.println("Executable not produced");
                System.exit(-1);
            }

            System.out.println("Pass 1: Building symbol table...");

            r = new AsmFileReader(args[0] + ".full");
            Hashtable symbols = new Hashtable();

            // Initialize the symbol table to contain labels for
            // STDIN=0xFFFFFFFC and STDOUT=0xFFFFFFF8
            symbols.put("STDIN", new Integer(-4));
            symbols.put("STDOUT", new Integer(-8));

            int errorCnt = Assembler.passOne(symbols, r);

            if (errorCnt != 0) {
                System.out.println(errorCnt + " lines with errors.");
                System.out.println("Executable not produced.");
            }
            else {
                try {
                    PrintWriter exeFile = new PrintWriter(new FileWriter(
                            args[1]));
                    r = new AsmFileReader(args[0] + ".full");

                    System.out
                            .println("Pass 2: Translating code to machine language...");

                    errorCnt = Assembler.passTwo(symbols, r, exeFile);
                    exeFile.close();

                    if (errorCnt != 0) {
                        System.out.println(errorCnt + " lines with errors.");
                        System.out.println("Executable not produced.");
                        new File(args[1]).delete();
                    }
                    else {
                        System.out.println("Assembly complete.");
                    }
                }
                catch (IOException e) {
                    System.out.println("Unable to create executable file: "
                            + args[1] + ".");
                }
            }
        }
    }
}

