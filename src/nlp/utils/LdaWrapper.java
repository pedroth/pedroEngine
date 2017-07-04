package nlp.utils;


import utils.StopWatch;
import utils.StreamGobbler;

import java.io.IOException;

public class LdaWrapper {
    public static String jLDADMMAddress = "lib/";

    public static void computeLda(String corpusAddress, int numberOfTopics, double alpha, double beta, int iterations, String modelName) {
        String command = "java -jar " + jLDADMMAddress + "jLDADMM.jar -model LDA" +
                " -corpus " + corpusAddress +
                " -ntopics " + numberOfTopics +
                " -alpha " + alpha +
                " -beta " + beta +
                " -niters " + iterations +
                " -name " + modelName;
        try {
            Runtime runtime = Runtime.getRuntime();
            StopWatch stopWatch = new StopWatch();
            Process proc = runtime.exec(command);

            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            errorGobbler.start();
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            outputGobbler.start();

            int state = proc.waitFor();
            System.out.println(state + ", time : " + stopWatch.getEleapsedTime());
        } catch (IOException e) {
            System.err.println("java -jar " + jLDADMMAddress + "/jLDADMM.jar -model LDA");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LdaWrapper.computeLda("C:/pedro/escolas/ist/Tese/jLDADMM_v1.0.1/test/corpus.txt", 20, 0.1, 0.01, 2000, "TestLda");
    }
}
