import javax.swing.*;
import java.awt.*;

/**
 * RegisterBankGUI.java GUI Component for displaying all of the registers.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 9/2/2000
 */

class RegisterBankGUI extends JPanel {

	private RegisterGUI[] regs;

	public RegisterBankGUI() {
		regs = new RegisterGUI[16];

		setLayout(new GridLayout(16, 1, 1, 1));

		for (int i = 0; i < 10; i++) {
			regs[i] = new RegisterGUI(" R" + i + ":");
			add(regs[i]);
		}
		for (int i = 10; i < 16; i++) {
			regs[i] = new RegisterGUI("R" + i + ":");
			add(regs[i]);
		}
	}

	public void set(RegisterBank bank) {
		for (int i = 0; i < 16; i++) {
			regs[i].set(bank.read(Integer.toBinaryString(i)));
		}
		repaint();
	}
}
