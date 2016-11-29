package other;

import tokenizer.SuffixTreeTokenizer;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class BuildPage {
    static String base = "C:/pedro/";
    static String canonAddress = base + "visualExperiments/tools/canon.html";
    static String canonWithCommentsAddress = base + "visualExperiments/tools/canonWithComments.html";
    static String commentsAddress = base + "visualExperiments/tools/comments.html";
    static String mainAddress = base + "visualExperiments/main.html";
    static String indexAddress = base + "visualExperiments/index.html";
    static String javaExperimentsAddress = base + "visualExperiments/JavaExperiments/JavaExperiments";
    static String jsExperimentsAddress = base + "visualExperiments/JsExperiments/JsExperiments";
    static String blogAddress = base + "visualExperiments/Blog/Blog";


    public static void buildJavaPage(String name, String path) {
        String[] special = {"<!--Special-->"};

        String text = "<div class=\"col-sm-15 text-left\"> \n\n<h1>" + name + "</h1>\n" + "<object type=\"application/x-java-applet\" height=\"100\" width=\"250\">\n" + "<param name=\"code\" value=\"apps.FrameApplet.class\" />\n" + "<param name=\"archive\" value=\"" + name + "Applet.jar\" />\nApplet failed to run.  No Java plug-in was found.\n" + " </object>\n" + " <p>input : </p><p>&lt; h &gt; : help button.</p>\n<p><a href='" + name + ".zip'> download application and play it faster</a>\n</div>";

//		if (name.equals("CellularAutomaton")) {
//			text = "<h1>CellularAutomaton</h1>\n" + "<object type=\"application/x-java-applet\" height=\"500\" width=\"500\">\n" + "<param name=\"code\" value=\"apps.ParallelCellularAutomaton.class\" />\n" + "<param name=\"archive\" value=\"CellularAutomatonApplet.jar\" />n" + "Applet failed to run.  No Java plug-in was found." + "</object>\n" + "<p>input : </p><p>&lt; h &gt; : help button.</p>\n";
//
//		} else if (name.equals("SimplePhysics")) {
//			text = "\n\n<h1>" + name + "</h1>\n" + "<object type=\"application/x-java-applet\" height=\"500\" width=\"500\">\n" + "<param name=\"code\" value=\"apps.SimplePhysics.class\" />\n" + "<param name=\"archive\" value=\"" + name + "Applet.jar\" />\nApplet failed to run.  No Java plug-in was found.\n" + " </object>\n" + " <p>input : </p><p>&lt; h &gt; : help button.</p>\n";
//		}

        try {
            SuffixTreeTokenizer parser = new SuffixTreeTokenizer(special);
            parser.init();
            BufferedReader reader = new BufferedReader(new FileReader(canonWithCommentsAddress));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + name + ".html"));
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

    public static String[] getDir(String path) {
        File file = new File(path);
        return file.list((current, name) -> new File(current, name).isDirectory());
    }

    public static String[] getFiles(String path) {
        File file = new File(path);
        return file.list((current, name) -> new File(current, name).isFile());
    }

    public static void buildPages(String path, PageBuilder pageBuilder) {
        String[] directories = getDir(path);

        for (String directory : directories) {
            pageBuilder.build(directory, path + "/" + directory);
            System.out.println(directory);
        }
        System.out.println(Arrays.toString(directories));
    }

    public static void buildPage(String templateAddress, String contentAddress, String outputAddress, String token) {
        String[] special = {token};
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

    public static void BuildWeb() {
        String pathJava = "C:/pedro/visualExperiments/JavaExperiments";
        String pathJs = "C:/pedro/visualExperiments/JsExperiments";
        String pathBlog = "C:/pedro/visualExperiments/Blog";

        buildPage(canonAddress, commentsAddress, canonWithCommentsAddress, "<!--Special-->");
        buildPage(canonAddress, mainAddress, indexAddress, "<!--Special-->");
        buildPage(canonAddress, javaExperimentsAddress + "App.html", javaExperimentsAddress + ".html", "<!--Special-->");
        buildPage(canonAddress, jsExperimentsAddress + "App.html", jsExperimentsAddress + ".html", "<!--Special-->");
        buildPage(canonAddress, blogAddress + "App.html", blogAddress + ".html", "<!--Special-->");
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
