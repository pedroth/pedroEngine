package utils;

import java.io.IOException;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * You need to have fmpeg installed
 */
public class FFMpegVideoApi {
    private static String ffmpegAddress = "C:/Program Files/ffmpeg/bin";

    private FFMpegVideoApi() {
        // empty on purpose
    }

    public static void cutVideo(String videoAddress, LocalTime ti, LocalTime tf, String outputAddress) {
        String command = addQuotingToString(ffmpegAddress + "/ffmpeg");
        command += " -ss " + ti + " -i " + addQuotingToString(videoAddress) + " -to " + tf + " -c copy -copyts " + addQuotingToString(outputAddress);
        try {
            StopWatch stopWatch = new StopWatch();
            CommandLineApi commandLineApi = new CommandLineApi();
            int state = commandLineApi.callCommand(command);
            System.out.println(state + ", time : " + stopWatch.getEleapsedTime());
        } catch (IOException e) {
            System.err.println("Error calling ffmpeg -ss -i -c copy -copyts");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static LocalTime getVideoDuration(String videoAddress) {
        String command = addQuotingToString(ffmpegAddress + "/ffmpeg");
        command += " -i " + addQuotingToString(videoAddress);
        System.out.println(command);
        String[] localTime = null;
        CommandLineApi commandLineApi = new CommandLineApi();
        try {
            commandLineApi.callCommand(command);
            String line = commandLineApi.getErrorStream().getText();
            Pattern pattern = Pattern.compile("(Duration:)\\s*((\\d+\\.?\\d*)|:)*");
            Matcher matcher = pattern.matcher(line);
            matcher.find();
            line = matcher.group(0);
            localTime = !line.isEmpty() ? line.split("Duration:\\s+") : new String[]{"00:00:00"};
        } catch (IOException e) {
            System.err.println("Error calling ffmpeg -i ");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return LocalTime.parse(localTime[1]);
    }

    public static void concat(String videoAddress1, String videoAddress2, String outputAddress) {
        String commandx = addQuotingToString(ffmpegAddress + "/ffmpeg") + " -i " + addQuotingToString(videoAddress1) + " -c copy -bsf:v h264_mp4toannexb -f mpegts intermediate1.ts";
        String commandy = addQuotingToString(ffmpegAddress + "/ffmpeg") + " -i " + addQuotingToString(videoAddress2) + " -c copy -bsf:v h264_mp4toannexb -f mpegts intermediate2.ts";
        String commandz = addQuotingToString(ffmpegAddress + "/ffmpeg") + "-i \"concat:intermediate1.ts|intermediate2.ts\" -c copy -bsf:a aac_adtstoasc " + outputAddress;
        CommandLineApi commandLineApi = new CommandLineApi();
        try {
            StopWatch stopWatch = new StopWatch();
            commandLineApi.callCommand(commandx);
            commandLineApi.callCommand(commandy);
            int state = commandLineApi.callCommand(commandz);
            System.out.println(state + ", time : " + stopWatch.getEleapsedTime());
        } catch (IOException e) {
            System.err.println("Error calling ffmpeg -ss -i -c copy -copyts");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String addQuotingToString(String s) {
        return "\"" + s + "\"";
    }

    public static void main(String[] args) {
        String videoAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWallS1E1.mkv";
        String outputAddress = "C:/Users/Pedroth/Desktop/cut0.mp4";
        FFMpegVideoApi.cutVideo(videoAddress, LocalTime.of(0, 7, 0, 22), LocalTime.of(0, 7, 39, 477), outputAddress);
        outputAddress = "C:/Users/Pedroth/Desktop/cut1.mp4";
        FFMpegVideoApi.cutVideo(videoAddress, LocalTime.of(0, 7, 39, 477), LocalTime.of(0, 8, 26, 358), outputAddress);
        outputAddress = "C:/Users/Pedroth/Desktop/cut2.mp4";
        FFMpegVideoApi.cutVideo(videoAddress, LocalTime.of(0, 8, 26, 358), LocalTime.of(0, 9, 23, 144), outputAddress);
        System.out.println("Duration: " + FFMpegVideoApi.getVideoDuration(videoAddress));
        FFMpegVideoApi.concat("C:/Users/Pedroth/Desktop/cut1.mp4", "C:/Users/Pedroth/Desktop/cut2.mp4", "C:/Users/Pedroth/Desktop/cut1+2.mp4");
    }

}
