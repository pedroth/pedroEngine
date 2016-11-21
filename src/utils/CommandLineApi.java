package utils;

import inputOutput.TextIO;

import java.io.IOException;

/**
 * The type Command line api.
 */
public class CommandLineApi {
    private StreamGobbler errorGlobber;
    private StreamGobbler outputGlobber;

    /**
     * Instantiates a new Command line api.
     */
    public CommandLineApi() {
    }

    public static void main(String[] args) {
        CommandLineApi commandLineApi = new CommandLineApi();
        try {
            commandLineApi.callCommand("ls");
            TextIO errorStream = commandLineApi.getOutputStream();
            System.out.println("Text: " + errorStream.getText());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call command.
     *
     * @param command the command
     * @return the state
     * @throws InterruptedException the interrupted exception
     * @throws InterruptedException the interrupted exception
     */
    public int callCommand(String command) throws InterruptedException, IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);
        // any error message?
        this.errorGlobber = new StreamGobbler(proc.getErrorStream(), "ERROR");
        errorGlobber.start();
        // any output?
        this.outputGlobber = new StreamGobbler(proc.getInputStream(), "OUTPUT");
        outputGlobber.start();
        return proc.waitFor();
    }

    /**
     * Gets error stream.
     *
     * @return the error stream
     */
    public TextIO getErrorStream() {
        return errorGlobber.getTextIO();
    }

    /**
     * Gets output stream.
     *
     * @return the output stream
     */
    public TextIO getOutputStream() {
        return outputGlobber.getTextIO();
    }
}
