import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Machine.java The heart and soul of the machine. It reads the instructions
 * from the memory and manipulates the registers and memory appropriately.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 8/29/2000
 */

class Machine extends Thread implements ActionListener {

	static MainMemory mem;
	static RegisterBank reg;
	static String PC;
	static String IR;
	static boolean running;

	static MemoryGUI memGUI;
	static RegisterBankGUI regGUI;
	static JTextField PCGUI;
	static JTextField IRGUI;
	static JTextField asmInstGUI;
	static JTextField mlInstGUI;
	static JButton reset;
	static JButton step;
	static JButton run;
	static JButton load;
	static JTextField filename;
	static JTextField consoleIn;
	static JTextField consoleOut;

	static JProgressBar runningBarGUI;

	static Toolkit GUIToolkit;

	private static final String ZERO = "00000000000000000000000000000000";
	private static final String HALT = "11111111111111111111111111111111";
	private static final String BREAK = "00001111000011110000111100001111";

	private static final String ADD = "00100011";
	private static final String SUB = "00101111";
	private static final String AND = "00100010";
	private static final String OR = "00100001";

	private static final String LOAD = "01000001";
	private static final String STORE = "01000010";

	private static final String MOV = "01101001";
	private static final String NOT = "01100100";
	private static final String SHL = "01100010";
	private static final String SHR = "01100001";

	private static final String LOADi = "10010011";
	private static final String LOADUi = "10011001";

	private static final String ADDi = "10110011";
	private static final String SUBi = "10111111";
	private static final String ANDi = "10110010";
	private static final String ORi = "10110001";

	private static final String BRANCH = "11000001";

	private static final String BZERO = "11100001";
	private static final String BNEG = "11100010";
	private static final String BODD = "11100011";
	private static final String JREG = "11100100";

	private static final String JAL = "01101000";

	private static final String CONSOLE_INPUT_ADDR = "11111111111111111111111111111100"; // 0xFFFFFFFC
	private static final String CONSOLE_OUTPUT_ADDR = "11111111111111111111111111111000"; // 0xFFFFFFF8

	public Machine() {
	}

	public Machine(String input) {

		mem = null;
		reg = new RegisterBank();
		PC = ZERO;
		IR = ZERO;

		running = false;

		setupGUI();

		if (input != null) {
			filename.setText(input);
			load();
		}
	}

	private void setupGUI() {
		JFrame myFrame = new JFrame("CS251 Machine Emulator");
		GUIToolkit = myFrame.getToolkit();

		Container thePane = myFrame.getContentPane();
		addComponents(thePane);

		// This code must be added to the frame you create.
		// it handles clicks on the [x] that closes the window.
		// Without this code you will have to use ctrl-c to stop
		// your program (even if you click on the [x].
		myFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		myFrame.pack();
		myFrame.setVisible(true);
	}

