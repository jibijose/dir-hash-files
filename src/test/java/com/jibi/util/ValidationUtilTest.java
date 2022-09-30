package com.jibi.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class ValidationUtilTest {

    private static String UNIX_TEMP = "/tmp";
    private static String WINDOWS_TEMP = System.getProperty("java.io.tmpdir");
    private static String CORRECT_WINDOWS_OUT_FILE = "fileinfo.xlsx";
    private static String CORRECT_WINDOWS_OUT_FILE_NEW = "fileinfonew.xlsx";

    private static String CORRECT_UNIX_OUT_FILE = "fileinfo.xlsx";
    private static String CORRECT_UNIX_OUT_FILE_NEW = "fileinfonew.xlsx";

    @Test
    public void test_validations_success() {
        if (SystemUtil.isWindowsSystem()) {
            assertDoesNotThrow(() -> ValidationUtil.validateCreateHash("MD5", WINDOWS_TEMP, CORRECT_WINDOWS_OUT_FILE));
            assertDoesNotThrow(() -> ValidationUtil.validateRecreateHash("MD5", WINDOWS_TEMP, CORRECT_WINDOWS_OUT_FILE, CORRECT_WINDOWS_OUT_FILE_NEW));
        } else {
            assertDoesNotThrow(() -> ValidationUtil.validateCreateHash("MD5", UNIX_TEMP, CORRECT_UNIX_OUT_FILE));
            assertDoesNotThrow(() -> ValidationUtil.validateRecreateHash("MD5", UNIX_TEMP, CORRECT_UNIX_OUT_FILE, CORRECT_UNIX_OUT_FILE_NEW));
        }
    }

}
