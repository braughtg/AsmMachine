import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MemCellRenderer.java A list cell renderer for displaying the contents of a
 * word of memory in the list.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 9/2/2000
 */

class MemoryGUI extends JPanel implements ListSelectionListener, ActionListener {

	private MainMemory theMem;
	private JList memList;
	private JScrollPane scrollPane;
	private JPanel mbPan;
	private JTextField mbField;
	JComboBox displayOptions;

	ListModel memData = new AbstractListModel() {
		public int getSize() {
			if (theMem != null) {
				return (theMem.getMem().size() / 4);
			} else {
				return 1;
			}
		}

		public Object getElementAt(int index) {
			String line;

			if (theMem != null) {
				String data = theMem.read(Integer.toBinaryString(index * 4));

				line = data.substring(0, 4) + " " + data.substring(4, 8) + " " + data.substring(8, 12) + " "
						+ data.substring(12, 16) + " " + data.substring(16, 20) + " " + data.substring(20, 24) + " "
						+ data.substring(24, 28) + " " + data.substring(28, 32);

				line = line + "  | ";

				String addr = "" + (index * 4);

				for (int i = 0; i < (6 - addr.length()); i++) {
					line = line + " ";
				}
				line = line + addr;

				return line;
			} else {
				return "0000 0000 0000 0000 0000 0000 0000 0000  |      0";
			}
		}
	};

	public MemoryGUI(MainMemory theMem) {

		this.theMem = theMem;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(new JLabel("  "));
		add(new JLabel("Memory Browser"));

		memList = new JList(memData);
		memList.setPrototypeCellValue("0000 0000 0000 0000 0000 0000 0000 0000  | 999999");
		memList.setVisibleRowCount(10);
		memList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memList.addListSelectionListener(this);
		memList.setAutoscrolls(true);

		scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(memList);

		add(scrollPane);

		mbPan = new JPanel();
		mbPan.add(new JLabel("Value: "));
		mbField = new JTextField(20);
		mbPan.add(mbField);
		add(mbPan);

		String[] data = { "Signed", "Unsigned", "Hexadecimal", "Machine Language" };
		displayOptions = new JComboBox(data);
		displayOptions.setFont(new Font("Monospaced", Font.PLAIN, 10));
		displayOptions.addActionListener(this);

		add(displayOptions);

		memList.revalidate();
		scrollPane.revalidate();
		scrollPane.repaint();
	}

	public void setMem(MainMemory theMem) {
		this.theMem = theMem;
		// These three lines are needed to be sure that
		// the memory browser JList is up to date and
		// displayed properly. Without them it has
		// some problems with displaying a new set of
		// memory contents.
		memList.revalidate();
		scrollPane.revalidate();
		scrollPane.repaint();
		// set();
	}

	/*
	 * public void selectAddr(String addr) { int line =
	 * Integer.parseInt(addr,2); memList.setSelectedIndex(line/4);
	 * 
	 * JScrollBar sb = scrollPane.getVerticalScrollBar(); int min =
	 * sb.getMinimum(); int max = sb.getMaximum();
	 * 
	 * int num = memList.getModel().getSize(); int size = (max-min)/num;
	 * 
	 * sb.setValue(size * (line/4)); }
	 */

	public void valueChanged(ListSelectionEvent e) {
		set();
		/*
		 * if (theMem != null) { asmField.setText(theMem.getAsmComment(
		 * Integer.toBinaryString(memList.getSelectedIndex()*4))); }
		 */
	}

	private void set() {

		if (theMem != null) {
			String displayAs = (String) displayOptions.getSelectedItem();
			String displayValue;

			if (memList.getSelectedIndex() >= 0) {

				String binVal = theMem.read(Integer.toBinaryString(memList.getSelectedIndex() * 4));

				if (displayAs.equals("Signed")) {
					Long longVal = Long.valueOf(binVal, 2);
					displayValue = new Integer((int) longVal.longValue()).toString();
				} else if (displayAs.equals("Unsigned")) {
					Long longVal = Long.valueOf(binVal, 2);
					displayValue = longVal.toString();
				} else if (displayAs.equals("Hexadecimal")) {
					Long longVal = Long.valueOf(binVal, 2);
					displayValue = Long.toHexString(longVal.longValue());
					displayValue = "0x" + displayValue.toUpperCase();
				} else if (displayAs.equals("Machine Language")) {
					displayValue = theMem.getMLComment(Integer.toBinaryString(memList.getSelectedIndex() * 4));
					if (displayValue == null) {
						displayValue = theMem.getAsmComment(Integer.toBinaryString(memList.getSelectedIndex() * 4));
					}
				} else {
					displayValue = "Crap!";
				}

				mbField.setText(displayValue);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		set();
	}

	public void update() {
		set();
		memList.revalidate();
		scrollPane.revalidate();
		scrollPane.repaint();
		memList.repaint();
		repaint();
	}

}
