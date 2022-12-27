package com.jibi.common;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Constants {

    public static final String CORRUPTED = "CORRUPTED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String LOCKED = "LOCKED";

    public static final int PHASER_THRESHOLD = 2;

    public static AtomicInteger BLOCKED_READS = new AtomicInteger(0);
    public static AtomicBoolean SKIP_ALL = new AtomicBoolean(false);
}
