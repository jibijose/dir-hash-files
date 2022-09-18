package com.jibi.util;

import com.jibi.file.ExcelPasswordProtection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static java.lang.String.format;

@Slf4j
public class FileUtil {

    public final static String NEWFILE = "NewFile";
    public final static String MATCH = "Match";
    public final static String MISMATCH = "Mismatch";
    public final static String MISSING = "Missing";

    public final static String INSYNC = "InSync";
    public final static String NOTSYNCED = "NotSynced";

    public static int PAD_MARK = 14;
    public static int PAD_HASH = 66;

    private static int FILE_PAD_HASH = 64;
    private static int FILE_PAD_SIZE = 15;
    private static int FILE_PAD_DATE = 36;


    public static boolean isValidFileOrDriectoryOrDrive(String value) {
        if (isValidFileExcel(value) || isValidDirectoryOrDrive(value)) {
            return true;
        }
        return false;
    }

    public static boolean isValidFileExcel(String filename) {
        if (filename == null) {
            log.warn("Null filename received");
            return false;
        }
        try {
            File file = new File(filename);
            if (file.exists() && file.isFile() && filename.endsWith(".xlsx")) {
                return true;
            }
            log.warn("{} is not a valid file excel xlsx", filename);
        } catch (Exception exception) {
            log.warn("Not able to get file excel {}", filename);
        }
        return false;
    }

    public static boolean isValidDirectoryOrDrive(String directory) {
        if (directory == null) {
            log.warn("Null directory received");
            return false;
        }
        if (SystemUtil.isWindowsSystem()) {
            if (directory.indexOf('/') >= 0) {
                log.warn("Windows directory/drive should have back slashes");
                return false;
            }
        } else if (SystemUtil.isUnixSystem()) {

        }
        try {
            File file = new File(directory);
            if (file.exists() && file.isDirectory()) {
                return true;
            }
            /*for (File fileDrive : File.listRoots()) {
                if (fileDrive.equals(file)) {
                    return true;
                }
            }*/
            log.warn("{} is not a valid directory/drive", directory);
        } catch (Exception exception) {
            log.warn("Not able to get file directory/drive {}", directory);
        }
        return false;
    }

    public static String getUserInputFilePassword(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(format("Enter %s : ", message));
        return scanner.nextLine();
    }

    public static void setExcelPassword(String filename) throws Exception {
        String excelPassword = FileUtil.getUserInputFilePassword(String.format("password for %s", filename));
        ExcelPasswordProtection excelPasswordProtection = new ExcelPasswordProtection();
        excelPasswordProtection.encryptWorkbook(new File(filename), excelPassword);
        log.info("File {} encrypted", filename);
    }

    public static String adjustDirectoryOrDrive(String directory) {
        String newDirectory = directory;
        if (directory != null && SystemUtil.isWindowsSystem()) {
            if (!directory.endsWith(":\\") && directory.endsWith("\\")) {
                log.info("Directory {} ends with \\ in windows system, trimming end \\", directory);
                newDirectory = directory.substring(0, directory.length() - 1);
            }
            if (directory.endsWith(":")) {
                log.info("Directory {} ends with colon, appending \\ to it", directory);
                newDirectory = directory + "\\";
            }
        } else if (directory != null && SystemUtil.isUnixSystem()) {
            if (!directory.equals("/") && directory.endsWith("/")) {
                log.info("Directory {} ends with / in unix system, trimming end /", directory);
                newDirectory = directory.substring(0, directory.length() - 1);
            }
        }
        log.info("Directory {} adjusted to {}", directory, newDirectory);
        return newDirectory;
    }

    public static String getDirValuePrefix(String dir) {
        if (SystemUtil.isWindowsSystem()) {
            if (dir.endsWith(":\\")) {
                return (dir).replaceAll("\\\\", "\\\\\\\\");
            } else {
                return (dir + "\\").replaceAll("\\\\", "\\\\\\\\");
            }
        } else if (SystemUtil.isUnixSystem()) {

        }
        return dir;
    }

    public static String getFileRelativePath(File file, String dirValuePrefix) {
        return file.toString().replaceFirst(dirValuePrefix, "");
    }

    public static List<File> getFiles(String directory) {
        if (directory == null) {
            log.warn("Null directory received");
            return Collections.EMPTY_LIST;
        }
        if (SystemUtil.isWindowsSystem() && !directory.endsWith(":\\") && directory.endsWith("\\")) {
            log.info("Directory {} ends with \\ in windows system, trimming end \\", directory);
            directory = directory.substring(0, directory.length() - 1);
        }
        if (SystemUtil.isUnixSystem() && directory.endsWith("/")) {
            log.info("Directory {} ends with / in unix system, trimming end /", directory);
            directory = directory.substring(0, directory.length() - 1);
        }
        if (directory.endsWith(":")) {
            log.info("Directory {} ends with colon, appending \\ to it", directory);
            directory = directory + "\\";
        }

        List<File> fileList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        for (File element : files) {
            if (element.isDirectory()) {
                fileList.addAll(getFiles(element.getPath()));
            } else {
                fileList.add(element);
            }
        }
        return fileList;
    }
}
