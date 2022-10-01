package com.jibi.util;

import com.jibi.file.ExcelPasswordProtection;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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

    public static boolean isValidFileExcelName(String filename) {
        if (filename == null) {
            log.warn("Null filename received");
            return false;
        }
        try {
            if (filename.endsWith(".xlsx")) {
                File file = new File(filename);
                if (file.exists()) {
                    log.warn("File {} will ve overwritten", filename);
                }
                return true;
            }
        } catch (Exception exception) {
            log.warn("Not able to get file excel {}", filename);
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
            if (directory.indexOf('\\') >= 0) {
                log.warn("Unix directory/drive should have back slashes");
                return false;
            }
        }
        try {
            File file = new File(directory);
            if (file.exists() && file.isDirectory()) {
                return true;
            }
        } catch (Exception exception) {
            log.warn("Not able to get file directory/drive {}", directory);
        }
        return false;
    }

    public static String getUserPasswordHidden(boolean passFlag, String filename) {
        String excelPassword = null;
        if (passFlag) {
            if (System.console() == null) {
                excelPassword = getUserInputFilePassword(String.format("password for %s", filename));
            } else {
                System.out.print(String.format("password for %s : ", filename));
                excelPassword = new String(System.console().readPassword());
            }
        }
        return excelPassword;
    }

    private static String getUserInputFilePassword(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(format("Enter %s : ", message));
        return scanner.nextLine();
    }

    public static void setExcelPassword(String filename, String excelPassword) throws Exception {
        ExcelPasswordProtection excelPasswordProtection = new ExcelPasswordProtection();
        excelPasswordProtection.encryptWorkbook(new File(filename), excelPassword);
        log.info("File {} encrypted", filename);
    }

    public static String adjustDirectoryOrDrive(String directory) {
        if (directory == null) {
            return directory;
        }
        String newDirectory = directory;
        if (SystemUtil.isWindowsSystem()) {
            if (!directory.endsWith(":\\") && directory.endsWith("\\")) {
                log.info("Directory {} ends with \\ in windows system, trimming end \\", directory);
                newDirectory = directory.substring(0, directory.length() - 1);
            }
            if (directory.endsWith(":")) {
                log.info("Directory {} ends with colon, appending \\ to it", directory);
                newDirectory = directory + "\\";
            }
        } else if (SystemUtil.isUnixSystem()) {
            if (!directory.equals("/") && directory.endsWith("/")) {
                log.info("Directory {} ends with / in unix system, trimming end /", directory);
                newDirectory = directory.substring(0, directory.length() - 1);
            }
        }
        log.info("Directory {} adjusted to {}", directory, newDirectory);
        return newDirectory;
    }

    public static String replaceFileName(String filename) {
        if (SystemUtil.isWindowsSystem()) {
            return filename.replaceAll("/", "\\\\");
        } else {
            return filename.replaceAll("\\\\", "/");
        }
    }

    public static String getDirValuePrefix(String dir) {
        if (SystemUtil.isWindowsSystem()) {
            if (dir.endsWith(":\\")) {
                return (dir).replaceAll("\\\\", "\\\\\\\\");
            } else {
                return (dir + "\\").replaceAll("\\\\", "\\\\\\\\");
            }
        } else if (SystemUtil.isUnixSystem()) {
            if (dir.equals("/")) {
                return dir;
            } else {
                return dir + "/";
            }
        }
        return dir;
    }

    public static String getFileRelativePath(File file, String dirValuePrefix) {
        return file.toString().replaceFirst(dirValuePrefix, "");
    }

    public static List<File> getFiles(String directory) {
        List<File> fileList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        for (File element : files) {
            if (element.isDirectory() && !Files.isSymbolicLink(element.toPath())) {
                if (!isTempDirectory(element.getPath())) {
                    fileList.addAll(getFiles(element.getPath()));
                }
            } else if (Files.isRegularFile(element.toPath()) && !Files.isSymbolicLink(element.toPath())) {
                if (!isTempFile(element.getPath())) {
                    fileList.add(element);
                }
            } else {
                log.trace("Not Dir/File {} ", element.toString());
            }
        }
        return fileList;
    }

    public static boolean ifFileWritable(String filename) {
        File file = new File(filename);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
            return true;
        } catch (IOException anyException) {
            return false;
        }
    }

    public static boolean isTempDirectory(String filename) {
        boolean fileTempStatus = false;
        String upperFileName = filename.toUpperCase();
        if (SystemUtil.isWindowsSystem()) {
            if (upperFileName.matches(".:\\\\\\$RECYCLE\\.BIN")) {
                fileTempStatus = true;
            } else if (upperFileName.matches(".:\\\\SYSTEM VOLUME INFORMATION")) {
                fileTempStatus = true;
            }
        } else if (SystemUtil.isUnixSystem()) {
            if (upperFileName.matches("\\/SYS\\/KERNEL")) {
                fileTempStatus = true;
            } else if (upperFileName.matches("\\/PROC")) {
                fileTempStatus = true;
            }
        }
        if (fileTempStatus) {
            log.info("Temp file/directory {}", filename);
        }
        return fileTempStatus;
    }

    public static boolean isTempFile(String filename) {
        boolean fileTempStatus = false;
        String upperFileName = filename.toUpperCase();
        if (SystemUtil.isWindowsSystem()) {

        } else if (SystemUtil.isUnixSystem()) {

        }
        if (fileTempStatus) {
            log.info("Temp file/directory {}", filename);
        }
        return fileTempStatus;
    }
}
