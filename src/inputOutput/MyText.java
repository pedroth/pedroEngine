package inputOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MyText {
	String text;

	public MyText() {
		text = "";
	}

	public MyText(String adress) {
		super();
		this.read(adress);
	}

	public void read(String address) {
		text = "";
		BufferedReader in;
		try {
			if (isUrl(address)) {
				URL url;
				url = new URL(address);
				in = new BufferedReader(new InputStreamReader(url.openStream()));

			} else {
				in = new BufferedReader(new FileReader(address));
			}

			String line;

			while ((line = in.readLine()) != null) {
				text += line + String.format("\n");
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String adress, String text) {
		this.text = text;
		if (isUrl(adress))
			return;

		try {
			File file = new File(adress);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			PrintStream bw = new PrintStream(file);

			bw.print(getText());

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean isUrl(String adress) {
		String[] aux = adress.split("http");
		if (aux.length > 1)
			return true;
		else
			return false;
	}

	public String getText() {
		return text;
	}
}
