package utils;


import com.sun.net.httpserver.HttpExchange;
import inputOutput.TextIO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * The type J server utils.
 */
public final class JServerUtils {
    /**
     * Instantiates a new J server utils.
     */
    private JServerUtils() {
    }

    /**
     * Respond with text file.
     *
     * @param httpExchange the http exchange
     * @param file         the file
     * @throws IOException the iO exception
     */
    public static void respondWithTextFile(HttpExchange httpExchange, String file) throws IOException {
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
    public static void respondWithText(HttpExchange httpExchange, String text) throws IOException {
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
    public static void respondWithBytes(HttpExchange httpExchange, String file) throws IOException {
        byte[] response = Files.readAllBytes(Paths.get(file));
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
        httpExchange.getResponseBody().write(response, 0, response.length);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }


    public static Map<String, String> parsePostMessage(String request) {
        Map<String, String> ans = new HashMap<>();
        String[] split = request.replace("%3A", ":").replace("%5C", "/").replace("%2F", "/").replace("+", " ").replace("\n", "").split("&");
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split("=");
            String key = split1[0];
            String value = split1[1];
            ans.put(key, value);
        }
        return ans;
    }

    public static Map<String, String> parsePostMessageUnformatted(String request) {
        request = request.substring(0, request.length() - 1);
        Map<String, String> ans = new HashMap<>();
        String[] split = request.split("&");
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split("=");
            String key = split1[0];
            String value = split1[1];
            ans.put(key, value);
        }
        return ans;
    }

}
