package other;


import com.sun.net.httpserver.HttpServer;
import inputOutput.TextIO;
import utils.JServerUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PublicChatServer {
    private final static String HOME_ADDRESS = "src/other/resources/";
    private final int serverPort;
    private List<String> log = new ArrayList<>();
    private JServerUtils serverUtils = new JServerUtils();

    public PublicChatServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public static void main(String[] args) {
        PublicChatServer publicChatServer = new PublicChatServer(8080);
        publicChatServer.start();
    }

    public void start() {
        HttpServer summaryServer = null;
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverPort);
            System.out.println("Start public chat server at : " + inetSocketAddress);
            summaryServer = HttpServer.create(inetSocketAddress, 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        summaryServer.createContext("/PublicChat", httpExchange -> {
            try {
                URI requestURI = httpExchange.getRequestURI();
                System.out.println(httpExchange.getRequestMethod() + " " + requestURI);
                System.out.println("address : " + httpExchange.getRemoteAddress());
                String file = parseGetInput(requestURI.toString());
                if ("".equals(file)) {
                    file = "PublicChat.html";
                }
                serverUtils.respondWithTextFile(httpExchange, HOME_ADDRESS + file);
            } catch (IOException e) {
                serverUtils.respondWithText(httpExchange, "<p>" + getTraceError(e) + "<p>");
            }
        });


        summaryServer.createContext("/chat", httpExchange -> {
            try {
                URI requestURI = httpExchange.getRequestURI();
                System.out.println(httpExchange.getRequestMethod() + " " + requestURI);
                System.out.println("address : " + httpExchange.getRemoteAddress());
                TextIO textIO = new TextIO();
                textIO.read(httpExchange.getRequestBody());
                String[] in = parsePostInput(textIO.getText());
                for (String s : in) {
                    System.out.println(s);
                }
            } catch (Exception e) {
                serverUtils.respondWithText(httpExchange, "<p>" + getTraceError(e) + "<p>");
            }
        });

        summaryServer.setExecutor(null);
        summaryServer.start();
    }

    private String parseGetInput(String request) {
        return request.replace("/PublicChat/", "").replace("%5B", "[").replace("%5D", "]").replace("%2C", ",");
    }

    private String[] parsePostInput(String request) {
        String[] split = request.replace("%3A", ":").replace("%5C", "/").replace("%2F", "/").replace("\n", "").split("&");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].split("=")[1];
        }
        return split;
    }

    private String getTraceError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        e.printStackTrace();
        return sw.toString();
    }
}
