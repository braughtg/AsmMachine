/**
 * Instructions.java Handle everything to do with the particular instructions
 * here. This class is filled with static private methods that know about the
 * instructions. There is a lot of hard coding going on in this file but it is
 * unadvoidable.
 * 
 * This implementation is really bad! The correct way to do this would be to
 * declare an instruction interface that has parse and generate methods and
 * then have a class for each instruction that implements that interface. The
 * huge if statements go away via polymorphism. Instruction may want to be a
 * class with a few abstract methods so that common functionality can be
 * factored out into class methods.
 * 
 * This means lots of classes but it also means that extending the system to
 * have a new instruction is just a matter of adding a new class.
 * 
 * @author Grant William Braught
 * @author Dickinson College
 * @version 8/21/2000
 */

import java.io.*;
import java.util.*;

class Instructions {

    public static final int ADDR_IMMEDIATE = 1;

    public static final int ADDR_IMMEDIATE_LABEL = 2;

    public static final int ADDR_DIRECT = 3;

    public static final int ADDR_INDIRECT = 4;

    public static final int ADDR_STACK = 5;

    //private static final int MAX_IMMEDIATE = (int)(Math.pow(2,16)) - 1;

    private static final int MAX_OFFSET = (int) (Math.pow(2, 15)) - 1;

    private static final int MIN_OFFSET = -(int) (Math.pow(2, 15));

