package utils;

import inputOutput.TextIO;

import java.io.InputStream;

/**
 * The type Stream gobbler.
 */
public class StreamGobbler extends Thread {

    private InputStream inputStream;
    private String type;
    private TextIO textIO;

    /**
     * Instantiates a new Stream gobbler.
     *
     * @param inputStream the input stream
     * @param type        the type
     */
    public StreamGobbler(InputStream inputStream, String type) {
        this.inputStream = inputStream;
        this.type = type;
    }

    public void run() {
        textIO = new TextIO();
        textIO.read(inputStream);
    }

    /**
     * Gets text iO with stream.
     *
     * @return the text iO
     */
    public TextIO getTextIO() {
        return textIO;
    }
}
