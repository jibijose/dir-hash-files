package com.jibi.concurrent;

import com.jibi.common.HashOperation;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class HashingTaskExecutor {

    public static List<File> getFiles(FileOperationPool fileOperationPool, Phaser phaser, String directory, String dirValuePrefix,
                                      MappingStatusPrint mappingStatusPrint, AtomicLong processedFiles, AtomicLong processedFileSize,
                                      Map<String, FileInfo> mapExistingFileInfos, Collection<FileInfo> listFileInfos, HashOperation hashOperation) {
        List<File> fileList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        for (File element : files) {
            if (element.isDirectory() && !Files.isSymbolicLink(element.toPath())) {
                if (!FileUtil.isTempDirectory(element.getPath())) {
                    fileList.addAll(getFiles(fileOperationPool, phaser, element.getPath(), dirValuePrefix, mappingStatusPrint, processedFiles, processedFileSize,
                            mapExistingFileInfos, listFileInfos, hashOperation));
                }
            } else if (Files.isRegularFile(element.toPath()) && !Files.isSymbolicLink(element.toPath())) {
                if (!FileUtil.isTempFile(element.getPath())) {
                    HashingTask hashingTask = new HashingTask(phaser, element, dirValuePrefix, mappingStatusPrint, processedFiles, processedFileSize,
                            mapExistingFileInfos, listFileInfos, hashOperation);
                    phaser.register();
                    fileOperationPool.submit(hashingTask);
                }
            } else {
                log.trace("Not Dir/File {} ", element);
            }
        }
        return fileList;
    }

}
