package org.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ResourceUtils {

    public static String getAbsolutePath(String path) {
//        if (!path.startsWith("/")) {
//            path = "/" + path;
//        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
//        String absolutePath = ResourceUtils.class.getResource(path).getPath();
//        if (absolutePath.contains(":") && absolutePath.startsWith("/")) {
//            absolutePath = absolutePath.substring(1);
//        }
        String absolutePath = null;
        try {
            // 获取资源流
            InputStream inputStream = ResourceUtils.class.getClassLoader()
                    .getResourceAsStream(path);
            if (inputStream == null) {
                throw new IOException("文件未找到");
            }
            // 创建临时文件
            Path tempFile = Files.createTempFile("resource-", ".tmp");
            // 复制内容到临时文件
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 关闭流
            inputStream.close();

            // 程序退出时删除临时文件（可选）
            tempFile.toFile().deleteOnExit();
            absolutePath = tempFile.toAbsolutePath().toString();
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
        return absolutePath;
    }

    public static void main(String[] args) {
        System.out.println("path: " + getAbsolutePath("intent_rules.json"));
    }
}
