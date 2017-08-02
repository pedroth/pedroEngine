package other;

import tokenizer.SuffixTreeTokenizer;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class BuildPage {
    private static String base = "C:/pedro/";
    private static String canonAddress = base + "visualExperiments/tools/canon.html";
    private static String canonWithCommentsAddress = base + "visualExperiments/tools/canonWithComments.html";
    private static String commentsAddress = base + "visualExperiments/tools/comments.html";
    private static String mainAddress = base + "visualExperiments/main.html";
    private static String indexAddress = base + "visualExperiments/index.html";
    private static String javaExperimentsAddress = base + "visualExperiments/JavaExperiments/JavaExperiments";
    private static String jsExperimentsAddress = base + "visualExperiments/JsExperiments/JsExperiments";
    private static String blogAddress = base + "visualExperiments/Blog/Blog";


    public static void buildJavaPage(String name, String path) {
        String[] special = {"<!--Special-->"};
        String text = "\n\n<h1>" + name + "</h1>\n";
        try {
            SuffixTreeTokenizer parser = new SuffixTreeTokenizer(special);
            parser.init();
            String content = new Scanner(new File(path + "/" + name + "App.html")).useDelimiter("\\Z").next();
            BufferedReader reader = new BufferedReader(new FileReader(canonWithCommentsAddress));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + name + ".html"));

            text += content;

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
        String[] special = {"<!--Special-->"};

        String text = "\n\n<h1>" + name + "</h1>\n";
        try {
            SuffixTreeTokenizer parser = new SuffixTreeTokenizer(special);
            parser.init();
            String content = new Scanner(new File(path + "/" + name + "App.html")).useDelimiter("\\Z").next();
            BufferedReader reader = new BufferedReader(new FileReader(canonWithCommentsAddress));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + name + ".html"));

            text += content;

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

    private static String[] getDir(String path) {
        File file = new File(path);
        return file.list((current, name) -> new File(current, name).isDirectory());
    }

    public static String[] getFiles(String path) {
        File file = new File(path);
        return file.list((current, name) -> new File(current, name).isFile());
    }

    private static void buildPages(String path, PageBuilder pageBuilder) {
        String[] directories = getDir(path);

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

    private static void BuildWeb() {
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

    public static void main(String[] args) {
        BuildWeb();
    }

    interface PageBuilder {
        void build(String name, String address);
    }
}
