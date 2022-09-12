package com.jibi.concurrent;

import java.util.concurrent.ForkJoinPool;

public class FileOperationPool extends ForkJoinPool {

    public FileOperationPool() {
        super(2);
    }
}
