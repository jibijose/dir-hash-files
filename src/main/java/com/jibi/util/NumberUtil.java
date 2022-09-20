package com.jibi.util;

public class NumberUtil {

    public static String formatCommasInNumber(String stringNumber) {
        StringBuffer stringBuffer = new StringBuffer("");
        for (int i = 0; i < stringNumber.length(); i++) {


            if ((stringNumber.length() - i - 1) % 3 == 0 && (stringNumber.length() - i - 1) != 0) {
                stringBuffer.append(stringNumber.charAt(i) + ",");
            } else {
                stringBuffer.append(stringNumber.charAt(i));
            }
        }
        return stringBuffer.toString();
    }

    public static String formatCommasInNumber(long number) {
        return formatCommasInNumber("" + number);
    }

}
