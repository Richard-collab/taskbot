package org.example.utils;

import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static List<String> readLines(String pathInput) throws IOException {
        return Files.readLines(new File(pathInput), StandardCharsets.UTF_8);
    }

    public static String readText(String pathInput) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(Paths.get(pathInput)), StandardCharsets.UTF_8);
    }

    public static void write(List<String> lineList, String pathOutput, boolean append) {
        File fileDir = new File(pathOutput).getParentFile();
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathOutput, append), StandardCharsets.UTF_8))) {
            for (String line: lineList) {
                out.write(line + "\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(String content, String pathOutput, boolean append) {
        write(Arrays.asList(content), pathOutput, append);
    }

    public static void makeDirsIfParentDirNotExists(String pathFile) {
        if (!StringUtils.isEmpty(pathFile)) {
            File fileDir = new File(pathFile).getParentFile();
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
        }
    }

    public static void main(String[] args) {
        makeDirsIfParentDirNotExists("data/img/1.txt");
    }
}
