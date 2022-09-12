package com.jibi.concurrent;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class MappingStatusPrint implements Runnable {

    private CountDownLatch countDownLatch;
    private long totalFiles;
    private long totalFileSize;
    @Setter
    private long processedFiles;
    @Setter
    private long processedFileSize;

    private int sleepIntervalMillis = 1000;
    private int numOfPrints = 10;

    DecimalFormat decimalFormat = new DecimalFormat("00.00");

    public MappingStatusPrint(CountDownLatch countDownLatch, long totalFiles, long totalFileSize) {
        this.countDownLatch = countDownLatch;
        this.totalFiles = totalFiles;
        this.totalFileSize = totalFileSize;

        int GBs = (int) (totalFileSize / 1024 / 1024 / 1024);

        this.sleepIntervalMillis = GBs * 1000 / numOfPrints;
        if (this.sleepIntervalMillis < 1000) {
            this.sleepIntervalMillis = 100;
        }
        if (this.sleepIntervalMillis > 10000) {
            this.sleepIntervalMillis = 10000;
        }
        log.info("Mapping status print interval {} milli seconds", this.sleepIntervalMillis);
    }

    @Override
    public void run() {
        while (true) {
            try {
                String percentageCompletedByCount = decimalFormat.format(100.0 * processedFiles / totalFiles);
                String percentageCompletedBySize = decimalFormat.format(100.0 * processedFileSize / totalFileSize);
                log.info("Progress Count {}% [{}/{}]  Size {}% [{}/{}]", percentageCompletedByCount, processedFiles, totalFiles, percentageCompletedBySize, processedFileSize, totalFileSize);
                if (totalFiles <= processedFiles) {
                    break;
                }
                if (totalFiles > processedFiles) {
                    Thread.sleep(sleepIntervalMillis);
                }
            } catch (InterruptedException interruptedException) {
                log.warn("Interuppted", interruptedException);
            }
        }
        countDownLatch.countDown();
    }
}
