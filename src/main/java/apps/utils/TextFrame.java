package apps.utils;

import javax.swing.*;
import java.awt.*;

public class TextFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public TextFrame(String title, String text) {
		super(title);
		// Set JFrame size
		setSize(800, 550);
		JTextArea txtArea = new JTextArea(text);
		JScrollPane areaScrollPane = new JScrollPane(txtArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(areaScrollPane);
	}

	public static TextFrameBuilder builder() {
		return new TextFrameBuilder();
	}

	public static class TextFrameBuilder {
		private StringBuffer stringBuffer = new StringBuffer();

		private TextFrameBuilder() { }

		public TextFrameBuilder addLine(String text) {
			stringBuffer.append(text + "\n");
			return this;
		}

		public TextFrame buildWithTitle(String title) {
			return new TextFrame(title, stringBuffer.toString());
		}
	}
}
