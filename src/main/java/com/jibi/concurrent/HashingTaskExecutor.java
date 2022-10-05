package com.jibi.concurrent;

import com.jibi.common.HashOperation;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Phaser;

@Slf4j
public class HashingTaskExecutor {

    private FileOperationPool fileOperationPool;
    private String dirValuePrefix;

    public HashingTaskExecutor(String dirValuePrefix) {
        fileOperationPool = new FileOperationPool();
        this.dirValuePrefix = dirValuePrefix;
    }

    public List<File> executeFileHashing(Phaser phaser, String directory, Map<String, FileInfo> mapExistingFileInfos, Collection<FileInfo> listFileInfos, HashOperation hashOperation) {
        List<File> fileList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        for (File file : files) {
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                if (!FileUtil.isTempDirectory(file.getPath(), true)) {
                    fileList.addAll(executeFileHashing(phaser, file.getPath(), mapExistingFileInfos, listFileInfos, hashOperation));
                }
            } else if (Files.isRegularFile(file.toPath()) && !Files.isSymbolicLink(file.toPath())) {
                if (!FileUtil.isTempFile(file.getPath(), true)) {
                    HashingTask hashingTask = new HashingTask(phaser, file, dirValuePrefix, mapExistingFileInfos, listFileInfos, hashOperation);
                    phaser.register();
                    fileOperationPool.submit(hashingTask);
                }
            } else {
                log.trace("Not Dir/File {} ", file);
            }
        }
        return fileList;
    }

}
