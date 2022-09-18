package com.jibi.util;

public class SystemUtil {

    public static boolean isUnixSystem() {
        return !isWindowsSystem();
    }

    public static boolean isWindowsSystem() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return true;
        }
        return false;
    }
}
