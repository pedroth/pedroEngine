package nlp.seriesSummary;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import inputOutput.TextIO;
import utils.FilesCrawler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static junit.framework.Assert.fail;

/**
 * based on  http://www.programcreek.com/java-api-examples/index.php?source_dir=middleman-master/test/middleman/proxy/DummyHttpServer.java
 */
public class ArcSummaryServer {
    private final static String HOME_ADDRESS = "src/nlp/resources/web/";
    private final static String SUMMARY_FOLDER_NAME = "summary";
    private final static String OUTPUT_VIDEO_EXTENSION = "mp4";
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
        String[] split = request.replace("%3A", ":").replace("%5C", "/").replace("%2F", "/").replace("\n", "").split("&");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].split("=")[1];
        }
        return split;
    }

    private String parseGetInput(String request) {
        return request.replace("/ArcSummary/", "").replace("%5B", "[").replace("%5D", "]").replace("%2C", ",");
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
                try {
                    URI requestURI = httpExchange.getRequestURI();
                    System.out.println(requestURI);
                    System.out.println(this.getClass() + " - received message from " + httpExchange.getRemoteAddress());
                    String file = parseGetInput(requestURI.toString());
                    if (OUTPUT_VIDEO_EXTENSION.equals(FilesCrawler.getExtension(file))) {
                        respondWithBytes(httpExchange, HOME_ADDRESS + file);
                    } else {
                        respondWithTextFile(httpExchange, HOME_ADDRESS + file);
                    }
                } catch (Exception e) {
                    respondWithText(httpExchange, getTraceError(e));
                }
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
                    Thread thread = null;
                    if (SUMMARY_FOLDER_NAME.equals(input[7])) {
                        thread = new Thread(() -> arcSummarizer.buildSummary(HOME_ADDRESS + input[7] + input[9], Double.valueOf(input[8])));
                    } else {
                        thread = new Thread(() -> arcSummarizer.buildSummary(input[7], Double.valueOf(input[8])));
                    }
                    arcSummarizerMap.put(input[9], new ArcSummaryThreadPair(thread, arcSummarizer));
                    thread.start();
                    respondWithText(httpExchange, "loading . . . ");
                } catch (Exception e) {
                    respondWithText(httpExchange, getTraceError(e));
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
                    respondWithText(httpExchange, "loading . . .  " + stringBuilder);
                } catch (Exception e) {
                    respondWithText(httpExchange, getTraceError(e));
                }
            }
        });

        summaryServer.createContext("/getVideo", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                try {
                    System.out.println(httpExchange.getRequestURI());
                    TextIO text = new TextIO();
                    text.read(httpExchange.getRequestBody());
                    String[] in = parseInput(text.getText());

                    String id = in[0];
                    int clusterId = Integer.valueOf(in[1]);
                    List<String> filesWithExtension = FilesCrawler.listFilesWithExtension(HOME_ADDRESS + SUMMARY_FOLDER_NAME + id + "/" + clusterId + "/", OUTPUT_VIDEO_EXTENSION);
                    StringBuilder stringBuilder = new StringBuilder();
                    // first append cluster number, this is necessary because communication is asynchronous
                    stringBuilder.append(in[1]).append(" ");
                    // append video addresses
                    for (int i = 0; i < filesWithExtension.size(); i++) {
                        String[] split = filesWithExtension.get(i).replace("\\", "/").split("/");
                        stringBuilder.append(split[split.length - 1]).append(i == filesWithExtension.size() - 1 ? "" : " ");
                    }
                    respondWithText(httpExchange, stringBuilder.toString());
                } catch (Exception e) {
                    respondWithText(httpExchange, getTraceError(e));
                }
            }
        });


        summaryServer.setExecutor(Executors.newCachedThreadPool());
        summaryServer.start();
    }


    private String getTraceError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        e.printStackTrace();
        return sw.toString();
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