    public static void parse(Vector rawLine, ParsedLine line, String srcFile) {

        // If there is an instruction code it must be
        // in element 1 of the raw line. The line number from
        // the source file is in location 0. If there was a label
        // it is already in the ParsedLine and has been removed
        // from the rawLine.
        try {
            String instCode = (String) (rawLine.elementAt(1));

            if (instCode.equalsIgnoreCase("ADD") || instCode.equalsIgnoreCase("SUB")
                    || instCode.equalsIgnoreCase("AND") || instCode.equalsIgnoreCase("OR")) {
                ADD_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("MOV") || instCode.equalsIgnoreCase("NOT")
                    || instCode.equalsIgnoreCase("SHL") || instCode.equalsIgnoreCase("SHR")) {
                NOT_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BPOS")) {
                BPOS_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BODD") || instCode.equalsIgnoreCase("BZERO")
                    || instCode.equalsIgnoreCase("BNEG")) {
                BODD_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BNZERO") || instCode.equalsIgnoreCase("BEVEN")) {
                BNZERO_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BGEQ") || instCode.equalsIgnoreCase("BLEQ")
                    || instCode.equalsIgnoreCase("BGT") || instCode.equalsIgnoreCase("BLT")
                    || instCode.equalsIgnoreCase("BEQ") || instCode.equalsIgnoreCase("BNEQ")) {
                BGEQ_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("JUMP")) {
                JUMP_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("CALL")) {
                CALL_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("RET")) {
                RET_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("PUSH") || instCode.equalsIgnoreCase("POP")) {
                PUSH_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("NOP") || instCode.equalsIgnoreCase("HALT")) {
                NOP_Parse(rawLine, line, srcFile);
            }
            else if (instCode.equalsIgnoreCase("LOAD") || instCode.equalsIgnoreCase("STORE")) {
                LOAD_Parse(rawLine, line, srcFile);
            }
            else if (!instCode.equalsIgnoreCase("")) {
                printError(line, "Unknown instruction in file " + srcFile + ".");
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            printError(line, "Unknown error parsing instruction in file " 
                    + srcFile + ".");
        }
    }

    public static int generate(ParsedLine line, String srcFile, PrintWriter exeFile,
            Hashtable symbols, int addr) {

        try {
            String instCode = line.getInst();

            if (instCode.equalsIgnoreCase("ADD")) {
                addr = addr + 4 * ADD_Generate(line, "0011", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("SUB")) {
                addr = addr + 4 * ADD_Generate(line, "1111", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("AND")) {
                addr = addr + 4 * ADD_Generate(line, "0010", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("OR")) {
                addr = addr + 4 * ADD_Generate(line, "0001", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("MOV")) {
                addr = addr + 4 * NOT_Generate(line, "1001", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("NOT")) {
                addr = addr + 4 * NOT_Generate(line, "0100", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("SHL")) {
                addr = addr + 4 * NOT_Generate(line, "0010", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("SHR")) {
                addr = addr + 4 * NOT_Generate(line, "0001", exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BODD")) {
                addr = addr + 4
                        * BODD_Generate(line, "0011", exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BZERO")) {
                addr = addr + 4
                        * BODD_Generate(line, "0001", exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BNEG")) {
                addr = addr + 4
                        * BODD_Generate(line, "0010", exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BPOS")) {
                addr = addr + 4 * BPOS_Generate(line, exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BNZERO")) {
                addr = addr + 4 * BNZERO_Generate(line, exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BEVEN")) {
                addr = addr + 4 * BEVEN_Generate(line, exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BGEQ") || instCode.equalsIgnoreCase("BLEQ")) {
                addr = addr + 4 * BGEQ_Generate(line, exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BGT") || instCode.equalsIgnoreCase("BLT")) {
                addr = addr + 4 * BGT_Generate(line, exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BEQ")) {
                addr = addr + 4 * BEQ_Generate(line, exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("BNEQ")) {
                addr = addr + 4 * BNEQ_Generate(line, exeFile, symbols, addr, srcFile);
            }
            else if (instCode.equalsIgnoreCase("JUMP")) {
                addr = addr + 4 * JUMP_Generate(line, exeFile, symbols, srcFile);
            }
            else if (instCode.equalsIgnoreCase("CALL")) {
                addr = addr + 4 * CALL_Generate(line, exeFile, symbols, srcFile);
            }
            else if (instCode.equalsIgnoreCase("RET")) {
                addr = addr + 4 * RET_Generate(line, exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("NOP")) {
                addr = addr + 4 * NOP_Generate(line, exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("HALT")) {
                addr = addr + 4 * HALT_Generate(line, exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("PUSH")) {
                addr = addr + 4 * PUSH_Generate(line, exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("POP")) {
                addr = addr + 4 * POP_Generate(line, exeFile, srcFile);
            }
            else if (instCode.equalsIgnoreCase("LOAD")) {
                addr = addr + 4 * LOAD_Generate(line, exeFile, "0001", symbols, srcFile);
            }
            else if (instCode.equalsIgnoreCase("STORE")) {
                addr = addr + 4 * LOAD_Generate(line, exeFile, "0010", symbols, srcFile);
            }
            else {
                printError(line, "Unknown instruction: " + line.getInst() + 
                        " in file " + srcFile + ".");
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            printError(line, "Crap! - This shouldn't happen.");
        }

        return addr;
    }

    /** ************************************************* */

    private static int ADD_Generate(ParsedLine line, String opcode,
            PrintWriter exeFile, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ":");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1) + " " + line.getOperand(2));

        int mode = line.getAddrMode();

        if (mode == ADDR_IMMEDIATE) {

            String o3 = line.getOperand(2);
            long io3 = ParsedLine.getNumValue(o3.substring(1, o3.length()));

            if (io3 > MAX_OFFSET || io3 < MIN_OFFSET) {
                String bo3 = imm2Bin32(o3);
                // LOADi
                exeFile.println("** LOADi R15 "
                        + bo3.substring(16, bo3.length()));
                exeFile.println("10010011" + "0000" + "1111"
                        + bo3.substring(16, bo3.length()));

                // LOADUi
                exeFile.println("** LOADUi R15 " + bo3.substring(0, 16));
                exeFile.println("10011001" + "0000" + "1111"
                        + bo3.substring(0, 16));

                // ADD
                exeFile.println("** " + line.getInst() + " "
                        + line.getOperand(0) + " " + line.getOperand(1)
                        + " R15");
                exeFile.println("0010" + opcode + reg2Bin(line.getOperand(1))
                        + "1111" + reg2Bin(line.getOperand(0))
                        + Assembler.binString(0, 12));
                return 3;
            }
            else {
                // ADDi
                exeFile.println("** " + line.getInst() + "i "
                        + line.getOperand(0) + " " + line.getOperand(1) + " "
                        + line.getOperand(2));
                exeFile.println("1011" + opcode + reg2Bin(line.getOperand(1))
                        + reg2Bin(line.getOperand(0))
                        + imm2Bin16(line.getOperand(2)));
                return 1;
            }
        }
        else if (mode == ADDR_DIRECT) {
            // ADD
            exeFile.println("** " + line.getInst() + " " + line.getOperand(0)
                    + " " + line.getOperand(1) + " " + line.getOperand(2));
            exeFile.println("0010" + opcode + reg2Bin(line.getOperand(1))
                    + reg2Bin(line.getOperand(2)) + reg2Bin(line.getOperand(0))
                    + Assembler.binString(0, 12));
            return 1;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int NOT_Generate(ParsedLine line, String opcode,
            PrintWriter exeFile, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {
            // NOT
            exeFile.println("** " + line.getInst() + " " + line.getOperand(0)
                    + " " + line.getOperand(1));
            exeFile.println("0110" + opcode + "0000"
                    + reg2Bin(line.getOperand(1)) + reg2Bin(line.getOperand(0))
                    + Assembler.binString(0, 12));
            return 1;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int BODD_Generate(ParsedLine line, String opcode,
            PrintWriter exeFile, Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(1));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(1) 
                        + " in file " + srcFile + ".");
            }
            else {
                int offset = label.intValue() - (addr);
                if ((offset % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(1)
                            + " in file " + srcFile + ".");
                }
                else if (offset / 4 > MAX_OFFSET || offset / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset = offset / 4;
                    // BODD
                    exeFile.println("** " + line.getInst() + " "
                            + line.getOperand(0) + " " + offset);
                    exeFile.println("1110" + opcode + "0000"
                            + reg2Bin(line.getOperand(0))
                            + Assembler.binString(offset, 16));

                    return 1;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile +  ".");
        }

        return 0;
    }

    private static int BPOS_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(1));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(1) 
                        + " in file " + srcFile +  ".");
            }
            else {
                // Need the + 8 to account for the expansion in this
                // instruction.
                int offset = label.intValue() - (addr + 8);
                if ((offset % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(1)
                            + " in file " + srcFile + ".");
                }
                else if (offset / 4 > MAX_OFFSET || offset / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset = offset / 4;
                    // BNEG
                    exeFile.println("** BNEG " + line.getOperand(0) + " 3");
                    exeFile.println("1110" + "0010" + "0000"
                            + reg2Bin(line.getOperand(0))
                            + Assembler.binString(3, 16));
                    // BZERO
                    exeFile.println("** BZERO " + line.getOperand(0) + " 2");
                    exeFile.println("1110" + "0001" + "0000"
                            + reg2Bin(line.getOperand(0))
                            + Assembler.binString(2, 16));
                    // BRANCH
                    exeFile.println("** BRANCH " + offset);
                    exeFile.println("1100" + "0001" + "0000" + "0000"
                            + Assembler.binString(offset, 16));

                    return 3;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int BNZERO_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(1));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(1) 
                        + " in file " + srcFile + ".");
            }
            else {
                // Need the + 4 to account for the expansion in this
                // instruction.
                int offset = label.intValue() - (addr + 4);
                if ((offset % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(1)
                            + " in file " + srcFile + ".");
                }
                else if (offset / 4 > MAX_OFFSET || offset / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset = offset / 4;
                    // BZERO
                    exeFile.println("** BZERO " + line.getOperand(0) + " 2");
                    exeFile.println("1110" + "0001" + "0000"
                            + reg2Bin(line.getOperand(0))
                            + Assembler.binString(2, 16));
                    // BRANCH
                    exeFile.println("** BRANCH " + offset);
                    exeFile.println("1100" + "0001" + "0000" + "0000"
                            + Assembler.binString(offset, 16));

                    return 2;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int BEVEN_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(1));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(1) 
                        + " in file " + srcFile + ".");
            }
            else {
                // Need the + 4 to account for the expansion in this
                // instruction.
                int offset = label.intValue() - (addr + 4);
                if ((offset % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(1)
                            + " in file " + srcFile + ".");
                }
                else if (offset / 4 > MAX_OFFSET || offset / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset = offset / 4;
                    // BODD
                    exeFile.println("** BODD " + line.getOperand(0) + " 2");
                    exeFile.println("1110" + "0011" + "0000"
                            + reg2Bin(line.getOperand(0))
                            + Assembler.binString(2, 16));
                    // BRANCH
                    exeFile.println("** BRANCH " + offset);
                    exeFile.println("1100" + "0001" + "0000" + "0000"
                            + Assembler.binString(offset, 16));

                    return 2;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int BGEQ_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1) + " " + line.getOperand(2));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(2));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(2) 
                        + " in file " + srcFile + ".");
            }
            else {
                // Need the + 8 to account for the expansion in this
                // instruction.
                int offset1 = label.intValue() - (addr + 4);
                int offset2 = label.intValue() - (addr + 8);

                if ((offset1 % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(2)
                            + " in file " + srcFile + ".");
                }
                else if (offset1 / 4 > MAX_OFFSET || offset1 / 4 < MIN_OFFSET
                        || offset2 / 4 > MAX_OFFSET || offset2 / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset1 = offset1 / 4;
                    offset2 = offset2 / 4;

                    // SUB
                    if (line.getInst().equalsIgnoreCase("BGEQ")) {
                        exeFile.println("** SUB R15 " + line.getOperand(1)
                                + " " + line.getOperand(0));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(1))
                                + reg2Bin(line.getOperand(0)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    else {
                        exeFile.println("** SUB R15 " + line.getOperand(0)
                                + " " + line.getOperand(1));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(0))
                                + reg2Bin(line.getOperand(1)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    // BNEG
                    exeFile.println("** BNEG R15 " + offset1);
                    exeFile.println("1110" + "0010" + "0000" + "1111"
                            + Assembler.binString(offset1, 16));
                    // BZERO
                    exeFile.println("** BZERO R15 " + offset2);
                    exeFile.println("1110" + "0001" + "0000" + "1111"
                            + Assembler.binString(offset2, 16));

                    return 3;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int BGT_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1) + " " + line.getOperand(2));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(2));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(2) 
                        + " in file " + srcFile + ".");
            }
            else {
                // Need the + 4 to account for the expansion in this
                // instruction.
                int offset = label.intValue() - (addr + 4);
                if ((offset % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(1)
                            + " in file " + srcFile + ".");
                }
                else if (offset / 4 > MAX_OFFSET || offset / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset = offset / 4;

                    // SUB
                    if (line.getInst().equalsIgnoreCase("BGT")) {
                        exeFile.println("** SUB R15 " + line.getOperand(1)
                                + " " + line.getOperand(0));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(1))
                                + reg2Bin(line.getOperand(0)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    else {
                        exeFile.println("** SUB R15 " + line.getOperand(0)
                                + " " + line.getOperand(1));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(0))
                                + reg2Bin(line.getOperand(1)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    // BNEG
                    exeFile.println("** BNEG R15 " + offset);
                    exeFile.println("1110" + "0010" + "0000" + "1111"
                            + Assembler.binString(offset, 16));

                    return 2;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int BEQ_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1) + " " + line.getOperand(2));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(2));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(2) 
                        + " in file " + srcFile + ".");
            }
            else {
                // Need the + 4 to account for the expansion in this
                // instruction.
                int offset = label.intValue() - (addr + 4);
                if ((offset % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(1)
                            + " in file " + srcFile + ".");
                }
                else if (offset / 4 > MAX_OFFSET || offset / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset = offset / 4;

                    // SUB
                    if (line.getInst().equalsIgnoreCase("BGT")) {
                        exeFile.println("** SUB R15 " + line.getOperand(1)
                                + " " + line.getOperand(0));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(1))
                                + reg2Bin(line.getOperand(0)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    else {
                        exeFile.println("** SUB R15 " + line.getOperand(0)
                                + " " + line.getOperand(1));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(0))
                                + reg2Bin(line.getOperand(1)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    // BZERO
                    exeFile.println("** BZERO R15 " + offset);
                    exeFile.println("1110" + "0001" + "0000" + "1111"
                            + Assembler.binString(offset, 16));

                    return 2;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int BNEQ_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, int addr, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1) + " " + line.getOperand(2));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(2));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(2) 
                        + " in file " + srcFile + ".");
            }
            else {
                // Need the + 8 to account for the expansion in this
                // instruction.
                int offset = label.intValue() - (addr + 8);

                if ((offset % 4) != 0) {
                    printError(line, "Invalid branch label "
                            + line.getOperand(2)
                            + " in file " + srcFile + ".");
                }
                else if (offset / 4 > MAX_OFFSET || offset / 4 < MIN_OFFSET) {
                    printError(line, "Branch target too far away, "
                            + " must be within 2^16 ML instructions"
                            + " in file " + srcFile + ".");
                }
                else {
                    offset = offset / 4;

                    // SUB
                    if (line.getInst().equalsIgnoreCase("BGEQ")) {
                        exeFile.println("** SUB R15 " + line.getOperand(1)
                                + " " + line.getOperand(0));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(1))
                                + reg2Bin(line.getOperand(0)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    else {
                        exeFile.println("** SUB R15 " + line.getOperand(0)
                                + " " + line.getOperand(1));
                        exeFile.println("0010" + "1111"
                                + reg2Bin(line.getOperand(0))
                                + reg2Bin(line.getOperand(1)) + "1111"
                                + Assembler.binString(0, 12));
                    }
                    // BZERO
                    exeFile.println("** BZERO R15 2");
                    exeFile.println("1110" + "0001" + "0000" + "1111"
                            + Assembler.binString(2, 16));
                    // BRANCH
                    exeFile.println("** BRANCH R15 " + offset);
                    exeFile.println("1100" + "0001" + "0000" + "1111"
                            + Assembler.binString(offset, 16));

                    return 3;
                }
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int JUMP_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {
            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(0));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(0) 
                        + " in file " + srcFile + ".");
            }
            else {
                int target = label.intValue();
                if ((target % 4) != 0) {
                    printError(line, "Invalid jump label " + line.getOperand(0)
                            + " in file " + srcFile + ".");
                }
                String targetStr = Assembler.binString(target, 32);

                // LOADi
                exeFile.println("** LOADi R15 "
                        + targetStr.substring(16, targetStr.length()));
                exeFile.println("10010011" + "0000" + "1111"
                        + targetStr.substring(16, targetStr.length()));
                // LOADUi
                exeFile.println("** LOADUi R15 " + targetStr.substring(0, 16));
                exeFile.println("10011001" + "0000" + "1111"
                        + targetStr.substring(0, 16));
                // JREG
                exeFile.println("** JREG R15");
                exeFile.println("1110" + "0100" + "0000" + "1111"
                        + Assembler.binString(0, 16));
                return 3;
            }
        }
        else if (mode == ADDR_INDIRECT) {
            // JREG
            exeFile.println("** JREG " + line.getOperand(0));
            exeFile.println("1110" + "0100" + "0000"
                    + reg2Bin(line.getOperand(0)) + Assembler.binString(0, 16));
            return 1;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int CALL_Generate(ParsedLine line, PrintWriter exeFile,
            Hashtable symbols, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ":");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {
            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(0));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(0) 
                        + " in file " + srcFile + ".");
            }
            else {
                int target = label.intValue();
                if ((target % 4) != 0) {
                    printError(line, "Invalid call label " + line.getOperand(0)
                            + " in file " + srcFile + ".");
                }
                String targetStr = Assembler.binString(target, 32);

                // LOADi
                exeFile.println("** LOADi R15 "
                        + targetStr.substring(16, targetStr.length()));
                exeFile.println("10010011" + "0000" + "1111"
                        + targetStr.substring(16, targetStr.length()));
                // LOADUi
                exeFile.println("** LOADUi R15 " + targetStr.substring(0, 16));
                exeFile.println("10011001" + "0000" + "1111"
                        + targetStr.substring(0, 16));
                // JAL
                exeFile.println("** JAL R15 R12");
                exeFile.println("0110" + "1000" + "0000" + "1111" + "1100"
                        + Assembler.binString(0, 12));
                return 3;
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int RET_Generate(ParsedLine line, PrintWriter exeFile, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst());

        int mode = line.getAddrMode();

        if (mode == ADDR_IMMEDIATE) {
            // JREG
            exeFile.println("** JREG R12");
            exeFile.println("1110" + "0100" + "0000" + "1100"
                    + Assembler.binString(0, 16));
            return 1;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int NOP_Generate(ParsedLine line, PrintWriter exeFile, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst());

        int mode = line.getAddrMode();

        if (mode == ADDR_IMMEDIATE) {
            // NOP
            exeFile.println("** NOP");
            exeFile.println(Assembler.binString(0, 32));
            return 1;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int HALT_Generate(ParsedLine line, PrintWriter exeFile, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst());

        int mode = line.getAddrMode();

        if (mode == ADDR_IMMEDIATE) {
            // HALT
            exeFile.println("** HALT");
            exeFile.println(Assembler.binString(-1, 32));
            return 1;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int PUSH_Generate(ParsedLine line, PrintWriter exeFile, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {
            // STORE
            exeFile.println("** STORE R13 " + line.getOperand(0));
            exeFile.println("0100" + "0010" + "1101"
                    + reg2Bin(line.getOperand(0)) + Assembler.binString(0, 16));
            // SUBi
            exeFile.println("** SUBi R13 R13 4");
            exeFile.println("1011" + "1111" + "1101" + "1101"
                    + Assembler.binString(4, 16));

            return 2;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int POP_Generate(ParsedLine line, PrintWriter exeFile, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.println(line.getInst() + " " + line.getOperand(0));

        int mode = line.getAddrMode();

        if (mode == ADDR_DIRECT) {
            // ADDi
            exeFile.println("** ADDi R13 R13 4");
            exeFile.println("1011" + "0011" + "1101" + "1101"
                    + Assembler.binString(4, 16));
            // LOAD
            exeFile.println("** LOAD " + line.getOperand(0) + " R13");
            exeFile.println("0100" + "0001" + "1101"
                    + reg2Bin(line.getOperand(0)) + Assembler.binString(0, 16));

            return 2;
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    private static int LOAD_Generate(ParsedLine line, PrintWriter exeFile,
            String opcode, Hashtable symbols, String srcFile) {

        exeFile.print("* ");
        if (line.getLabel() != null) {
            exeFile.print(line.getLabel() + ": ");
        }
        exeFile.print(line.getInst() + " " + line.getOperand(0) + " "
                + line.getOperand(1));

        int mode = line.getAddrMode();

        if (mode == ADDR_IMMEDIATE) {
            exeFile.println();
            String o2 = line.getOperand(1);
            long io2 = ParsedLine.getNumValue(o2.substring(1, o2.length()));

            if (io2 > MAX_OFFSET || io2 < MIN_OFFSET) {
                String io2b = Assembler.binString((int) io2, 32);

                // LOADi
                exeFile.println("** LOADi " + line.getOperand(0) + " "
                        + io2b.substring(16, io2b.length()));
                exeFile.println("1001" + "0011" + "0000"
                        + reg2Bin(line.getOperand(0))
                        + io2b.substring(16, io2b.length()));
                // LOADUi
                exeFile.println("** LOADUi " + line.getOperand(0) + " "
                        + io2b.substring(0, 16));
                exeFile.println("1001" + "1001" + "0000"
                        + reg2Bin(line.getOperand(0)) + io2b.substring(0, 16));

                return 2;
            }
            else {
                String io2b = Assembler.binString((int) io2, 16);
                // LOADi
                exeFile.println("** LOADi " + line.getOperand(0) + " "
                        + Assembler.binString((int) io2, 16));
                exeFile.println("1001" + "0011" + "0000"
                        + reg2Bin(line.getOperand(0))
                        + Assembler.binString((int) io2, 16));
                return 1;
            }
        }
        else if (mode == ADDR_IMMEDIATE_LABEL) {
            // Look up the symbol and do some math..
            String op2 = line.getOperand(1);
            Integer label = (Integer) symbols.get(op2
                    .substring(1, op2.length()));
            if (label == null) {
                printError(line, "Undefined label " + op2 
                        + " in file " + srcFile + ".");
            }
            else {
                int target = label.intValue();
                exeFile.println();

                String targetStr = Assembler.binString(target, 32);

                // LOADi
                exeFile.println("** LOADi " + line.getOperand(0) + " "
                        + targetStr.substring(16, targetStr.length()));
                exeFile.println("1001" + "0011" + "0000"
                        + reg2Bin(line.getOperand(0))
                        + targetStr.substring(16, targetStr.length()));
                // LOADUi
                exeFile.println("** LOADUi " + line.getOperand(0) + " "
                        + targetStr.substring(0, 16));
                exeFile.println("1001" + "1001" + "0000"
                        + reg2Bin(line.getOperand(0))
                        + targetStr.substring(0, 16));

                return 2;
            }
        }
        else if (mode == ADDR_INDIRECT) {
            exeFile.println();

            // LOAD/STORE
            exeFile.println("** " + line.getInst() + " " + line.getOperand(0)
                    + " " + line.getOperand(1));
            exeFile.println("0100" + opcode + reg2Bin(line.getOperand(1))
                    + reg2Bin(line.getOperand(0)) + Assembler.binString(0, 16));

            return 1;
        }
        else if (mode == ADDR_STACK) {
            exeFile.println(" " + line.getOperand(2));

            // LOAD/STORE
            exeFile.println("** " + line.getInst() + " " + line.getOperand(0)
                    + " " + line.getOperand(1) + " " + line.getOperand(2));
            exeFile.println("0100" + opcode + reg2Bin(line.getOperand(1))
                    + reg2Bin(line.getOperand(0))
                    + imm2Bin16(line.getOperand(2)));

            return 1;
        }
        else if (mode == ADDR_DIRECT) {
            exeFile.println();

            // Look up the symbol and do some math..
            Integer label = (Integer) symbols.get(line.getOperand(1));
            if (label == null) {
                printError(line, "Undefined label " + line.getOperand(1) 
                        + " in file " + srcFile + ".");
            }
            else {
                int target = label.intValue();
                if ((target % 4) != 0) {
                    printError(line, "Invalid load label " + line.getOperand(1)
                            + " in file " + srcFile + ".");
                }
                String targetStr = Assembler.binString(target, 32);

                // LOADi
                exeFile.println("** LOADi R15 "
                        + targetStr.substring(16, targetStr.length()));
                exeFile.println("1001" + "0011" + "0000" + "1111"
                        + targetStr.substring(16, targetStr.length()));
                // LOADUi
                exeFile.println("** LOADUi R15 " + targetStr.substring(0, 16));
                exeFile.println("1001" + "1001" + "0000" + "1111"
                        + targetStr.substring(0, 16));
                // LOAD/STORE
                exeFile.println("** " + line.getInst() + " "
                        + line.getOperand(0) + " R15 "
                        + Assembler.binString(0, 16));
                exeFile.println("0100" + opcode + "1111"
                        + reg2Bin(line.getOperand(0))
                        + Assembler.binString(0, 16));

                return 3;
            }
        }
        else {
            printError(line, "Invalid addressing mode for " + line.getInst()
                    + " in file " + srcFile + ".");
        }

        return 0;
    }

    /** ************************************************* */

    private static void ADD_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 5) {
            printError(line, "3 operands required" 
                    + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));
            String o2 = (String) (rawLine.elementAt(3));
            String o3 = (String) (rawLine.elementAt(4));

            if (!validRegister(o1) || !validRegister(o2)) {
                printError(line, "First two operands must "
                        + "be registers [R0-R15]"
                        + " in file " + srcFile + ".");
            }
            else {
                line.setOperand(0, o1);
                line.setOperand(1, o2);
            }

            if (validRegister(o3)) {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(2, o3);
                line.setExpFactor(1);
            }
            else if (validImmediate(o3)) {
                line.setAddrMode(ADDR_IMMEDIATE);
                line.setOperand(2, o3);

                // ADD/SUB etc expand to 3 instructions if the
                // immediate value is too large for 16 bits.
                // The expansion is: LOAD, LOADU, ADD
                if (ParsedLine.getNumValue(o3.substring(1, o3.length())) > MAX_OFFSET
                        || ParsedLine.getNumValue(o3.substring(1, o3.length())) < MIN_OFFSET) {
                    line.setExpFactor(3);
                }
                else {
                    line.setExpFactor(1);
                }
            }
            else {
                printError(line, "Bad third operand"
                        + " in file " + srcFile + ".");
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void NOT_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 4) {
            printError(line, "2 operands required"
                    + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));
            String o2 = (String) (rawLine.elementAt(3));

            if (!validRegister(o1) || !validRegister(o2)) {
                printError(line, "Operands must " + "be registers [R0-R15]"
                        + " in file " + srcFile + ".");
            }
            else {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setOperand(1, o2);
                line.setExpFactor(1);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void BPOS_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        String inst = (String) (rawLine.elementAt(1));
        line.setInst(inst);

        if (rawLine.size() != 4) {
            printError(line, "2 operands required" + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));
            String o2 = (String) (rawLine.elementAt(3));

            if (validRegLabel(line, o1, o2)) {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setOperand(1, o2);
                line.setExpFactor(3);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void BODD_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        String inst = (String) (rawLine.elementAt(1));
        line.setInst(inst);

        if (rawLine.size() != 4) {
            printError(line, "2 operands required" + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));
            String o2 = (String) (rawLine.elementAt(3));

            if (validRegLabel(line, o1, o2)) {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setOperand(1, o2);
                line.setExpFactor(1);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void BNZERO_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        String inst = (String) (rawLine.elementAt(1));
        line.setInst(inst);

        if (rawLine.size() != 4) {
            printError(line, "2 operands required" + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));
            String o2 = (String) (rawLine.elementAt(3));

            if (validRegLabel(line, o1, o2)) {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setOperand(1, o2);
                line.setExpFactor(2);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void BGEQ_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 5) {
            printError(line, "3 operands required" + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));
            String o2 = (String) (rawLine.elementAt(3));
            String o3 = (String) (rawLine.elementAt(4));

            if (!validRegister(o1) || !validRegister(o2)) {
                printError(line, "First two operands must "
                        + "be registers [R0-R15]"
                        + " in file " + srcFile + ".");
            }
            else if (!validLabel(o3)) {
                printError(line, "Third operand must be " + "a label"
                        + " in file " + srcFile + ".");
            }
            else {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setOperand(1, o2);
                line.setOperand(2, o3);

                if (line.getInst().equalsIgnoreCase("BNEQ")
                        || line.getInst().equalsIgnoreCase("BGEQ")
                        || line.getInst().equalsIgnoreCase("BLEQ")) {
                    line.setExpFactor(3);
                }
                else {
                    line.setExpFactor(2);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void JUMP_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 3) {
            printError(line, "1 operand required" 
                    + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));

            if (validRegister(o1)) {
                line.setAddrMode(ADDR_INDIRECT);
                line.setOperand(0, o1);
                line.setExpFactor(1);
            }
            else if (validLabel(o1)) {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setExpFactor(3);
            }
            else {
                printError(line, "Operand must be a"
                        + "register [R0-R15] or a label" 
                        + " in file " + srcFile + ".");
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void CALL_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 3) {
            printError(line, "1 operand required" 
                    + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));

            if (validLabel(o1)) {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setExpFactor(3);
            }
            else {
                printError(line, "Operand must be a label" 
                        + " in file " + srcFile + ".");
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void RET_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 2) {
            printError(line, "No operands permitted" 
                    + " in file " + srcFile + ".");
        }
        line.setAddrMode(ADDR_IMMEDIATE);
        line.setExpFactor(1);
    }

    private static void PUSH_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 3) {
            printError(line, "1 operand required"
                    + " in file " + srcFile + ".");
        }

        try {
            String o1 = (String) (rawLine.elementAt(2));

            if (validRegister(o1)) {
                line.setAddrMode(ADDR_DIRECT);
                line.setOperand(0, o1);
                line.setExpFactor(2);
            }
            else {
                printError(line, "Operand must be a" + "register [R0-R15]"
                        + " in file " + srcFile + ".");
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private static void NOP_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        line.setInst((String) (rawLine.elementAt(1)));

        if (rawLine.size() != 2) {
            printError(line, "No operands permitted" 
                    + " in file " + srcFile + ".");
        }
        line.setAddrMode(ADDR_IMMEDIATE);
        line.setExpFactor(1);
    }

    private static void LOAD_Parse(Vector rawLine, ParsedLine line, String srcFile) {

        String inst = (String) (rawLine.elementAt(1));
        line.setInst(inst);

        if (rawLine.size() < 4) {
            printError(line, "At least two operands required for " + inst
                    + " instruction" 
                    + " in file " + srcFile + ".");
        }
        else {
            String o1 = (String) (rawLine.elementAt(2));
            line.setOperand(0, o1);

            if (!validRegister(o1)) {
                printError(line, "First operand must "
                        + "be a register [R0-R15] for all " + inst
                        + " instructions"
                        + " in file " + srcFile + ".");
            }
            else {
                String o2 = (String) (rawLine.elementAt(3));
                line.setOperand(1, o2);

                // Is it LOAD R # or LOAD R #L
                if (o2.startsWith("#") && inst.equalsIgnoreCase("LOAD")) {
                    if (validImmediate(o2)) {
                        // It's LOAD R #
                        line.setAddrMode(ADDR_IMMEDIATE);

                        if (ParsedLine
                                .getNumValue(o2.substring(1, o2.length())) > MAX_OFFSET
                                || ParsedLine.getNumValue(o2.substring(1, o2
                                        .length())) < MIN_OFFSET) {
                            line.setExpFactor(2);
                        }
                        else {
                            line.setExpFactor(1);
                        }
                    }
                    else {
                        // It's LOAD R #L
                        line.setAddrMode(ADDR_IMMEDIATE_LABEL);
                        line.setExpFactor(2);
                        if (!validLabel(o2.substring(1, o2.length()))) {
                            printError(line, "Label expected following # in "
                                    + "LOAD R #L"
                                    + " in file " + srcFile + ".");
                        }
                    }
                }
                // Is it LOAD/STORE R L
                else if (validLabel(o2)) {
                    // Valid number of operands for this format?
                    if (rawLine.size() != 4) {
                        printError(line, "2 operands expected in " + inst
                                + " R L" 
                                + " in file " + srcFile + ".");
                    }

                    // It is LOAD/STORE R L
                    line.setAddrMode(ADDR_DIRECT);
                    line.setExpFactor(3);
                }
                // Is it LOAD/STORE R R, LOAD/STORE R R +?
                else if (validRegister(o2)) {
                    // Is it LOAD/STORE R R
                    if (rawLine.size() == 4) {
                        line.setAddrMode(ADDR_INDIRECT);
                        line.setExpFactor(1);
                    }
                    else {
                        String o3 = (String) (rawLine.elementAt(4));
                        line.setOperand(2, o3);

                        // Is it LOAD/STORE R R +
                        if (validOffset(o3)) {
                            line.setAddrMode(ADDR_STACK);
                            line.setExpFactor(1);
                        }
                        else {
                            printError(line, "Offset expected after + in "
                                    + inst + " R R +"
                                    + " in file " + srcFile + ".");
                        }
                    }
                }
                // Must be an error!
                else {
                    printError(line, "Invalid format for " + inst
                            + " instruction" + " in file " + srcFile + ".");
                }
            }
        }
    }

    /** ************************************************* */

    private static String reg2Bin(String reg) {

        int ireg = (int) ParsedLine.getNumValue(reg.substring(1, reg.length()));
        return Assembler.binString(ireg, 4);
    }

    private static String imm2Bin16(String op) {
        int iop = (int) ParsedLine.getNumValue(op.substring(1, op.length()));
        return Assembler.binString(iop, 16);
    }

    private static String imm2Bin32(String op) {
        int iop = (int) ParsedLine.getNumValue(op.substring(1, op.length()));
        return Assembler.binString(iop, 32);
    }

    private static boolean validRegLabel(ParsedLine line, String o1, String o2) {
        if (!validRegister(o1)) {
            printError(line, "First operand must " + "be a register [R0-R15].");
            return false;
        }
        else if (!validLabel(o2)) {
            printError(line, "Second operand must " + "be a label.");
            return false;
        }
        else {
            return true;
        }
    }

    /*
     * private static boolean validLabelInd(String op) { if
     * (op.charAt(op.length()-1) == ')' &&
     * validLabel(op.substring(0,op.length()-1))) { return true; }
     * 
     * return false; }
     * 
     * private static boolean validIndLabel(String op) { if (op.charAt(0) == '(' &&
     * op.charAt(op.length()-1) == ')' &&
     * validLabel(op.substring(1,op.length()-1))) { return true; }
     * 
     * return false; }
     * 
     * private static boolean validIndReg(String op) { if (op.charAt(0) == '(' &&
     * validRegister(op.substring(1,op.length()))) { return true; }
     * 
     * return false; }
     */

    private static boolean validLabel(String op) {
        if (validRegister(op) || !Character.isLetter(op.charAt(0))) {
            return false;
        }

        return true;
    }

    private static boolean validOffset(String op) {
        try {
            if (op.startsWith("+")) {
                ParsedLine.getNumValue(op.substring(1, op.length()));
                return true;
            }
            else {
                return false;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean validImmediate(String op) {
        try {
            if (op.startsWith("#")) {
                ParsedLine.getNumValue(op.substring(1, op.length()));
                return true;
            }
            else {
                return false;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean validRegister(String op) {
        try {
            if (!op.startsWith("R")
                    || Integer.parseInt(op.substring(1, op.length())) < 0
                    || Integer.parseInt(op.substring(1, op.length())) > 15) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static void printError(ParsedLine line, String msg) {
        System.out.println("Line " + line.getLineNum() + ": " + msg);
        line.setError();
    }

    /**
     * Test method.
     */
    public static void main(String[] args) {
        AsmFileReader r = new AsmFileReader("test-inst.asm");
        ParsedLine pl;

        Vector line;

        line = r.nextLine();
        while (line != null) {
            pl = new ParsedLine(line, r.getFilename());
            line = r.nextLine();
        }
    }
}

