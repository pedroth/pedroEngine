package other;

import tokenizer.SuffixTreeTokenizer;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class BuildPage {

    public static void buildJavaPage(String name, String path) {
        String[] special = {"<!--Special-->"};

        String text = "\n\n<h1>" + name + "</h1>\n" + "<object type=\"application/x-java-applet\" height=\"100\" width=\"250\">\n" + "<param name=\"code\" value=\"apps.utils.FrameApplet.class\" />\n" + "<param name=\"archive\" value=\"" + name + "Applet.jar\" />\nApplet failed to run.  No Java plug-in was found.\n" + " </object>\n" + " <p>input : </p><p>&lt; h &gt; : help button.</p>\n<p><a href='" + name + ".zip'> download applet and play it faster</a>\n";

//		if (name.equals("CellularAutomaton")) {
//			text = "<h1>CellularAutomaton</h1>\n" + "<object type=\"application/x-java-applet\" height=\"500\" width=\"500\">\n" + "<param name=\"code\" value=\"apps.ParallelCellularAutomaton.class\" />\n" + "<param name=\"archive\" value=\"CellularAutomatonApplet.jar\" />n" + "Applet failed to run.  No Java plug-in was found." + "</object>\n" + "<p>input : </p><p>&lt; h &gt; : help button.</p>\n";
//
//		} else if (name.equals("SimplePhysics")) {
//			text = "\n\n<h1>" + name + "</h1>\n" + "<object type=\"application/x-java-applet\" height=\"500\" width=\"500\">\n" + "<param name=\"code\" value=\"apps.SimplePhysics.class\" />\n" + "<param name=\"archive\" value=\"" + name + "Applet.jar\" />\nApplet failed to run.  No Java plug-in was found.\n" + " </object>\n" + " <p>input : </p><p>&lt; h &gt; : help button.</p>\n";
//		}

        try {
            SuffixTreeTokenizer parser = new SuffixTreeTokenizer(special);
            parser.init();
            BufferedReader reader = new BufferedReader(new FileReader("C:/pedro/visualExperiments/tools/canonWithComments.html"));
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
            // TODO Auto-generated catch block
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
            BufferedReader reader = new BufferedReader(new FileReader("C:/pedro/visualExperiments/tools/canonWithComments.html"));
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
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String[] getDir(String path) {
        File file = new File(path);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return directories;
    }

    public static String[] getFiles(String path) {
        File file = new File(path);
        String[] files = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
        return files;
    }

    public static void buildJava() {
        String path = "C:/pedro/visualExperiments/JavaExperiments";

        String[] directories = getDir(path);

        for (int i = 0; i < directories.length; i++) {
            buildJavaPage(directories[i], path + "/" + directories[i]);
            System.out.println(directories[i]);
        }
        System.out.println(Arrays.toString(directories));
    }

    public static void buildJs() {
        String path = "C:/pedro/visualExperiments/JsExperiments";

        String[] directories = getDir(path);

        for (int i = 0; i < directories.length; i++) {
            buildJsPage(directories[i], path + "/" + directories[i]);
            System.out.println(directories[i]);
        }
        System.out.println(Arrays.toString(directories));
    }

    public static void BuildWeb() {
        buildJava();
        buildJs();
    }

    public static void main(String[] args) {
        BuildWeb();
    }
}
