package com.jibi.util;

import org.apache.commons.lang3.StringUtils;

public class FileUtil {

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
        if ( StringUtils.isEmpty(sideValue) ) {
            return false;
        }
        if ( !isDriveOrFolder(sideValue)) {
            return false;
        } else if ( !isFileInfoExcel(sideValue)) {
            return false;
        }
        return true;
    }
}
