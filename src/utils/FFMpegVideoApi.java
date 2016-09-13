package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;

/**
 * You need to have fmpeg installed
 */
public class FFMpegVideoApi {
    private static String ffmpegAddress = "C:/Program Files/ffmpeg/bin";

    private FFMpegVideoApi() {
        // empty on purpose
    }

    public static String getFfmpegAddress() {
        return ffmpegAddress;
    }

    public static void setFfmpegAddress(String ffmpegAddress) {
        FFMpegVideoApi.ffmpegAddress = ffmpegAddress;
    }

    public static void cutVideo(String videoAddress, LocalTime ti, LocalTime tf, String outputAddress) {
        String command = addQuotingToString(ffmpegAddress + "/ffmpeg");
        command += " -ss " + ti + " -i " + addQuotingToString(videoAddress) + " -to " + tf + " -c copy -copyts " + addQuotingToString(outputAddress);
        System.out.println(command);
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            System.err.println("Error calling ffmpeg -ss -i -c copy -copyts");
            e.printStackTrace();
        }
    }

    //TODO
    public static LocalTime getVideoDuration(String videoAddress) {
        String command = addQuotingToString(ffmpegAddress + "/ffmpeg");

        command += " -i " + addQuotingToString(videoAddress) + " 2>&1 | grep Duration | awk '{print $2}'";
        System.out.println(command);
        String[] localTime = null;
        try {
            String line = executeCommand(command);
            localTime = !line.isEmpty() ? line.split(",") : new String[]{"00:00:00"};
        } catch (IOException e) {
            System.err.println("Error calling ffmpeg -i |grep |awk");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return LocalTime.parse(localTime[0]);
    }

    private static String addQuotingToString(String s) {
        return "\"" + s + "\"";
    }

    private static String executeCommand(String command) throws IOException, InterruptedException {

        StringBuffer output = new StringBuffer();

        Process p;
        p = Runtime.getRuntime().exec(command);
        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }

        return output.toString();

    }

    public static void main(String[] args) {

        String videoAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.mkv";
        String outputAddress = "C:/Users/Pedroth/Desktop/cut0.mp4";
        FFMpegVideoApi.cutVideo(videoAddress, LocalTime.of(0, 7, 0, 22), LocalTime.of(0, 7, 39, 477), outputAddress);
        outputAddress = "C:/Users/Pedroth/Desktop/cut1.mp4";
        FFMpegVideoApi.cutVideo(videoAddress, LocalTime.of(0, 7, 39, 477), LocalTime.of(0, 8, 26, 358), outputAddress);
        outputAddress = "C:/Users/Pedroth/Desktop/cut2.mp4";
        FFMpegVideoApi.cutVideo(videoAddress, LocalTime.of(0, 8, 26, 358), LocalTime.of(0, 9, 23, 144), outputAddress);
        System.out.println(FFMpegVideoApi.getVideoDuration(videoAddress));
    }

}
