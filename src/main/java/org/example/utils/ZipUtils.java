package org.example.utils;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {


    /**
     * 压缩单个文件
     */
    public static void zipFile(String sourceFile, String zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(sourceFile)) {

            ZipEntry zipEntry = new ZipEntry(new File(sourceFile).getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }

    /**
     * 压缩整个文件夹
     */
    public static void zipFolder(String sourceFolder, String zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            File folder = new File(sourceFolder);
            addFolderToZip(folder, folder.getName(), zos);
        }
    }

    private static void addFolderToZip(File folder, String parentPath,
                                       ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderToZip(file, parentPath + "/" + file.getName(), zos);
                continue;
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                String entryName = parentPath + "/" + file.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * 压缩多个文件
     */
    public static void zipMultipleFiles(List<String> filePathList, String zipFilePath) throws IOException {
        filePathList = new ArrayList<>(new LinkedHashSet<>(filePathList));
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : filePathList) {
                File file = new File(filePath);
                if (!file.exists()) continue;

                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * 解压ZIP文件
     */
    public static void unzip(String zipFile, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String filePath = destDir + File.separator + entry.getName();

                // 防止ZIP炸弹和路径遍历攻击
                if (!isSafePath(destDir, filePath)) {
                    throw new SecurityException("Unsafe ZIP entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    new File(filePath).mkdirs();
                    continue;
                }

                // 创建父目录
                new File(filePath).getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * 安全检查
     */
    private static boolean isSafePath(String destinationDir, String filePath) {
        try {
            File destFile = new File(filePath);
            File destDirFile = new File(destinationDir);

            String destNormalized = destFile.getCanonicalPath();
            String destDirNormalized = destDirFile.getCanonicalPath();

            return destNormalized.startsWith(destDirNormalized);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 列出ZIP文件内容
     */
    public static List<String> listZipContents(String zipFile) throws IOException {
        List<String> contents = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                contents.add(entry.getName() + " - " +
                        (entry.isDirectory() ? "目录" : "文件") +
                        " - " + entry.getSize() + " bytes");
                zis.closeEntry();
            }
        }
        return contents;
    }

    public static void main(String[] args) throws IOException {
//        List<String> pathFileList = Arrays.asList(new File("data/话术报备").listFiles()).stream()
//                .map(x -> x.getAbsolutePath()).collect(Collectors.toList());
//        zipMultipleFiles(pathFileList, "data/a.zip");
//        System.out.println(listZipContents("data/a.zip"));
        unzip("data/a.zip", "data/tmpa");
    }
}
