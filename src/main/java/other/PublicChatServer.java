package other;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import inputOutput.TextIO;
import stateMachine.State;
import tokenizer.SuffixTreeTokenizer;
import utils.FilesCrawler;
import utils.JServerUtils;
import utils.StopWatch;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PublicChatServer {
    private final static String HOME_ADDRESS = "src/main/java/other/resources/";
    private final static String DATA_ADDRESS = HOME_ADDRESS + "/data/";
    // time in seconds
    private final static double TIMEOUT = 10;
    private static int BUFFER_SIZE = 2000000 * (1 << 10);
    private final int serverPort;
    private List<UnitLog> log = new ArrayList<>();
    private Map<String, Double> uID2TimeMap = new ConcurrentHashMap<>();
    private StopWatch stopWatch;

    public PublicChatServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public PublicChatServer(int serverPort, int uploadBytes) {
        this.serverPort = serverPort;
        this.BUFFER_SIZE = uploadBytes;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            final String regex = "[0-9]*";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(args[0]);
            PublicChatServer publicChatServer = new PublicChatServer(matcher.find() ? Integer.valueOf(args[0]) : 8080, args.length > 1 ? Integer.valueOf(args[1]) : PublicChatServer.BUFFER_SIZE);
            publicChatServer.start();
        } else {
            PublicChatServer publicChatServer = new PublicChatServer(8080);
            publicChatServer.start();
        }
    }

    public void start() {
        HttpServer summaryServer = null;
        stopWatch = new StopWatch();

        //check for dead clients
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                double dt = stopWatch.getEleapsedTime();
                stopWatch.resetTime();
                for (String id : uID2TimeMap.keySet()) {
                    Double t = uID2TimeMap.get(id);
                    if (t > TIMEOUT) {
                        uID2TimeMap.remove(id);
                    } else {
                        uID2TimeMap.put(id, t + dt);
                    }
                }
            }
        }, 0, 1000);

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
                printClientData(httpExchange);
                String file = parseGetInput(httpExchange.getRequestURI().toString());
                if ("/PublicChat".equals(file)) {
                    file = "PublicChat.html";
                    JServerUtils.respondWithTextFile(httpExchange, HOME_ADDRESS + file);
                } else {
                    JServerUtils.respondWithBytes(httpExchange, HOME_ADDRESS + file);
                }
            } catch (IOException e) {
                JServerUtils.respondWithText(httpExchange, "<p>" + getTraceError(e) + "<p>");
            }
        });

        summaryServer.createContext("/chat", httpExchange -> {
            try {
                printClientData(httpExchange);
                TextIO textIO = new TextIO();
                textIO.read(httpExchange.getRequestBody());
                String text = textIO.getText();
                if ("".equals(text)) {
                    throw new RuntimeException("Empty String");
                }
                Map<String, String> stringMap = JServerUtils.parsePostMessage(text);
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                String id = stringMap.get("id");
                putClientInMap(id);
                String jsonAns = getLogInfo(Integer.valueOf(stringMap.get("index").replaceAll("\n", "")));
                System.out.println(jsonAns);
                JServerUtils.respondWithText(httpExchange, jsonAns);
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, "<p>" + getTraceError(e) + "<p>");
            }
        });

        summaryServer.createContext("/putText", httpExchange -> {
            try {
                printClientData(httpExchange);

                TextIO textIO = new TextIO();
                textIO.read(httpExchange.getRequestBody());
                Map<String, String> stringMap = JServerUtils.parsePostMessageUnformatted(textIO.getText());
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                String id = stringMap.get("id");
                putClientInMap(id);
                log.add(new UnitLog(id, stringMap.get("log")));
                JServerUtils.respondWithText(httpExchange, "OK");
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, "<p>" + getTraceError(e) + "<p>");
            }
        });

        summaryServer.createContext("/clear", httpExchange -> {
            try {
                printClientData(httpExchange);
                TextIO textIO = new TextIO();
                textIO.read(httpExchange.getRequestBody());
                Map<String, String> stringMap = JServerUtils.parsePostMessage(textIO.getText());
                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                log.removeAll(log);
                removeData();
                JServerUtils.respondWithText(httpExchange, "OK");
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, "<p>" + getTraceError(e) + "<p>");
            }
        });

        summaryServer.createContext("/upload", httpExchange -> {
            try {
                printClientData(httpExchange);
                String fileName = uploadFile(httpExchange);
                JServerUtils.respondWithText(httpExchange, fileName);
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, "<p>" + getTraceError(e) + "<p>");
            }
        });

        summaryServer.setExecutor(Executors.newCachedThreadPool());
        summaryServer.start();
    }

    private void removeData() {
        FilesCrawler.applyFiles(DATA_ADDRESS, File::delete);
    }

    private String uploadFile(HttpExchange he) throws IOException {
        int c;
        String fileName;
        OutputStream outputStream = null;
        final InputStream requestBody = he.getRequestBody();
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            SMUpload smUpload = new SMUpload(buffer);
            while ((c = requestBody.read()) != -1) {
                smUpload.next(c);
            }
            fileName = smUpload.getFileName();
            assert fileName == null : new RuntimeException("No file name");
            FilesCrawler.createDirs(DATA_ADDRESS);
            outputStream = new FileOutputStream(DATA_ADDRESS + fileName);
            outputStream.write(buffer, 0, smUpload.getIndex());
        } catch (Exception e) {
            throw e;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return fileName;
    }

    private String getLogInfo(int index) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{ \"users\": [");
        String[] uids = uID2TimeMap.keySet().toArray(new String[uID2TimeMap.size()]);
        for (int i = 0; i < uids.length; i++) {
            stringBuilder.append("\"" + uids[i] + "\"" + ((i == uids.length - 1) ? "" : ","));
        }

        stringBuilder.append("],");
        stringBuilder.append("\"log\": [");
        for (int i = index + 1; i < log.size(); i++) {
            UnitLog unitLog = log.get(i);
            stringBuilder.append("{\"id\": \"" + unitLog.getId() + "\", \"text\":\"" + unitLog.getText() + "\"}" + ((i == log.size() - 1) ? "" : ","));
        }

        stringBuilder.append("],");
        stringBuilder.append("\"needClean\": ");
        stringBuilder.append("" + (index >= log.size()) + "");
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    private String parseGetInput(String request) throws UnsupportedEncodingException {
        return URLDecoder.decode(request, StandardCharsets.UTF_8.toString()).replace("/PublicChat/", "");
    }


    private String getTraceError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        e.printStackTrace();
        return sw.toString();
    }

    private void printClientData(HttpExchange httpExchange) {
        URI requestURI = httpExchange.getRequestURI();
        System.out.println(httpExchange.getRequestMethod() + " " + requestURI);
        System.out.println("address : " + httpExchange.getRemoteAddress());
    }

    private void putClientInMap(String id) {
        uID2TimeMap.put(id, 0.0);
    }

    private static class SMUpload {
        private String fileName;
        private byte[] buffer;
        private State<Integer> state;
        private int index = 0;

        private State<Integer> stateFive = new State<Integer>() {
            @Override
            public State<Integer> next(Integer x) {
                return stateFive;
            }
        };

        private State<Integer> stateFour = new State<Integer>() {
            final String regex = "------WebKitFormBoundary";
            private SuffixTreeTokenizer tokenizer;

            {
                tokenizer = new SuffixTreeTokenizer(new String[]{regex});
                tokenizer.init();
            }

            @Override
            public State<Integer> next(Integer x) {
                final Optional<String> token = tokenizer.next((char) x.intValue());
                if (index < buffer.length) {
                    buffer[index] = x.byteValue();
                }
                if (token.isPresent()) {
                    index = index - regex.length() + 1;
                    return stateFive;
                }
                index++;
                return stateFour;
            }
        };

        private State<Integer> stateThree = new State<Integer>() {
            private int accNewLines = 0;

            @Override
            public State<Integer> next(Integer x) {
                if (x == '\n') {
                    accNewLines++;
                }
                if (accNewLines == 3) {
                    accNewLines = 0;
                    return stateFour;
                }
                return stateThree;
            }
        };

        private State<Integer> stateTwo = new State<Integer>() {
            private StringBuilder stack = new StringBuilder();

            @Override
            public State<Integer> next(Integer x) {
                if (x == '"') {
                    fileName = this.stack.toString();
                    this.stack = new StringBuilder();
                    return stateThree;
                }
                this.stack.append((char) x.intValue());
                return stateTwo;
            }
        };

        private State<Integer> stateOne = new State<Integer>() {
            private SuffixTreeTokenizer tokenizer;

            {
                tokenizer = new SuffixTreeTokenizer(new String[]{"filename=\""});
                tokenizer.init();
            }

            @Override
            public State<Integer> next(Integer x) {
                final Optional<String> next = tokenizer.next((char) x.intValue());
                if (next.isPresent()) {
                    return stateTwo;
                }
                return stateOne;
            }
        };

        SMUpload(byte[] buffer) {
            this.buffer = buffer;
            this.state = this.stateOne;
        }

        String getFileName() {
            return fileName;
        }

        int getIndex() {
            return index;
        }

        void next(int c) {
            state = state.next(c);
        }
    }

    private class UnitLog {
        private String id;
        private String text;

        public UnitLog(String id, String text) {
            this.id = id;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }


}
