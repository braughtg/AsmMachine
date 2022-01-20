import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * RegisterGUI.java
 *    GUI Component for displaying a register.  It permits the value
 *    to be displayed in signed, unsigned, binary or hexadecimal 
 *    versions.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 9/2/2000
 */

class RegisterGUI extends JPanel implements ActionListener {

    private JTextField regDisplay;
    private JComboBox displayOptions;
    private String curDisplay = "Signed";

    public RegisterGUI(String regLabel) {
      
	setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

	JLabel lab = new JLabel(regLabel);
	lab.setFont(new Font("Monospaced",
			     Font.BOLD,
			     10));
	this.add(lab);
	
	regDisplay = new JTextField("0",33);
	regDisplay.setEditable(false);
	regDisplay.setFont(new Font("Monospaced",
				    Font.PLAIN,
				    10));
	this.add(regDisplay);

	String[] data = {"Signed", "Unsigned", "Binary", "Hexadecimal"};
	displayOptions = new JComboBox(data);
	displayOptions.setFont(new Font("Monospaced",
					Font.PLAIN,
					10));
	displayOptions.addActionListener(this);

	this.add(displayOptions);
    }

    public void set(String binVal) { 
	
	String displayAs = (String)displayOptions.getSelectedItem();
	String displayValue;

	binVal = paddTo32(binVal);

	if (displayAs.equals("Signed")) {
	    Long longVal = Long.valueOf(binVal,2);
	    displayValue = new Integer((int)longVal.longValue()).toString();
	}
	else if (displayAs.equals("Unsigned")) {
	    Long longVal = Long.valueOf(binVal,2);
	    displayValue = longVal.toString();
	}
	else if (displayAs.equals("Binary")) {
	    displayValue = binVal;
	}
	else if (displayAs.equals("Hexadecimal")) {
	    Long longVal = Long.valueOf(binVal,2);
	    displayValue = Long.toHexString(longVal.longValue());
	    displayValue = "0x" + displayValue.toUpperCase();
	}
	else {
	    displayValue = "Crap!";
	}

	regDisplay.setText(displayValue);
	curDisplay = displayAs;
    }

    public void actionPerformed (ActionEvent e) {

	String binValue;
	String curValue = regDisplay.getText();
	
	if (curDisplay.equals("Signed") ||
	    curDisplay.equals("Unsigned")) {
	    Long longVal = Long.valueOf(curValue);
	    binValue = Long.toBinaryString(longVal.longValue());
	}
	else if (curDisplay.equals("Binary")) {
	    binValue = curValue;
	}
	else if (curDisplay.equals("Hexadecimal")) {
	    Long longVal = Long.valueOf(curValue.substring(2,curValue.length()),16);
	    binValue = Long.toBinaryString(longVal.longValue());
	}
	else {
	    binValue = "Crap!";
	}
	
	set(binValue);
    } 

    private String paddTo32(String binValue) {
	if (binValue.length() > 32) {
	    binValue = binValue.substring(binValue.length()-32, 
					  binValue.length());
	}
	else {
	    while (binValue.length() < 32) {
		binValue = "0" + binValue;
	    }
	}
	return binValue;
    }
}