	private void addComponents(Container thePane) {

		thePane.setLayout(new BoxLayout(thePane, BoxLayout.X_AXIS));

		regGUI = new RegisterBankGUI();
		thePane.add(regGUI);

		JPanel contMem = new JPanel();
		contMem.setLayout(new BoxLayout(contMem, BoxLayout.Y_AXIS));

		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));

		step = new JButton("Step");
		step.addActionListener(this);
		step.setEnabled(false);
		controls.add(step);

		run = new JButton("Run");
		run.addActionListener(this);
		run.setEnabled(false);
		controls.add(run);

		reset = new JButton("Reset");
		reset.addActionListener(this);
		reset.setEnabled(false);
		controls.add(reset);

		load = new JButton("Load");
		load.addActionListener(this);
		controls.add(load);

		contMem.add(controls);

		JPanel fn = new JPanel();
		fn.setLayout(new BoxLayout(fn, BoxLayout.X_AXIS));
		JLabel fnLab = new JLabel("File: ");
		fnLab.setFont(new Font("Monospaced", Font.PLAIN, 12));
		fn.add(fnLab);
		filename = new JTextField(32);
		filename.setFont(new Font("Monospaced", Font.PLAIN, 12));
		filename.setText("");
		fn.add(filename);
		contMem.add(fn);

		runningBarGUI = new JProgressBar(0, 10);
		runningBarGUI.setString("Stopped");
		runningBarGUI.setStringPainted(true);
		contMem.add(runningBarGUI);

		contMem.add(new JLabel("  "));
		JLabel instInfLab = new JLabel("Instruction Information");
		contMem.add(instInfLab);

		JPanel pc = new JPanel();
		pc.setLayout(new BoxLayout(pc, BoxLayout.X_AXIS));
		JLabel pcLab = new JLabel(" PC: ");
		pcLab.setFont(new Font("Monospaced", Font.PLAIN, 12));
		pc.add(pcLab);

		PCGUI = new JTextField(32);
		PCGUI.setEditable(false);
		PCGUI.setFont(new Font("Monospaced", Font.PLAIN, 12));
		PCGUI.setText(PC + " (0)");
		pc.add(PCGUI);
		contMem.add(pc);

		JPanel ir = new JPanel();
		ir.setLayout(new BoxLayout(ir, BoxLayout.X_AXIS));
		JLabel irLab = new JLabel(" IR: ");
		irLab.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ir.add(irLab);

		IRGUI = new JTextField(32);
		IRGUI.setEditable(false);
		IRGUI.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ir.add(IRGUI);
		contMem.add(ir);

		JPanel ml = new JPanel();
		ml.setLayout(new BoxLayout(ml, BoxLayout.X_AXIS));
		JLabel mlLab = new JLabel(" ML: ");
		mlLab.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ml.add(mlLab);
		mlInstGUI = new JTextField(22);
		mlInstGUI.setEditable(false);
		mlInstGUI.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ml.add(mlInstGUI);
		contMem.add(ml);

		JPanel asm = new JPanel();
		asm.setLayout(new BoxLayout(asm, BoxLayout.X_AXIS));
		JLabel asmLab = new JLabel("ASM: ");
		asmLab.setFont(new Font("Monospaced", Font.PLAIN, 12));
		asm.add(asmLab);
		asmInstGUI = new JTextField(22);
		asmInstGUI.setEditable(false);
		asmInstGUI.setFont(new Font("Monospaced", Font.PLAIN, 12));
		asm.add(asmInstGUI);
		contMem.add(asm);

		contMem.add(new JLabel("  "));
		JLabel conLab = new JLabel("Console");
		contMem.add(conLab);

		JPanel conIn = new JPanel();
		conIn.setLayout(new BoxLayout(conIn, BoxLayout.X_AXIS));
		JLabel conInLab = new JLabel(" Input: ");
		conInLab.setFont(new Font("Monospaced", Font.PLAIN, 12));
		consoleIn = new JTextField(20);
		conIn.add(conInLab);
		conIn.add(consoleIn);
		contMem.add(conIn);

		JPanel conOut = new JPanel();
		conOut.setLayout(new BoxLayout(conOut, BoxLayout.X_AXIS));
		JLabel conOutLab = new JLabel("Output: ");
		conOutLab.setFont(new Font("Monospaced", Font.PLAIN, 12));
		consoleOut = new JTextField(20);
		consoleOut.setEditable(false);
		conOut.add(conOutLab);
		conOut.add(consoleOut);
		contMem.add(conOut);

		memGUI = new MemoryGUI(mem);
		contMem.add(memGUI);

		thePane.add(contMem);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("Step")) {
			if (!running) {
				runningBarGUI.setString("Running");
				reset.setEnabled(false);
				step.setEnabled(false);
				load.setEnabled(false);
				step();
				reset.setEnabled(true);
				step.setEnabled(true);
				load.setEnabled(true);
				runningBarGUI.setString("Stopped");
				updateGUI();
			}
		} else if (cmd.equals("Run")) {
			running = true;
			run.setText("Stop");

			Machine m = new Machine();
			m.start();
		} else if (cmd.equals("Stop")) {
			running = false;
			run.setText("Run");
		} else if (cmd.equals("Reset")) {
			load();
			reset();
		} else if (cmd.equals("Load")) {
			load();
		}
	}

	private void load() {
		mem = new MainMemory(filename.getText().trim());

		if (!mem.getError()) {
			memGUI.setMem(mem);

			runningBarGUI.setMaximum(mem.getMem().size());

			reset();
		} else {
			filename.setText("Error loading: " + filename.getText());
			memGUI.setMem(null);
		}
	}

	private void reset() {
		PC = ZERO;
		PCGUI.setText(PC + " (0)");
		IR = ZERO;
		IRGUI.setText("");
		asmInstGUI.setText("");
		mlInstGUI.setText("");
		runningBarGUI.setValue(0);
		running = false;
		run.setText("Run");
		reg.reset();

		consoleIn.setText("");
		consoleOut.setText("");

		run.setEnabled(true);
		step.setEnabled(true);

		updateGUI();
	}

	public void run() {
		runningBarGUI.setString("Running");
		reset.setEnabled(false);
		step.setEnabled(false);
		load.setEnabled(false);

		if (IR.equals(BREAK)) {
			step();
		}

		while (!IR.equals(HALT) && !IR.equals(BREAK) && running == true) {
			step();
			// yield();
		}

		runningBarGUI.setString("Stopped");
		running = false;

		if (IR.equals(HALT)) {
			run.setText("Run");
			run.setEnabled(false);
		}
		if (IR.equals(BREAK)) {
			run.setText("Run");
			step.setEnabled(true);
		} else {
			step.setEnabled(true);
		}

		reset.setEnabled(true);
		load.setEnabled(true);

		updateGUI();
	}

	public void step() {

		if (!IR.equals(HALT)) {

			runningBarGUI.setValue(Integer.parseInt(PC, 2));

			IR = mem.read(PC);
			IRGUI.setText(IR);
			asmInstGUI.setText(mem.getAsmComment(PC));
			mlInstGUI.setText(mem.getMLComment(PC));

			// The execute function returns true if the
			// pc is to be incremented. Thus, false will
			// be returned when a branch or jump is
			// performed.
			try {
				if (execute()) {
					PC = binAdd(PC, "100");
				}
				PCGUI.setText(PC + " (" + Integer.parseInt(PC, 2) + ")");
			} catch (Exception e) {
				System.err.println("\u0007");
				System.err.println("Unrecoverable Error - Machine being reset.");
				reset();
			}
			// Runtime.getRuntime().gc();
		}
	}

	private void updateGUI() {
		regGUI.set(reg);
		memGUI.update();
	}

	private boolean execute() {
		if (IR.startsWith(ADD)) {
			ADD();
		} else if (IR.startsWith(SUB)) {
			SUB();
		} else if (IR.startsWith(AND)) {
			AND();
		} else if (IR.startsWith(OR)) {
			OR();
		} else if (IR.startsWith(LOAD)) {
			LOAD();
		} else if (IR.startsWith(STORE)) {
			STORE();
		} else if (IR.startsWith(MOV)) {
			MOV();
		} else if (IR.startsWith(NOT)) {
			NOT();
		} else if (IR.startsWith(SHL)) {
			SHL();
		} else if (IR.startsWith(SHR)) {
			SHR();
		} else if (IR.startsWith(LOADi)) {
			LOADi();
		} else if (IR.startsWith(LOADUi)) {
			LOADUi();
		} else if (IR.startsWith(ADDi)) {
			ADDi();
		} else if (IR.startsWith(SUBi)) {
			SUBi();
		} else if (IR.startsWith(ANDi)) {
			ANDi();
		} else if (IR.startsWith(ORi)) {
			ORi();
		} else if (IR.startsWith(BRANCH)) {
			BRANCH();
			return false;
		} else if (IR.startsWith(BZERO)) {
			return BZERO();
		} else if (IR.startsWith(BNEG)) {
			return BNEG();
		} else if (IR.startsWith(BODD)) {
			return BODD();
		} else if (IR.startsWith(JREG)) {
			JREG();
			return false;
		} else if (IR.startsWith(JAL)) {
			JAL();
			return false;
		} else if (IR.startsWith(ZERO)) {
		} else if (IR.startsWith(HALT)) {
		} else if (IR.startsWith(BREAK)) {
		} else {
			System.out.println("Unknown Instruction: " + IR);
		}

		return true;
	}

	private void ADD() {
		String RegA = IR.substring(8, 12);
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valA = reg.read(RegA);
		String valB = reg.read(RegB);

		String sum = binAdd(valA, valB);

		reg.write(RegC, sum);
	}

	private void SUB() {
		String RegA = IR.substring(8, 12);
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valA = reg.read(RegA);
		String valB = reg.read(RegB);

		String sum = binSub(valA, valB);

		reg.write(RegC, sum);
	}

	private void AND() {
		String RegA = IR.substring(8, 12);
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valA = reg.read(RegA);
		String valB = reg.read(RegB);

		String sum = binAND(valA, valB);

		reg.write(RegC, sum);
	}

	private void OR() {
		String RegA = IR.substring(8, 12);
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valA = reg.read(RegA);
		String valB = reg.read(RegB);

		String sum = binOR(valA, valB);

		reg.write(RegC, sum);
	}

	private void LOAD() {
		String Breg = IR.substring(8, 12);
		String Reg = IR.substring(12, 16);
		String Off = IR.substring(16, 32);

		String addr = binAdd(reg.read(Breg), Off);
		String val;

		if (addr.equals(CONSOLE_INPUT_ADDR)) {
			val = getFromConsole();
		} else {
			val = mem.read(addr);
		}
		reg.write(Reg, val);
	}

	private String getFromConsole() {
		String allVal = consoleIn.getText();
		int i = 0;
		String inVal = "";

		while (i < allVal.length() && allVal.charAt(i) != ' ') {
			inVal = inVal + allVal.charAt(i);
			i++;
		}

		if (inVal.equals("")) {
			consoleIn.setText("");
			inVal = "0";
		} else {
			consoleIn.setText(allVal.substring(i, allVal.length()).trim());
			if (inVal.charAt(0) == '-') {
				inVal = Integer.toBinaryString(Integer.parseInt(inVal));
			} else {
				inVal = "0" + Integer.toBinaryString(Integer.parseInt(inVal));
			}
		}

		return signExtend(inVal);
	}

	private void STORE() {
		String Breg = IR.substring(8, 12);
		String Reg = IR.substring(12, 16);
		String Off = IR.substring(16, 32);

		String addr = binAdd(reg.read(Breg), Off);
		String val = reg.read(Reg);

		if (addr.equals(CONSOLE_OUTPUT_ADDR)) {
			Long longVal = Long.valueOf(val, 2);
			String displayValue = new Integer((int) longVal.longValue()).toString();
			writeToConsole(displayValue);
		} else {
			mem.write(addr, val);
		}
	}

	private void writeToConsole(String value) {
		consoleOut.setText(consoleOut.getText() + " " + value);
	}

	private void MOV() {
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valB = reg.read(RegB);
		reg.write(RegC, valB);
	}

	private void NOT() {
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valB = reg.read(RegB);

		String sum = binNot(valB);

		reg.write(RegC, sum);
	}

	private void SHL() {
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valB = reg.read(RegB);

		String sum = binSHL(valB);

		reg.write(RegC, sum);
	}

	private void SHR() {
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		String valB = reg.read(RegB);

		String sum = binSHR(valB);

		reg.write(RegC, sum);
	}

	private void LOADi() {
		String RegC = IR.substring(12, 16);
		String Imm = signExtend(IR.substring(16, 32));

		reg.write(RegC, extend(Imm));
	}

	private void LOADUi() {
		String RegC = IR.substring(12, 16);
		String Imm = IR.substring(16, 32);

		String val = reg.read(RegC);
		Imm = Imm + val.substring(16, 32);

		reg.write(RegC, Imm);
	}

	private void ADDi() {
		String RegB = IR.substring(8, 12);
		String RegC = IR.substring(12, 16);
		String Imm = signExtend(IR.substring(16, 32));

		String valB = reg.read(RegB);

		String sum = binAdd(valB, Imm);

		reg.write(RegC, sum);
	}

	private void SUBi() {
		String RegB = IR.substring(8, 12);
		String RegC = IR.substring(12, 16);
		String Imm = signExtend(IR.substring(16, 32));

		String valB = reg.read(RegB);

		String sum = binSub(valB, Imm);

		reg.write(RegC, sum);
	}

	private void ANDi() {
		String RegB = IR.substring(8, 12);
		String RegC = IR.substring(12, 16);
		String Imm = signExtend(IR.substring(16, 32));

		String valB = reg.read(RegB);

		String sum = binAND(valB, Imm);

		reg.write(RegC, sum);
	}

	private void ORi() {
		String RegB = IR.substring(8, 12);
		String RegC = IR.substring(12, 16);
		String Imm = IR.substring(16, 32);

		String valB = reg.read(RegB);

		String sum = signExtend(binOR(valB, Imm));

		reg.write(RegC, sum);
	}

	private void BRANCH() {
		String Off = signExtend(IR.substring(16, 32));

		PC = binAdd(PC, binSHL(binSHL(Off)));
	}

	private boolean BZERO() {
		String Reg = IR.substring(12, 16);
		String Off = signExtend(IR.substring(16, 32));

		if (reg.read(Reg).equals(ZERO)) {
			PC = binAdd(PC, binSHL(binSHL(Off)));
			return false;
		} else {
			return true;
		}
	}

	private boolean BNEG() {
		String Reg = IR.substring(12, 16);
		String Off = signExtend(IR.substring(16, 32));

		if (reg.read(Reg).charAt(0) == '1') {
			PC = binAdd(PC, binSHL(binSHL(Off)));
			return false;
		} else {
			return true;
		}
	}

	private boolean BODD() {
		String Reg = IR.substring(12, 16);
		String Off = signExtend(IR.substring(16, 32));

		if (reg.read(Reg).endsWith("1")) {
			PC = binAdd(PC, binSHL(binSHL(Off)));
			return false;
		} else {
			return true;
		}
	}

	private void JREG() {
		String Reg = IR.substring(12, 16);

		PC = reg.read(Reg);
	}

	private void JAL() {
		String RegB = IR.substring(12, 16);
		String RegC = IR.substring(16, 20);

		reg.write(RegC, binAdd(PC, "0100"));
		PC = reg.read(RegB);
	}

	private String signExtend(String op) {
		while (op.length() < 32) {
			op = op.charAt(0) + op;
		}
		return op;
	}

	private String extend(String op) {
		while (op.length() < 32) {
			op = "0" + op;
		}
		return op;
	}

	private String binAdd(String in, String val) {
		int iIn = (int) Long.parseLong(in, 2);
		int iVal = (int) Long.parseLong(val, 2);

		String sum = Integer.toBinaryString(iIn + iVal);

		return extend(sum);
	}

	private String binSub(String in, String val) {
		int iIn = (int) Long.parseLong(in, 2);
		int iVal = (int) Long.parseLong(val, 2);

		String sum = Integer.toBinaryString(iIn - iVal);

		return extend(sum);
	}

	private String binAND(String in, String val) {
		int iIn = (int) Long.parseLong(in, 2);
		int iVal = (int) Long.parseLong(val, 2);

		String sum = Integer.toBinaryString(iIn & iVal);

		return extend(sum);
	}

	private String binOR(String in, String val) {
		int iIn = (int) Long.parseLong(in, 2);
		int iVal = (int) Long.parseLong(val, 2);

		String sum = Integer.toBinaryString(iIn | iVal);

		return extend(sum);
	}

	private String binNot(String in) {
		int iIn = (int) Long.parseLong(in, 2);

		String sum = Integer.toBinaryString(~iIn);

		return extend(sum);
	}

	private String binSHL(String in) {
		int iIn = (int) Long.parseLong(in, 2);

		String sum = Integer.toBinaryString(iIn << 1);

		return extend(sum);
	}

	private String binSHR(String in) {
		int iIn = (int) Long.parseLong(in, 2);

		String sum = Integer.toBinaryString(iIn >>> 1);

		return extend(sum);
	}

	public static void main(String[] args) {

		if (args.length == 0) {
			Machine mach = new Machine(null);
		} else if (args.length == 1) {
			Machine mach = new Machine(args[0]);
		} else {
			printUsage();
		}
	}

	private static void printUsage() {
		System.out.println("java Machine [<mem image>]");
		System.out.println();
		System.out.println("   <mem image>: Memory image to load.");
	}
}
