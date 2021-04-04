package nlp.seriesSummary;

import algebra.Vector;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.MaxDerivativeSegmentator;
import nlp.utils.RemoveStopWordsPredicate;
import nlp.utils.RemoveWordsPredicate;
import numeric.Distance;
import utils.FFMpegVideoApi;
import utils.FilesCrawler;
import utils.JServerUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * based on  http://www.programcreek.com/java-api-examples/index.php?source_dir=middleman-master/test/middleman/proxy/DummyHttpServer.java
 */
public class ArcSummaryServer {
    private final static String HOME_ADDRESS = "web/";
    private final static String OUTPUT_VIDEO_EXTENSION = "mp4";
    private static RemoveWordsPredicate necessaryWordPredicate;
    private final int serverPort;
    private Map<String, ArcSummaryThreadPair> arcSummarizerMap = new HashMap<>(3);

    public ArcSummaryServer(int serverPort) throws IOException {
        this.serverPort = serverPort;
        System.out.println(String.format("Arc Summary started at port %d", serverPort));
        try {
            necessaryWordPredicate = new RemoveStopWordsPredicate("stopWords.txt");
        } catch (IOException e) {
            necessaryWordPredicate = new RemoveStopWordsPredicate();
        }

    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            final String regex = "[0-9]*";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(args[0]);
            ArcSummaryServer arcSummaryServer = new ArcSummaryServer(matcher.find() ? Integer.valueOf(args[0]) : 8080);
            arcSummaryServer.start();
        } else {
            ArcSummaryServer arcSummaryServer = new ArcSummaryServer(8080);
            arcSummaryServer.start();
        }
        try {
            TextIO textIO = new TextIO("conf.txt");
            final String text = textIO.getText();
            final String[] split = text.split("\n");
            final String[] parameters = split[0].split("=");
            System.out.println(parameters[1]);
            FFMpegVideoApi.ffmpegAddress = parameters[1];
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            return;
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
                        JServerUtils.respondWithBytes(httpExchange, HOME_ADDRESS + file);
                        return;
                    }
                    if ("/ArcSummary".equals(file)) {
                        JServerUtils.respondWithTextFile(httpExchange, HOME_ADDRESS + "ArcSummary.html");
                    } else {
                        JServerUtils.respondWithTextFile(httpExchange, HOME_ADDRESS + file);
                    }
                } catch (Exception e) {
                    JServerUtils.respondWithText(httpExchange, getTraceError(e));
                }
            }
        });

        summaryServer.createContext("/input", httpExchange -> {
            try {
                System.out.println(httpExchange.getRequestURI());
                TextIO text = new TextIO();
                text.read(httpExchange.getRequestBody());
                Map<String, String> input = JServerUtils.parsePostMessage(text.getText());
                ArcSummaryBuilder arcSummaryBuilder = new ArcSummaryBuilder()
                        .setDistance(BaseArcSummarizer.getDistanceByName(input.get("distance")))
                        .setEntropy(Double.valueOf(input.get("entropy")))
                        .setFileExtension(input.get("video"))
                        .setHeat(Double.valueOf(input.get("heat")))
                        .setKnn(Integer.valueOf(input.get("knn")))
                        .setNumberOfCluster(Integer.valueOf(input.get("kcluster")))
                        .setSeriesAddress(input.get("file"));
                final BaseArcSummarizer arcSummarizer = ArcSummaryProviderEnum.getSummarizerByName(input.get("method"), arcSummaryBuilder);
                Thread thread = new Thread(() -> arcSummarizer.buildSummary(HOME_ADDRESS + input.get("out") + input.get("id"), Double.valueOf(input.get("time"))));
                arcSummarizerMap.put(input.get("id"), new ArcSummaryThreadPair(thread, arcSummarizer));
                thread.start();
                JServerUtils.respondWithText(httpExchange, "loading . . . ");
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, getTraceError(e));
            }
        });


        summaryServer.createContext("/log", httpExchange -> {
            try {
                System.out.println(httpExchange.getRequestURI());
                TextIO text = new TextIO();
                text.read(httpExchange.getRequestBody());
                Map<String, String> in = JServerUtils.parsePostMessage(text.getText());

                ArcSummaryThreadPair arcSummaryThreadPair = arcSummarizerMap.get(in.get("id"));

                StringBuilder stringBuilder = new StringBuilder();
                for (String log : arcSummaryThreadPair.getArcSummarySummarizer().getLog()) {
                    stringBuilder.append(log + "<br>");
                }
                JServerUtils.respondWithText(httpExchange, "loading . . .  " + stringBuilder);
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, getTraceError(e));
            }
        });

        summaryServer.createContext("/getVideo", httpExchange -> {
            try {
                System.out.println(httpExchange.getRequestURI());
                TextIO text = new TextIO();
                text.read(httpExchange.getRequestBody());
                Map<String, String> in = JServerUtils.parsePostMessage(text.getText());
                int clusterId = Integer.valueOf(in.get("clusterId"));
                StringBuilder stringBuilder = new StringBuilder();
                // first append cluster number, this is necessary because communication is asynchronous
                stringBuilder.append(clusterId).append(" ");
                stringBuilder.append("Arc" + clusterId + "Summary.mp4");
                JServerUtils.respondWithText(httpExchange, stringBuilder.toString());
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, getTraceError(e));
            }
        });

        summaryServer.createContext("/getInit", httpExchange -> {
            try {
                System.out.println(httpExchange.getRequestURI());
                StringBuilder stringBuilder = new StringBuilder();
                for (ArcSummaryProviderEnum provider : ArcSummaryProviderEnum.values()) {
                    stringBuilder.append(provider.getName()).append(" ");
                }
                JServerUtils.respondWithText(httpExchange, stringBuilder.toString());
            } catch (Exception e) {
                JServerUtils.respondWithText(httpExchange, getTraceError(e));
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


    private enum ArcSummaryProviderEnum {
        SPECTRAL_NG("SpectralNG", x -> {
            BaseArcSummarizer baseArcSummarizer = new ArcSummarizerSpectral(x.seriesAddress, x.fileExtension, x.heat, x.entropy, x.knn, x.numberOfCluster, x.distance);
            baseArcSummarizer.setNecessaryWordPredicate(necessaryWordPredicate);
            baseArcSummarizer.setCutVideo(true);
            baseArcSummarizer.setVideoConcat(true);
            baseArcSummarizer.setLowBowSegmentator(MaxDerivativeSegmentator.getInstance());
            ((ArcSummarizerSpectral) baseArcSummarizer).setSpectralType(ArcSummarizerSpectral.SpectralTypeEnum.ANDREW_ET_AL);
            return baseArcSummarizer;
        }),
        SPECTRAL_NORM("SpectralNorm", x -> {
            BaseArcSummarizer baseArcSummarizer = new ArcSummarizerSpectral(x.seriesAddress, x.fileExtension, x.heat, x.entropy, x.knn, x.numberOfCluster, x.distance);
            baseArcSummarizer.setNecessaryWordPredicate(necessaryWordPredicate);
            baseArcSummarizer.setCutVideo(true);
            baseArcSummarizer.setVideoConcat(true);
            baseArcSummarizer.setLowBowSegmentator(MaxDerivativeSegmentator.getInstance());
            ((ArcSummarizerSpectral) baseArcSummarizer).setSpectralType(ArcSummarizerSpectral.SpectralTypeEnum.NORM);
            return baseArcSummarizer;
        }),
        SPECTRAL_UNORM("SpectralUNorm", x -> {
            BaseArcSummarizer baseArcSummarizer = new ArcSummarizerSpectral(x.seriesAddress, x.fileExtension, x.heat, x.entropy, x.knn, x.numberOfCluster, x.distance);
            baseArcSummarizer.setNecessaryWordPredicate(necessaryWordPredicate);
            baseArcSummarizer.setCutVideo(true);
            baseArcSummarizer.setVideoConcat(true);
            baseArcSummarizer.setLowBowSegmentator(MaxDerivativeSegmentator.getInstance());
            ((ArcSummarizerSpectral) baseArcSummarizer).setSpectralType(ArcSummarizerSpectral.SpectralTypeEnum.NNORM);
            return baseArcSummarizer;
        }),
        DIFFUSION("Diffusion", x -> {
            BaseArcSummarizer baseArcSummarizer = new ArcSummarizerDiffusion(x.seriesAddress, x.fileExtension, x.heat, x.entropy, x.knn, x.numberOfCluster, x.distance);
            baseArcSummarizer.setNecessaryWordPredicate(necessaryWordPredicate);
            baseArcSummarizer.setCutVideo(true);
            baseArcSummarizer.setVideoConcat(true);
            baseArcSummarizer.setLowBowSegmentator(MaxDerivativeSegmentator.getInstance());
            ((ArcSummarizerDiffusion) baseArcSummarizer).setHeatTime(-1);
            ((ArcSummarizerDiffusion) baseArcSummarizer).setNormalized(false);
            return baseArcSummarizer;
        }),
        LDA("LDA", x -> {
            BaseArcSummarizer baseArcSummarizer = new ArcSummarizerLda(x.seriesAddress, x.fileExtension, x.heat, x.entropy, x.knn, x.numberOfCluster, x.distance);
            baseArcSummarizer.setNecessaryWordPredicate(necessaryWordPredicate);
            baseArcSummarizer.setCutVideo(true);
            baseArcSummarizer.setVideoConcat(true);
            baseArcSummarizer.setLowBowSegmentator(MaxDerivativeSegmentator.getInstance());
            return baseArcSummarizer;
        });

        private static Map<String, ArcSummaryProviderEnum> providerEnumByNameMap = new HashMap<>(ArcSummaryProviderEnum.values().length);

        static {
            for (ArcSummaryProviderEnum provider : ArcSummaryProviderEnum.values()) {
                providerEnumByNameMap.put(provider.getName(), provider);
            }
        }

        private String name;
        private Function<ArcSummaryBuilder, BaseArcSummarizer> factory;

        ArcSummaryProviderEnum(String name, Function<ArcSummaryBuilder, BaseArcSummarizer> factory) {
            this.name = name;
            this.factory = factory;
        }

        public static BaseArcSummarizer getSummarizerByName(String name, ArcSummaryBuilder builder) {
            return providerEnumByNameMap.get(name).getFactory().apply(builder);
        }

        public String getName() {
            return name;
        }

        public Function<ArcSummaryBuilder, BaseArcSummarizer> getFactory() {
            return factory;
        }
    }

    private static class ArcSummaryBuilder {
        String seriesAddress;
        String fileExtension;
        int numberOfCluster;
        double heat;
        double entropy;
        int knn;
        Distance<Vector> distance;

        public ArcSummaryBuilder setSeriesAddress(String seriesAddress) {
            this.seriesAddress = seriesAddress;
            return this;
        }

        public ArcSummaryBuilder setFileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
            return this;
        }

        public ArcSummaryBuilder setNumberOfCluster(int numberOfCluster) {
            this.numberOfCluster = numberOfCluster;
            return this;
        }

        public ArcSummaryBuilder setHeat(double heat) {
            this.heat = heat;
            return this;
        }

        public ArcSummaryBuilder setEntropy(double entropy) {
            this.entropy = entropy;
            return this;
        }

        public ArcSummaryBuilder setKnn(int knn) {
            this.knn = knn;
            return this;
        }

        public ArcSummaryBuilder setDistance(Distance<Vector> distance) {
            this.distance = distance;
            return this;
        }
    }

    private class ArcSummaryThreadPair {
        private final BaseArcSummarizer arcSummarySummarizer;
        private final Thread thread;

        public ArcSummaryThreadPair(Thread thread, BaseArcSummarizer arcSummarySummarizer) {
            this.thread = thread;
            this.arcSummarySummarizer = arcSummarySummarizer;
        }

        public BaseArcSummarizer getArcSummarySummarizer() {
            return arcSummarySummarizer;
        }

        public Thread getThread() {
            return thread;
        }
    }
}
