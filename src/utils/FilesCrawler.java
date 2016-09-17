package utils;

import java.io.File;
import java.util.*;

public class FilesCrawler {

    /**
     * @param directoryName
     * @return Map of all filesPathsByFileName under a directory
     */
    public static Map<String, String> listFilesRecursively(String directoryName) {
        File directory = new File(directoryName);
        Map<String, String> filePathByFileName = new HashMap<String, String>();
        Stack<File> stack = new Stack<>();
        stack.add(directory);
        while (!stack.isEmpty()) {
            File f = stack.pop();
            if (f.isFile()) {
                filePathByFileName.put(f.getName(), f.getAbsolutePath());
            } else if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (File file : files) {
                    stack.add(file);
                }
            }
        }
        return filePathByFileName;
    }

    /**
     * @param directoryName
     * @return Map of all filesPathsByFileName under a directory
     */
    public static List<String> listFilesWithExtension(String directoryName, String extension) {
        List<String> ans = new ArrayList<>();
        Map<String, String> filesMap = FilesCrawler.listFilesRecursively(directoryName);
        for (Map.Entry<String, String> entry : filesMap.entrySet()) {
            String address = entry.getValue();
            if (isExtension(address, extension)) {
                ans.add(address);
            }
        }
        return ans;
    }

    public static boolean isExtension(String fileName, String extension) {
        String[] s = fileName.split("\\.");
        return extension.equals(s[s.length - 1]);
    }
}
