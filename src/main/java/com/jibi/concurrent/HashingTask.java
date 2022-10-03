package com.jibi.concurrent;

import com.jibi.common.HashOperation;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class HashingTask extends Thread {

    private Phaser phaser;
    private File file;
    private String dirValuePrefix;
    private MappingStatusPrint mappingStatusPrint;
    private AtomicLong processedFiles;
    private AtomicLong processedFileSize;
    private Map<String, FileInfo> mapExistingFileInfos;
    private Collection<FileInfo> listFileInfos;
    private HashOperation hashOperation;

    public HashingTask(Phaser phaser, File file, String dirValuePrefix, MappingStatusPrint mappingStatusPrint, AtomicLong processedFiles, AtomicLong processedFileSize,
                       Map<String, FileInfo> mapExistingFileInfos, Collection<FileInfo> listFileInfos, HashOperation hashOperation) {
        this.phaser = phaser;
        this.file = file;
        this.dirValuePrefix = dirValuePrefix;
        this.mappingStatusPrint = mappingStatusPrint;
        this.processedFiles = processedFiles;
        this.processedFileSize = processedFileSize;
        this.mapExistingFileInfos = mapExistingFileInfos;
        this.listFileInfos = listFileInfos;
        this.hashOperation = hashOperation;
    }

    public void run() {
        String relativeFilePath = FileUtil.getFileRelativePath(file, dirValuePrefix);
        FileInfo fileInfo;
        if (mapExistingFileInfos.containsKey(relativeFilePath)
                && file.length() == mapExistingFileInfos.get(relativeFilePath).getSize()
                && file.lastModified() == mapExistingFileInfos.get(relativeFilePath).getLastModified().getTime()) {
            fileInfo = mapExistingFileInfos.get(relativeFilePath);
            listFileInfos.add(fileInfo);
            log.trace("Copied hash of file {}", relativeFilePath);
        } else {
            String fileHash = hashOperation.getFileChecksum(file);
            fileInfo = new FileInfo(relativeFilePath, file.length(), fileHash, new Date(file.lastModified()));
            listFileInfos.add(fileInfo);
            log.trace("Hashed file {}", relativeFilePath);
        }
        mappingStatusPrint.setProcessedFiles(processedFiles.incrementAndGet());
        mappingStatusPrint.setProcessedFileSize(processedFileSize.addAndGet(fileInfo.getSize()));
        phaser.arriveAndDeregister();
    }
}