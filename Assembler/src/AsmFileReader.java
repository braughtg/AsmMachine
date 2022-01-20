/**
 * AsmFileReader.java Manage the reading of a .asm file. This has a method to
 * return the next line of the file parsed out into an array of strings with one
 * token per string. Any context sensitive processing is done elsewhere.
 * 
 * @author Grant William Braught
 * @author Dickinson College
 * @version 8/22/2000
 */

import java.io.*;
import java.util.*;

class AsmFileReader {

    private BufferedReader reader;

    private String filename;

    private boolean done;

    private int lineno;

    /**
     * Construct a reader for the .asm file indicated by filename. Subsequent
     * calls to nextLine return the next non-blank, non-comment line from the
     * file parsed into tokens.
     * 
     * @param filename the name of the file to read.
     */
    public AsmFileReader(String filename) {
        this.filename = filename;
        done = false;
        lineno = 0;

        try {
            reader = new BufferedReader(new FileReader(filename));
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: Unable to open " + filename + ".");
            System.exit(-1);
        }

    }

    public Vector nextLineNoSkips() {
        Vector tokens;

        if (!done) {
            tokens = nextRawLine();
            return tokens;
        }
        else {
            return null;
        }
    }

    /**
     * Return the next line of the file as a Vector of string tokens. This
     * method skips all blank and comment lines in the file. Comments are lines
     * beginning with *.
     * 
     * @return a Vector containing the tokens in the next line.
     */
    public Vector nextLine() {
        Vector tokens;

        if (!done) {

            tokens = nextRawLine();

            // Skip all empty lines and all comment lines (NOTE:
            // comments appear as blank lines when returned from nextRawLine).
            while (tokens != null && tokens.size() == 1) { 
                tokens = nextRawLine();
            }

            return tokens;
        }
        else {
            return null;
        }
    }

    public static boolean isBlank(Vector line) {
        return line != null && line.size() == 1;
    }

    /**
     * Tokenize the next line from the file being read.
     */
    private Vector nextRawLine() {
        String line = "";
        Vector tokens = new Vector(5, 8);

        try {
            line = reader.readLine();
            lineno++;

            if (line != null) {
                // Make the whole assember case-insensitive...
                //line = line.toUpperCase();

                tokens.addElement(new Integer(lineno));

                // Go backwards through the line lopping off tokens
                // until the line is empty.
                while (line.length() > 0) {

                    // Commas, spaces and tabs are the token separators.
                    int loc = line.lastIndexOf(' ');
                    loc = Math.max(loc, line.lastIndexOf('\t'));
                    loc = Math.max(loc, line.lastIndexOf(','));

                    String tmp;

                    // No more tokens.
                    if (loc == -1) {
                        tmp = line;
                        line = "";
                    }
                    else {
                        // Get the token and lop it off the end of
                        // the string.
                        tmp = line.substring(loc + 1, line.length());
                        line = line.substring(0, loc);
                    }

                    // Clean up the token.
                    tmp = tmp.trim();

                    // If the token starts with an * then we want
                    // to ignore everything we have read so far
                    // because it is a comment.
                    if (tmp.startsWith("*")) {
                        tokens.removeAllElements();
                        tokens.addElement(new Integer(lineno));
                        tmp = "";
                    }

                    // Don't add empty tokens.
                    if (!tmp.equalsIgnoreCase("")) {
                        tokens.add(1, tmp.trim());
                    }
                }

                return tokens;
            }
            else {
                done = true;
            }

            return null;
        }
        catch (IOException e) {
            System.out.println("Error: Failure reading " + filename + ".");
            System.out.println(e);
            System.exit(-1);
        }

        return null;
    }

    public String getFilename() {
        return filename;
    }
    
    public void setLineNum(int num) {
        lineno = num;
    }

    public int getLineNum() {
        return lineno;
    }
    /**
     * Test routine.
     */
    public static void main(String[] args) {
        AsmFileReader r = new AsmFileReader("test.asm");
        Vector line;

        line = r.nextLine();
        while (line != null) {
            System.out.println(line);
            line = r.nextLine();
        }
    }
}

