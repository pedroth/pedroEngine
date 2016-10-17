package nlp.seriesSummary;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import inputOutput.TextIO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static junit.framework.Assert.fail;

/**
 * based on  http://www.programcreek.com/java-api-examples/index.php?source_dir=middleman-master/test/middleman/proxy/DummyHttpServer.java
 */
public class ArcSummaryServer {
    private final int serverPort;
    private Map<String, ArcSummaryThreadPair> arcSummarizerMap = new HashMap<>(3);

    public ArcSummaryServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public static void main(String[] args) {
        ArcSummaryServer arcSummaryServer = new ArcSummaryServer(8080);
        arcSummaryServer.start();
    }

    private void respondWithTextFile(HttpExchange httpExchange, String file) throws IOException {
        TextIO text = new TextIO(file);
        byte[] response = text.getText().getBytes();
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
        httpExchange.getResponseBody().write(response);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }

    private void respondWithText(HttpExchange httpExchange, String text) throws IOException {
        byte[] response = text.getBytes();
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
        httpExchange.getResponseBody().write(response);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }

    private void respondWithBytes(HttpExchange httpExchange, String file) throws IOException {
        byte[] response = Files.readAllBytes(Paths.get(file));
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
        httpExchange.getResponseBody().write(response);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
    }

    private String[] parseInput(String request) {
        String[] split = request.replace("%3A", ":").replace("%5C", "/").replace("%2F", "/").split("&");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].split("=")[1];
        }
        return split;
    }

    public void start() {
        HttpServer summaryServer = null;
        try {
            summaryServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Couldn't start server");
        }


        summaryServer.createContext("/ArcSummary", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                URI requestURI = httpExchange.getRequestURI();
                String[] split = requestURI.toString().split("/");
                String file = split[split.length - 1];
                System.out.println(requestURI);
                System.out.println(this.getClass() + " - received message from " + httpExchange.getRemoteAddress());
                respondWithTextFile(httpExchange, "src/nlp/resources/web/" + file);
            }
        });

        summaryServer.createContext("/input", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                try {
                    System.out.println(httpExchange.getRequestURI());
                    TextIO text = new TextIO();
                    text.read(httpExchange.getRequestBody());
                    String[] input = parseInput(text.getText());
                    ArcSummarizer arcSummarizer = new ArcSummarizer(input[0], input[1], Double.valueOf(input[2]), Double.valueOf(input[3]), Integer.valueOf(input[4]), Integer.valueOf(input[5]), ArcSummarizer.getDistanceByName(input[6]));
                    Thread thread = new Thread(() -> arcSummarizer.buildSummary(input[7], Double.valueOf(input[8])));
                    arcSummarizerMap.put(input[9], new ArcSummaryThreadPair(thread, arcSummarizer));
                    thread.start();
                    respondWithText(httpExchange, "loading . . . ");
                } catch (Exception e) {
                    respondWithText(httpExchange, e.getMessage());
                }
            }
        });


        summaryServer.createContext("/log", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                try {
                    System.out.println(httpExchange.getRequestURI());

                    TextIO text = new TextIO();
                    text.read(httpExchange.getRequestBody());
                    String[] in = parseInput(text.getText());

                    ArcSummaryThreadPair arcSummaryThreadPair = arcSummarizerMap.get(in[0]);

                    StringBuilder stringBuilder = new StringBuilder();
                    for (String log : arcSummaryThreadPair.getArcSummarySummarizer().getLog()) {
                        stringBuilder.append(log + "<br>");
                    }
//                    System.out.println(stringBuilder);
                    respondWithText(httpExchange, "loading . . .  " + stringBuilder);
                } catch (Exception e) {
                    respondWithText(httpExchange, e.getMessage());
                }
            }
        });


        summaryServer.setExecutor(Executors.newCachedThreadPool());
        summaryServer.start();
    }

    private class ArcSummaryThreadPair {
        private final ArcSummarizer arcSummarySummarizer;
        private final Thread thread;

        public ArcSummaryThreadPair(Thread thread, ArcSummarizer arcSummarySummarizer) {
            this.thread = thread;
            this.arcSummarySummarizer = arcSummarySummarizer;
        }

        public ArcSummarizer getArcSummarySummarizer() {
            return arcSummarySummarizer;
        }

        public Thread getThread() {
            return thread;
        }
    }
}
