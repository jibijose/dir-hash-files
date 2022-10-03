package com.jibi.concurrent;

import static java.lang.String.format;
import static com.jibi.util.NumberUtil.formatCommasInNumber;

import com.jibi.util.DateUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class MappingStatusPrint implements Runnable {
    private long totalFiles;
    private long totalFileSize;
    @Setter
    private long processedFiles;
    @Setter
    private long processedFileSize;

    private int sleepIntervalMillis = 1000;
    private int numOfPrints = 10;

    private Date dateStartTime;
    private int digitsTotalFiles;
    private int digitsTotalFileSize;

    DecimalFormat decimalFormat = new DecimalFormat("00.00");

    public MappingStatusPrint(long totalFiles, long totalFileSize) {
        this.totalFiles = totalFiles;
        this.totalFileSize = totalFileSize;
        if (totalFiles > 0) {
            long number = totalFiles;
            for (; number != 0; number /= 10, ++digitsTotalFiles) {
            }
        }
        if (totalFileSize > 0) {
            long number = totalFileSize;
            for (; number != 0; number /= 10, ++digitsTotalFileSize) {
            }
        }
        decimalFormat.setRoundingMode(RoundingMode.DOWN);

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
        dateStartTime = new Date();
        boolean exitNow = false;
        while (true) {
            try {
                if (totalFiles > 0 && totalFileSize > 0 && processedFiles > 0 && processedFileSize > 0) {
                    String percentageCompletedByCount = format("%1$6s", decimalFormat.format(100.0 * processedFiles / totalFiles));
                    String percentageCompletedBySize = format("%1$6s", decimalFormat.format(100.0 * processedFileSize / totalFileSize));

                    Date dateNow = new Date();
                    long timeTakenSoFar = dateNow.getTime() - dateStartTime.getTime();
                    long expectedTimeNeededByFiles = timeTakenSoFar * totalFiles / processedFiles;
                    long expectedTimeNeededByFileSizes = timeTakenSoFar * totalFileSize / processedFileSize;
                    long expectedMaxTimeLeftSeconds = ((expectedTimeNeededByFileSizes > expectedTimeNeededByFiles) ? expectedTimeNeededByFileSizes : expectedTimeNeededByFiles) / 1000;

                    long hours = 0;
                    long minutes = 0;
                    long seconds = 0;
                    long countDownSeconds = 0;

                    countDownSeconds = timeTakenSoFar / 1000;
                    if (countDownSeconds / 60 / 60 > 0) {
                        hours = countDownSeconds / 60 / 60;
                        countDownSeconds = countDownSeconds - (hours * 60 * 60);
                    }
                    if (countDownSeconds / 60 > 0) {
                        minutes = countDownSeconds / 60;
                        countDownSeconds = countDownSeconds - (minutes * 60);
                    }
                    seconds = countDownSeconds;
                    String formattedTimeSpent = format("%02d:%02d:%02d", hours, minutes, seconds);

                    hours = 0;
                    minutes = 0;
                    seconds = 0;
                    countDownSeconds = expectedMaxTimeLeftSeconds - timeTakenSoFar / 1000;
                    if (countDownSeconds / 60 / 60 > 0) {
                        hours = countDownSeconds / 60 / 60;
                        countDownSeconds = countDownSeconds - (hours * 60 * 60);
                    }
                    if (countDownSeconds / 60 > 0) {
                        minutes = countDownSeconds / 60;
                        countDownSeconds = countDownSeconds - (minutes * 60);
                    }
                    seconds = countDownSeconds;
                    String formattedTimeLeft = format("%02d:%02d:%02d", hours, minutes, seconds);

                    String formattedDateEtc = DateUtil.displayTimeFormatted(new Date(dateStartTime.getTime() + expectedMaxTimeLeftSeconds * 1000));

                    log.info("Progress: File {}% [{}/{}]  Size {}% [{}/{}]   Spent [{}]   Left [{}]   ETC [{}]",
                            percentageCompletedByCount, formatCommasInNumber(format("%0" + digitsTotalFiles + "d", processedFiles)), formatCommasInNumber(totalFiles),
                            percentageCompletedBySize, formatCommasInNumber(format("%0" + digitsTotalFileSize + "d", processedFileSize)), formatCommasInNumber(totalFileSize),
                            formattedTimeSpent, formattedTimeLeft, formattedDateEtc);
                }
                if (exitNow) {
                    break;
                }
                if (!exitNow) {
                    Thread.sleep(sleepIntervalMillis);
                }
            } catch (InterruptedException interruptedException) {
                exitNow = true;
            }
        }
    }
}
