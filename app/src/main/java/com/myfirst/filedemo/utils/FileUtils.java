package com.myfirst.filedemo.utils;

import java.io.File;

public class FileUtils {

    public static String getFileSuffix(String fileName) {
        if (fileName == null) return null;

        String[] parts = fileName.split("\\.");
        int length = parts.length;

        if (length > 1) {
            return "." + parts[length - 1];
        } else {
            return "";
        }
    }

    public static String getFileSuffix(File file) {
        if (file == null) return null;

        return getFileSuffix(file.getName());
    }

    public static String getFileNameWithoutSuffix(String fileName) {
        if (fileName == null) return null;

        String fileSuffix = getFileSuffix(fileName);

        return fileName.replace(fileSuffix, "");
    }

    public static String getFileNameWithoutSuffix(File file) {
        if (file == null) return null;

        String fileName = file.getName();

        return getFileNameWithoutSuffix(fileName);
    }

}
