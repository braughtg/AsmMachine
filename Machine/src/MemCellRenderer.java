import javax.swing.*;
import java.awt.*;

/**
 * MemCellRenderer.java
 *    A list cell renderer for displaying the contents of a
 *    word of memory in the list.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 9/2/2000
 */

class MemCellRenderer extends JLabel implements ListCellRenderer {
    
    public MemCellRenderer() {
	setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
						  Object value,
						  int index,
						  boolean isSelected,
						  boolean cellHasFocus) {
	
	setText(value.toString());
	setBackground(isSelected ? Color.red : Color.white);
	setForeground(isSelected ? Color.white : Color.black);
	return this;
    }
}
