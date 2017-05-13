package other;

import inputOutput.TextIO;
import table.src.HyperTable;
import utils.FilesCrawler;

import java.util.HashSet;

public class BuildArcSummaryDataScript {

    public static void main(String[] args) {
        String address = "C:/pedro/escolas/ist/Tese/Stuff/Data/4th_stop_word_list_automatic_params";

        HashSet<String> xCoord = new HashSet<>();
        HashSet<String> yCoord = new HashSet<>();

        // build series/method table
        HyperTable<String, String> histogramDataTable = new HyperTable<>(2);
        HyperTable<String, String> numberOfSegmentsTable = new HyperTable<>(2);
        final String[] dirs = FilesCrawler.getDirs(address);
        for (String dir : dirs) {
            final String[] seriesDirs = FilesCrawler.getDirs(address + "/" + dir);
            for (String seriesDir : seriesDirs) {
                final String methodName = getMethodName(address + "/" + dir + "/" + seriesDir);
                xCoord.add(dir);
                yCoord.add(methodName);
                histogramDataTable.set(new String[]{dir, methodName}, getHistogramData(address + "/" + dir + "/" + seriesDir));
                numberOfSegmentsTable.set(new String[]{dir, methodName}, getNumberOfSegmentsData(address + "/" + dir + "/" + seriesDir));
            }
        }
        StringBuilder matlabDataScript = new StringBuilder();
        fillMethodAndSeriesVars(xCoord, yCoord, matlabDataScript);
        fillHistogramData(xCoord, yCoord, histogramDataTable, matlabDataScript);

        System.out.println(matlabDataScript.toString());
        System.out.println();
        matlabDataScript = new StringBuilder();
        fillNumberOfSegmentsData(xCoord, yCoord, numberOfSegmentsTable, matlabDataScript);
        System.out.println(matlabDataScript.toString());
    }

    private static String getNumberOfSegmentsData(String address) {
        StringBuilder stringBuilder = new StringBuilder();
        TextIO textIO = new TextIO(address + "/info/numberOfSegmentsPerCluster.txt");
        final String text = textIO.getText();
        final String[] lines = text.split("\n");
        stringBuilder.append("[");
        for (String line : lines) {
            final String[] split = line.split("\t");
            stringBuilder.append(split[1] + ",");
        }
        return stringBuilder.append("]").toString();

    }

    private static void fillNumberOfSegmentsData(HashSet<String> xCoord, HashSet<String> yCoord, HyperTable<String, String> numberOfSegmentsTable, StringBuilder matlabDataScript) {
        matlabDataScript.append("NumberOfSegments = [];");
        int i = 1;
        for (String seriesNames : xCoord) {
            int j = 1;
            for (String methodName : yCoord) {
                matlabDataScript.append("NumberOfSegments(" + i + ",:," + j + ") = " + numberOfSegmentsTable.get(new String[]{seriesNames, methodName}) + ";\n");
                j++;
            }
            i++;
        }
    }

    private static void fillHistogramData(HashSet<String> xCoord, HashSet<String> yCoord, HyperTable<String, String> hyperTable, StringBuilder matlabDataScript) {
        matlabDataScript.append("InterHist  = []\n");
        matlabDataScript.append("IntraHist  = []\n");
        matlabDataScript.append("InterRange = []\n");
        matlabDataScript.append("IntraRange = []\n");
        int i = 1;
        for (String methodName : yCoord) {
            fillMatlabMatrix(matlabDataScript, methodName, xCoord, hyperTable, i);
            i++;
        }
    }

    private static void fillMethodAndSeriesVars(HashSet<String> xCoord, HashSet<String> yCoord, StringBuilder matlabDataScript) {
        matlabDataScript.append("methodName = {");
        for (String methodName : yCoord) {
            matlabDataScript.append("'" + methodName + "'").append(",");
        }
        matlabDataScript.append("};\n");
        matlabDataScript.append("seriesName = {");
        for (String seriesName : xCoord) {
            matlabDataScript.append("'" + seriesName + "'").append(",");
        }
        matlabDataScript.append("};\n");
    }

    private static void fillMatlabMatrix(StringBuilder matlabDataScript, String methodName, HashSet<String> xCoord, HyperTable<String, String> hyperTable, int i) {
        int j = 1;
        for (String seriesName : xCoord) {
            final String data = hyperTable.get(new String[]{seriesName, methodName});
            final String[] intraInterData = data.split("\n");

            matlabDataScript.append("InterHist(" + j + ",:," + i + ") = ");
            fillMatrix(matlabDataScript, intraInterData[0]);
            matlabDataScript.append("\n");
            matlabDataScript.append("InterRange(" + j + ",:," + i + ") = ");
            fillMatrix(matlabDataScript, intraInterData[1]);
            matlabDataScript.append("\n");
            matlabDataScript.append("IntraHist(" + j + ",:," + i + ") = ");
            fillMatrix(matlabDataScript, intraInterData[2]);
            matlabDataScript.append("\n");
            matlabDataScript.append("IntraRange(" + j + ",:," + i + ") = ");
            fillMatrix(matlabDataScript, intraInterData[3]);
            matlabDataScript.append("\n");
            j++;
        }
    }

    private static void fillMatrix(StringBuilder matlabDataScript, String data) {
        matlabDataScript.append("[");
        final String[] split = data.split(",");
        for (int i = 0; i < split.length; i++) {
            matlabDataScript.append(split[i] + (i == (split.length - 1) ? "];" : ","));
        }
    }

    private static String getHistogramData(String seriesDir) {
        StringBuilder stringBuilder = new StringBuilder();
        final String InterAddress = seriesDir + "/InterClusterDistanceHist.txt";
        final String IntraAddress = seriesDir + "/IntraClusterDistanceHist.txt";
        getHistogramData(stringBuilder, InterAddress);
        getHistogramData(stringBuilder, IntraAddress);
        return stringBuilder.toString();
    }

    private static void getHistogramData(StringBuilder stringBuilder, String histAddressData) {
        TextIO textIO = new TextIO(histAddressData);
        final String[] split = textIO.getText().split("===\nmin\tmax");
        final String[] interHist = split[0].split("\n");
        for (String data : interHist) {
            stringBuilder.append(data.replace("\n", "")).append(",");
        }
        stringBuilder.append("\n");
        final String[] interRange = split[1].split("\t");
        for (String data : interRange) {
            stringBuilder.append(data.replace("\n", "")).append(",");
        }
        stringBuilder.append("\n");
    }

    private static String getMethodName(String seriesDir) {
        TextIO textIO = new TextIO(seriesDir + "/info/Param.txt");
        final String text = textIO.getText();
        final String methodRaw = text.split("\n")[0];
        String mainMethod = methodRaw.split("\\{")[0];
        //ugly exception if
        if ("ArcSummarizerSpectral".equals(mainMethod)) {
            mainMethod = methodRaw.split("spectralType=")[1];
            mainMethod = mainMethod.replace("}", "");
        }
        return mainMethod;
    }
}