package inputOutput;

import java.io.*;
import java.net.URL;

public class TextIO {
    StringBuilder text;

    public TextIO() {
        text = new StringBuilder();
    }

    public TextIO(String adress) {
        super();
        this.read(adress);
    }

    public void read(String address) {
        this.text = new StringBuilder();
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
                text.append(line).append(String.format("\n"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(InputStream address) {
        this.text = new StringBuilder();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(address));
            String line;

            while ((line = in.readLine()) != null) {
                text.append(line).append(String.format("\n"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String adress, String text) {
        if (isUrl(adress))
            return;

        try {
            File file = new File(adress);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintStream bw = new PrintStream(file);

            bw.print(text);

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
        return text.toString();
    }
}
