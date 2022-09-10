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
}
