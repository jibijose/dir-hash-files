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

    public static boolean isDriveOrFolder(String sideValue) {
        if (sideValue != null && !StringUtils.isEmpty(sideValue) && !sideValue.endsWith(".xlsx")) {
            return true;
        }
        return false;
    }

    public static boolean isFileInfoExcel(String sideValue) {
        if (sideValue != null && !StringUtils.isEmpty(sideValue) && sideValue.endsWith(".xlsx")) {
            return true;
        }
        return false;
    }

    public static boolean validDirDriveFileValue(String sideValue) {
        if (StringUtils.isEmpty(sideValue)) {
            return false;
        }
        if (isDriveOrFolder(sideValue)) {
            return true;
        }
        if (isFileInfoExcel(sideValue)) {
            return true;
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

    public static List<File> getFiles(String directory) {
        if (directory == null) {
            return Collections.EMPTY_LIST;
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
                //fileList.addAll(getFiles(element.getPath()));
                log.debug("DIR={}", element.getPath());
            } else {
                fileList.add(element);
                log.debug("FIL={}", element.getPath());
            }
        }
        return fileList;
    }
}
