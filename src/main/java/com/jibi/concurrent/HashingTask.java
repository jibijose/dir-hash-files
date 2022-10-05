package com.jibi.concurrent;

import com.jibi.common.HashOperation;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Phaser;

@Slf4j
public class HashingTask extends Thread {

    private Phaser phaser;
    private File[] files;
    private String dirValuePrefix;
    private Map<String, FileInfo> mapExistingFileInfos;
    private Collection<FileInfo> listFileInfos;
    private HashOperation hashOperation;

    public HashingTask(Phaser phaser, File[] files, String dirValuePrefix, Map<String, FileInfo> mapExistingFileInfos, Collection<FileInfo> listFileInfos, HashOperation hashOperation) {
        this.phaser = phaser;
        this.files = files;
        this.dirValuePrefix = dirValuePrefix;
        this.mapExistingFileInfos = mapExistingFileInfos;
        this.listFileInfos = listFileInfos;
        this.hashOperation = hashOperation;
    }

    public void run() {
        for (File file : files) {
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
            MappingStatusPrint.PROCESSED_FILE_COUNT.incrementAndGet();
            MappingStatusPrint.PROCESSED_FILE_SIZE.addAndGet(fileInfo.getSize());
            phaser.arriveAndDeregister();
        }
    }
}
