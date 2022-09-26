package com.jibi.util;

import com.jibi.common.Algorithm;
import org.apache.commons.lang3.StringUtils;

import static com.jibi.util.FileUtil.*;
import static com.jibi.util.FileUtil.isValidFileOrDriectoryOrDrive;
import static java.lang.String.format;

public class ValidationUtil {
    private static void commonValidation(String hashAlgoValue, String outFileValue) {
        if (!StringUtils.isEmpty(hashAlgoValue) && !Algorithm.isValidAlgo(hashAlgoValue)) {
            throw new RuntimeException(format("incorrect hash algo parameter %s Supported algorithms [MD2, MD5, SHA, SHA224, SHA256, SHA384, SHA512]", hashAlgoValue));
        }
        if (!isValidFileExcelName(outFileValue)) {
            throw new RuntimeException("Out file excel not correct");
        }
        if (!FileUtil.ifFileWritable(outFileValue)) {
            throw new RuntimeException("Out file excel not writable");
        }
    }

    public static void validateCreateHash(String hashAlgoValue, String inDirValue, String outFileValue) {
        commonValidation(hashAlgoValue, outFileValue);
        if (!isValidDirectoryOrDrive(inDirValue)) {
            throw new RuntimeException("In dir/drive value not correct");
        }
    }

    public static void validateRecreateHash(String hashAlgoValue, String inDirValue, String inFileValue, String outFileValue) {
        validateCreateHash(hashAlgoValue, inDirValue, outFileValue);
        if (!isValidFileExcelName(outFileValue)) {
            throw new RuntimeException("In file excel not correct");
        }
    }

    public static void validateCompareHash(String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String outFileValue) {
        commonValidation(hashAlgoValue, outFileValue);

        if (!isValidFileOrDriectoryOrDrive(leftSideValue)) {
            throw new RuntimeException(format("incorrect left side dir/drive/file.xlsx parameter %s", leftSideValue));
        }

        if (centerSideValue != null && !isValidFileOrDriectoryOrDrive(centerSideValue)) {
            throw new RuntimeException(format("incorrect center side dir/drive/file.xlsx parameter %s", centerSideValue));
        }

        if (!isValidFileOrDriectoryOrDrive(rightSideValue)) {
            throw new RuntimeException(format("incorrect right side dir/drive/file.xlsx parameter %s", rightSideValue));
        }
    }

    public static void validateRecompareHash(String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String inFileValue, String outFileValue) {
        validateCompareHash(hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, outFileValue);
        if (!isValidFileExcelName(outFileValue)) {
            throw new RuntimeException("In file excel not correct");
        }
    }

}
