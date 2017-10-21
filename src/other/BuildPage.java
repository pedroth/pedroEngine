package other;

import inputOutput.TextIO;
import tokenizer.SuffixTreeTokenizer;
import utils.FilesCrawler;
import utils.Zipper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class BuildPage {
    private final static String base = "C:/pedro/";
    private final static String jarBuildingAddress = base + "visualExperiments/tools/JarsBuilding/";
    private final static String canonAddress = base + "visualExperiments/tools/canon.html";
    private final static String canonWithCommentsAddress = base + "visualExperiments/tools/canonWithComments.html";
    private final static String commentsAddress = base + "visualExperiments/tools/comments.html";
    private final static String mainAddress = base + "visualExperiments/main.html";
    private final static String indexAddress = base + "visualExperiments/index.html";
    private final static String javaExperimentsAddress = base + "visualExperiments/JavaExperiments/JavaExperiments";
    private final static String jsExperimentsAddress = base + "visualExperiments/JsExperiments/JsExperiments";
    private final static String blogAddress = base + "visualExperiments/Blog/Blog";
    private final static Map<String, List<String>> jarConfig = new HashMap<>();

    static {
        try {
            //assumes config is correct
            TextIO textIO = new TextIO(jarBuildingAddress + "config");
            final String text = textIO.getText();
            for (String line : text.split("\n")) {
                final String[] split = line.split(" ");
                List<String> list = new ArrayList<>(split.length - 1);
                for (int i = 1; i < split.length; i++) {
                    list.add(split[i]);
                }
                jarConfig.put(split[0], list);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void buildJavaPage(String name, String path) throws IOException {
        fillPage(name, path);
        // add Java zip
        // create folder
        File file = new File(name);
        file.mkdir();
        // generate README and run.bat
        TextIO textIO = new TextIO();
        String readMe = textIO.read(jarBuildingAddress + "README.txt");
        String runBat = textIO.read(jarBuildingAddress + "run.bat");
        String newJarName = name + ".jar";
        final List<String> config = jarConfig.get(name);
        readMe = readMe.replace("<jar>", newJarName);
        readMe = readMe.replace("<app name>", config.get(1));
        runBat = runBat.replace("<jar>", newJarName);
        runBat = runBat.replace("<app name>", config.get(1));
        // copy jar
        Files.copy(Paths.get(jarBuildingAddress + config.get(0)), Paths.get(name + "/" + newJarName), StandardCopyOption.REPLACE_EXISTING);
        textIO.write(name + "/README.txt", readMe);
        textIO.write(name + "/run.bat", runBat);
        textIO.write(name + "/run.sh", runBat);
        Zipper.zipIt(name, path + "/" + name + ".zip");
        FilesCrawler.applyFiles(name, File::delete);
        file.delete();
    }

    private static void fillPage(String name, String path) {
        String[] special = {"<!--Special-->"};
        StringBuilder text = new StringBuilder("\n\n<h1>" + name + "</h1>\n");
        try {
            SuffixTreeTokenizer parser = new SuffixTreeTokenizer(special);
            parser.init();
            String content = new Scanner(new File(path + "/" + name + "App.html")).useDelimiter("\\Z").next();
            BufferedReader reader = new BufferedReader(new FileReader(canonWithCommentsAddress));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + name + ".html"));

            text.append(content);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] aux = parser.tokenize(line);
                System.out.println(line);
                if (aux.length == 0) {
                    writer.write(line + "\n");
                } else {
                    writer.write(text + "\n");
                    writer.write("</br></br></br><p>Download app here :<a href='" + name + ".zip'>" + name + ".zip</a></p>");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildJsPage(String name, String path) {
        fillPage(name, path);
    }

    private static void buildPages(String path, PageBuilder pageBuilder) throws IOException {
        String[] directories = FilesCrawler.getDirs(path);

        for (String directory : directories) {
            pageBuilder.build(directory, path + "/" + directory);
            System.out.println(directory);
        }
        System.out.println(Arrays.toString(directories));
    }

    private static void buildPage(String templateAddress, String contentAddress, String outputAddress) {
        String[] special = {"<!--Special-->"};
        StringBuilder text = new StringBuilder();
        try {
            SuffixTreeTokenizer parser = new SuffixTreeTokenizer(special);
            parser.init();
            String content = new Scanner(new File(contentAddress)).useDelimiter("\\Z").next();
            BufferedReader reader = new BufferedReader(new FileReader(templateAddress));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputAddress));

            text.append(content);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] aux = parser.tokenize(line);
                System.out.println(line);
                if (aux.length == 0) {
                    writer.write(line + "\n");
                } else {
                    writer.write(text + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void BuildWeb() throws IOException {
        String pathJava = "C:/pedro/visualExperiments/JavaExperiments";
        String pathJs = "C:/pedro/visualExperiments/JsExperiments";
        String pathBlog = "C:/pedro/visualExperiments/Blog";

        buildPage(canonAddress, commentsAddress, canonWithCommentsAddress);
        buildPage(canonAddress, mainAddress, indexAddress);
        buildPage(canonAddress, javaExperimentsAddress + "App.html", javaExperimentsAddress + ".html");
        buildPage(canonAddress, jsExperimentsAddress + "App.html", jsExperimentsAddress + ".html");
        buildPage(canonAddress, blogAddress + "App.html", blogAddress + ".html");
        buildPages(pathJava, BuildPage::buildJavaPage);
        buildPages(pathJs, BuildPage::buildJsPage);
        buildPages(pathBlog, BuildPage::buildJsPage);
    }

    public static void main(String[] args) throws IOException {
        BuildWeb();
    }

    interface PageBuilder {
        void build(String name, String address) throws IOException;
    }
}
