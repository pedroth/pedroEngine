package utils;


import com.sun.net.httpserver.HttpExchange;
import inputOutput.TextIO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The type J server utils.
 */
public class JServerUtils {
    /**
     * Instantiates a new J server utils.
     */
    public JServerUtils() {
    }

    /**
     * Respond with text file.
     *
     * @param httpExchange the http exchange
     * @param file         the file
     * @throws IOException the iO exception
     */
    public void respondWithTextFile(HttpExchange httpExchange, String file) throws IOException {
        TextIO text = new TextIO(file);
        byte[] response = text.getText().getBytes();
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
        httpExchange.getResponseBody().write(response);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }

    /**
     * Respond with text.
     *
     * @param httpExchange the http exchange
     * @param text         the text
     * @throws IOException the iO exception
     */
    public void respondWithText(HttpExchange httpExchange, String text) throws IOException {
        byte[] response = text.getBytes();
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
        httpExchange.getResponseBody().write(response);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }

    /**
     * Respond with bytes.
     *
     * @param httpExchange the http exchange
     * @param file         the file
     * @throws IOException the iO exception
     */
    public void respondWithBytes(HttpExchange httpExchange, String file) throws IOException {
        byte[] response = Files.readAllBytes(Paths.get(file));
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
        httpExchange.getResponseBody().write(response, 0, response.length);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }

}
