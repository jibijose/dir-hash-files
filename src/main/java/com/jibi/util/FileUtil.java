package com.jibi.util;

import org.apache.commons.lang3.StringUtils;

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
}
