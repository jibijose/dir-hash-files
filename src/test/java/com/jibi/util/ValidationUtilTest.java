package com.jibi.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

@PrepareForTest(ValidationUtil.class)
@RunWith(PowerMockRunner.class)
public class ValidationUtilTest {

    private static String CORRECT_OUT_FILE = "C:\\fileinfo.xlsx";
    private static String INCORRECT_OUT_FILE_1 = "C:\\fileinfo.xls";
    private static String INCORRECT_OUT_FILE_2 = "C:\\fileinfoxlsx";
    private static String INCORRECT_OUT_FILE_3 = "C:\\";

    @Before
    public void setup() {
        PowerMockito.mockStatic(ValidationUtil.class);
    }


    @Test
    public void test_validations_success() throws IOException {
        assertDoesNotThrow(() -> ValidationUtil.validateCreateHash("MD5", "C:\\", CORRECT_OUT_FILE));
    }

}
