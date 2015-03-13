package tools.simple;

import java.awt.TextArea;

import javax.swing.JFrame;

public class TextFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TextFrame(String title,String text) {
		super(title);
		// Set JFrame size
		setSize(800, 550);
		// Set default close operation for JFrame
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		TextArea txtArea = new TextArea(text);
		this.add(txtArea);
		setVisible(true);
	}	
}
