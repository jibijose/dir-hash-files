package com.jibi.service;

import static com.jibi.util.NumberUtil.formatCommasInNumber;

import static com.jibi.util.FileUtil.MATCH;
import static com.jibi.util.FileUtil.MISMATCH;
import static com.jibi.util.FileUtil.MISSING;
import static com.jibi.util.FileUtil.NEWFILE;
import static com.jibi.util.FileUtil.INSYNC;
import static com.jibi.util.FileUtil.NOTSYNCED;
import static com.jibi.util.FileUtil.PAD_MARK;
import static com.jibi.util.FileUtil.isValidFileExcel;
import static com.jibi.util.FileUtil.isValidFileExcelName;
import static com.jibi.util.FileUtil.isValidDirectoryOrDrive;
import static com.jibi.util.FileUtil.isValidFileOrDriectoryOrDrive;

import com.jibi.common.Algorithm;
import com.jibi.common.HashOperation;
import com.jibi.concurrent.FileOperationPool;
import com.jibi.concurrent.MappingStatusPrint;
import com.jibi.file.*;
import com.jibi.util.FileUtil;
import com.jibi.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.rightPad;

@Slf4j
public class HashService {

    public void startCreate(boolean passFlag, String hashAlgoValue, String dirValue, String outFileValue) {
        validateCreateHash(hashAlgoValue, dirValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = null;
        if (passFlag) {
            excelPassword = FileUtil.getUserInputFilePassword(String.format("password for %s", outFileValue));
        }
        try {
            Collection<FileInfo> listFileInfos = mapDirFiles(algoSelected, dirValue);
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(excelPassword, algoSelected, listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startRecreate(boolean passFlag, String hashAlgoValue, String dirValue, String inFileValue, String outFileValue) {
        validateRecreateHash(inFileValue, inFileValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = null;
        if (passFlag) {
            excelPassword = FileUtil.getUserInputFilePassword(String.format("password for %s", outFileValue));
        }
        try {
            Collection<FileInfo> listFileInfos = mapDirFiles(algoSelected, dirValue, inFileValue);
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(excelPassword, algoSelected, listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startCompare(boolean passFlag, String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String outFileValue) {
        validateCompareHash(hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = null;
        if (passFlag) {
            excelPassword = FileUtil.getUserInputFilePassword(String.format("password for %s", outFileValue));
        }
        try {
            Collection<FileInfo> listFileInfosLeft = null;
            if (isValidDirectoryOrDrive(leftSideValue)) {
                log.info("Left side {} is drive or folder", leftSideValue);
                listFileInfosLeft = mapDirFiles(algoSelected, leftSideValue);
            } else if (isValidFileExcel(leftSideValue)) {
                log.info("Left side {} is FileInfo excel", leftSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(leftSideValue);
                listFileInfosLeft = fileInfoExcelReader.readExcel(algoSelected);
            }

            Collection<FileInfo> listFileInfosCenter = null;
            if (centerSideValue != null) {
                if (isValidDirectoryOrDrive(centerSideValue)) {
                    log.info("Center side {} is drive or folder", centerSideValue);
                    listFileInfosCenter = mapDirFiles(algoSelected, centerSideValue);
                } else if (isValidFileExcel(centerSideValue)) {
                    log.info("Center side {} is FileInfo excel", centerSideValue);
                    FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(centerSideValue);
                    listFileInfosCenter = fileInfoExcelReader.readExcel(algoSelected);
                }
            }

            Collection<FileInfo> listFileInfosRight = null;
            if (isValidDirectoryOrDrive(rightSideValue)) {
                log.info("Right side {} is drive or folder", rightSideValue);
                listFileInfosRight = mapDirFiles(algoSelected, rightSideValue);
            } else if (isValidFileExcel(rightSideValue)) {
                log.info("Right side {} is FileInfo excel", rightSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(rightSideValue);
                listFileInfosRight = fileInfoExcelReader.readExcel(algoSelected);
            }

            if (listFileInfosCenter == null) {
                Map<String, HashStatusTwo> hashStatusMap;
                hashStatusMap = compareLeftRight(algoSelected, listFileInfosLeft, listFileInfosRight);
                HashStatusTwoExcelWriter hashStatusTwoExcelWriter = new HashStatusTwoExcelWriter(outFileValue);
                hashStatusTwoExcelWriter.writeExcel(excelPassword, algoSelected, hashStatusMap);
            } else {
                Map<String, HashStatusThree> hashStatusMap;
                hashStatusMap = compareLeftCenterRight(algoSelected, listFileInfosLeft, listFileInfosCenter, listFileInfosRight);
                HashStatusThreeExcelWriter hashStatusThreeExcelWriter = new HashStatusThreeExcelWriter(outFileValue);
                hashStatusThreeExcelWriter.writeExcel(excelPassword, algoSelected, hashStatusMap);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startRecompare(boolean passFlag, String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String inFileValue, String outFileValue) {
        validateRecompareHash(hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, inFileValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = null;
        if (passFlag) {
            excelPassword = FileUtil.getUserInputFilePassword(String.format("password for %s", outFileValue));
        }

        HashStatusReader hashStatusReader = new HashStatusReader(inFileValue);
        Collection<HashStatus> listExistingHashStatus = hashStatusReader.readExcel(algoSelected);
        Map<String, HashStatus> mapExistingHashStatus = listExistingHashStatus.stream()
                .collect(Collectors.toMap(hashStatus -> hashStatus.getFilename(), hashStatus -> hashStatus));

        Map<String, OneSide> mapExistingLeftOneSide = mapExistingHashStatus.keySet().stream()
                .collect(Collectors.toMap(filename -> filename, filename -> {
                    HashStatus hashStatus = mapExistingHashStatus.get(filename);
                    if (hashStatus instanceof HashStatusThree) {
                        return ((HashStatusThree) hashStatus).getLeft();
                    } else {
                        return ((HashStatusTwo) hashStatus).getLeft();
                    }
                }));
        Map<String, OneSide> mapExistingCenterOneSide = mapExistingHashStatus.keySet().stream()
                .collect(Collectors.toMap(filename -> filename, filename -> {
                    HashStatus hashStatus = mapExistingHashStatus.get(filename);
                    if (hashStatus instanceof HashStatusThree) {
                        return ((HashStatusThree) hashStatus).getCenter();
                    } else {
                        return new OneSide();
                    }
                }));
        Map<String, OneSide> mapExistingRightOneSide = mapExistingHashStatus.keySet().stream()
                .collect(Collectors.toMap(filename -> filename, filename -> {
                    HashStatus hashStatus = mapExistingHashStatus.get(filename);
                    if (hashStatus instanceof HashStatusThree) {
                        return ((HashStatusThree) hashStatus).getRight();
                    } else {
                        return ((HashStatusTwo) hashStatus).getRight();
                    }
                }));

        try {
            Collection<FileInfo> listFileInfosLeft = null;
            if (isValidDirectoryOrDrive(leftSideValue)) {
                log.info("Left side {} is drive or folder", leftSideValue);
                listFileInfosLeft = mapDirFiles(algoSelected, leftSideValue, mapExistingLeftOneSide);
            } else if (isValidFileExcel(leftSideValue)) {
                log.info("Left side {} is FileInfo excel", leftSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(leftSideValue);
                listFileInfosLeft = fileInfoExcelReader.readExcel(algoSelected);
            }

            Collection<FileInfo> listFileInfosCenter = null;
            if (centerSideValue != null) {
                if (isValidDirectoryOrDrive(centerSideValue)) {
                    log.info("Center side {} is drive or folder", centerSideValue);
                    listFileInfosCenter = mapDirFiles(algoSelected, centerSideValue, mapExistingCenterOneSide);
                } else if (isValidFileExcel(centerSideValue)) {
                    log.info("Center side {} is FileInfo excel", centerSideValue);
                    FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(centerSideValue);
                    listFileInfosCenter = fileInfoExcelReader.readExcel(algoSelected);
                }
            }

            Collection<FileInfo> listFileInfosRight = null;
            if (isValidDirectoryOrDrive(rightSideValue)) {
                log.info("Right side {} is drive or folder", rightSideValue);
                listFileInfosRight = mapDirFiles(algoSelected, rightSideValue, mapExistingRightOneSide);
            } else if (isValidFileExcel(rightSideValue)) {
                log.info("Right side {} is FileInfo excel", rightSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(rightSideValue);
                listFileInfosRight = fileInfoExcelReader.readExcel(algoSelected);
            }
            
            if (listFileInfosCenter == null) {
                Map<String, HashStatusTwo> hashStatusMap;
                hashStatusMap = compareLeftRight(algoSelected, listFileInfosLeft, listFileInfosRight);
                HashStatusTwoExcelWriter hashStatusTwoExcelWriter = new HashStatusTwoExcelWriter(outFileValue);
                hashStatusTwoExcelWriter.writeExcel(excelPassword, algoSelected, hashStatusMap);
            } else {
                Map<String, HashStatusThree> hashStatusMap;
                hashStatusMap = compareLeftCenterRight(algoSelected, listFileInfosLeft, listFileInfosCenter, listFileInfosRight);
                HashStatusThreeExcelWriter hashStatusThreeExcelWriter = new HashStatusThreeExcelWriter(outFileValue);
                hashStatusThreeExcelWriter.writeExcel(excelPassword, algoSelected, hashStatusMap);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Collection<FileInfo> mapDirFiles(Algorithm algoSelected, String dir) {
        log.debug("************************************************************************************************************************");
        Collection<File> files = FileUtil.getFiles(dir);
        long totalFiles = files.size();
        long totalFileSize = files.stream().mapToLong(File::length).sum();
        log.info("Got {} files of total size {} to process", formatCommasInNumber(totalFiles), formatCommasInNumber(totalFileSize));
        AtomicLong processedFiles = new AtomicLong(0);
        AtomicLong processedFileSize = new AtomicLong(0);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        MappingStatusPrint mappingStatusPrint = new MappingStatusPrint(countDownLatch, totalFiles, totalFileSize);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(mappingStatusPrint);

        Collection<FileInfo> listFileInfos = Collections.synchronizedList(new ArrayList<>());
        String dirValuePrefix = FileUtil.getDirValuePrefix(dir);

        try {
            FileOperationPool fileOperationPool = new FileOperationPool();
            HashOperation hashOperation = new HashOperation(algoSelected);
            fileOperationPool.submit(
                    () -> files.parallelStream().forEach(file -> {
                        String fileHash = hashOperation.getFileChecksum(file);
                        String relativeFilePath = FileUtil.getFileRelativePath(file, dirValuePrefix);
                        FileInfo fileInfo = new FileInfo(relativeFilePath, file.length(), fileHash, new Date(file.lastModified()));
                        listFileInfos.add(fileInfo);
                        mappingStatusPrint.setProcessedFiles(processedFiles.incrementAndGet());
                        mappingStatusPrint.setProcessedFileSize(processedFileSize.addAndGet(fileInfo.getSize()));
                        log.trace("Hashed file {}", fileInfo.getFilename());
                    })).get();
        } catch (InterruptedException interruptedException) {
            log.warn("Interrupted exception", interruptedException);
        } catch (ExecutionException executionException) {
            log.warn("Execution exception", executionException);
        }

        try {
            countDownLatch.await();
            executorService.shutdownNow();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            log.warn("Interuppted countdownwatch", interruptedException);
        }

        log.info("Mapped {} file hashes out of {} files", listFileInfos.size(), totalFiles);
        return listFileInfos;
    }

    public Collection<FileInfo> mapDirFiles(Algorithm algoSelected, String dir, String inFileInfo) {
        FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(inFileInfo);
        Collection<FileInfo> listExistingFileInfos = fileInfoExcelReader.readExcel(algoSelected);
        Map<String, FileInfo> mapExistingFileInfos = listExistingFileInfos.stream().collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(), fileInfo -> fileInfo));

        log.debug("************************************************************************************************************************");
        Collection<File> files = FileUtil.getFiles(dir);
        long totalFiles = files.size();
        long totalFileSize = files.stream().mapToLong(File::length).sum();
        log.info("Got {} files of total size {} to process", formatCommasInNumber(totalFiles), formatCommasInNumber(totalFileSize));
        AtomicLong processedFiles = new AtomicLong(0);
        AtomicLong processedFileSize = new AtomicLong(0);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        MappingStatusPrint mappingStatusPrint = new MappingStatusPrint(countDownLatch, totalFiles, totalFileSize);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(mappingStatusPrint);

        Collection<FileInfo> listFileInfos = Collections.synchronizedList(new ArrayList<>());
        String dirValuePrefix = (dir + "\\").replaceAll("\\\\", "\\\\\\\\");

        try {
            FileOperationPool fileOperationPool = new FileOperationPool();
            HashOperation hashOperation = new HashOperation(algoSelected);
            fileOperationPool.submit(
                    () -> files.parallelStream().forEach(file -> {
                        String relativeFilePath = file.toString().replaceFirst(dirValuePrefix, "");
                        FileInfo fileInfo;
                        if (mapExistingFileInfos.containsKey(relativeFilePath)) {
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
                    })).get();
        } catch (InterruptedException interruptedException) {
            log.warn("Interrupted exception", interruptedException);
        } catch (ExecutionException executionException) {
            log.warn("Execution exception", executionException);
        }

        try {
            countDownLatch.await();
            executorService.shutdownNow();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            log.warn("Interuppted countdownwatch", interruptedException);
        }

        log.info("Mapped {} file hashes out of {} files", listFileInfos.size(), totalFiles);
        return listFileInfos;
    }

    private Collection<FileInfo> mapDirFiles(Algorithm algoSelected, String dir, Map<String, OneSide> mapExistingOneSide) {
        Map<String, FileInfo> mapExistingFileInfos = mapExistingOneSide.keySet().stream()
                .collect(Collectors.toMap(filename -> filename, filename -> {
                    OneSide oneSide = mapExistingOneSide.get(filename);
                    return new FileInfo(filename, oneSide.getSize(), oneSide.getHash(), oneSide.getLastModified());
                }));

        log.debug("************************************************************************************************************************");
        Collection<File> files = FileUtil.getFiles(dir);
        long totalFiles = files.size();
        long totalFileSize = files.stream().mapToLong(File::length).sum();
        log.info("Got {} files of total size {} to process", formatCommasInNumber(totalFiles), formatCommasInNumber(totalFileSize));
        AtomicLong processedFiles = new AtomicLong(0);
        AtomicLong processedFileSize = new AtomicLong(0);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        MappingStatusPrint mappingStatusPrint = new MappingStatusPrint(countDownLatch, totalFiles, totalFileSize);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(mappingStatusPrint);

        Collection<FileInfo> listFileInfos = Collections.synchronizedList(new ArrayList<>());
        String dirValuePrefix = (dir + "\\").replaceAll("\\\\", "\\\\\\\\");

        try {
            FileOperationPool fileOperationPool = new FileOperationPool();
            HashOperation hashOperation = new HashOperation(algoSelected);
            fileOperationPool.submit(
                    () -> files.parallelStream().forEach(file -> {
                        String relativeFilePath = file.toString().replaceFirst(dirValuePrefix, "");
                        FileInfo fileInfo;
                        if (mapExistingFileInfos.containsKey(relativeFilePath)) {
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
                    })).get();
        } catch (InterruptedException interruptedException) {
            log.warn("Interrupted exception", interruptedException);
        } catch (ExecutionException executionException) {
            log.warn("Execution exception", executionException);
        }

        try {
            countDownLatch.await();
            executorService.shutdownNow();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            log.warn("Interuppted countdownwatch", interruptedException);
        }

        log.info("Mapped {} file hashes out of {} files", listFileInfos.size(), totalFiles);
        return listFileInfos;
    }

    private Map<String, HashStatusTwo> compareLeftRight(Algorithm algoSelected, Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosRight) {
        Map<String, HashStatusTwo> hashStatusMap = new HashMap<>();

        listFileInfosLeft.stream().forEach(fileInfoLeft -> {
            hashStatusMap.put(fileInfoLeft.getFilename(), HashStatusTwo.buildWithLeftHash(fileInfoLeft.getFilename(), MISSING, fileInfoLeft));
        });
        listFileInfosRight.stream().forEach(fileInfoRight -> {
            if (listFileInfosLeft.contains(fileInfoRight)) {
                FileInfo fileInfoLeft = listFileInfosLeft.stream().filter(fileInfo -> fileInfo.equals(fileInfoRight)).findFirst().get();
                hashStatusMap.get(fileInfoRight.getFilename()).getRight().setHash(fileInfoRight.getHash());
                hashStatusMap.get(fileInfoRight.getFilename()).getRight().setSize(fileInfoRight.getSize());
                hashStatusMap.get(fileInfoRight.getFilename()).getRight().setLastModified(fileInfoRight.getLastModified());

                if (algoSelected == null) {
                    if (fileInfoLeft.getSize() == fileInfoRight.getSize() && fileInfoLeft.getLastModified().compareTo(fileInfoRight.getLastModified()) == 0) {
                        hashStatusMap.get(fileInfoRight.getFilename()).setStatus(MATCH);
                    } else {
                        hashStatusMap.get(fileInfoRight.getFilename()).setStatus(MISMATCH);
                    }
                } else {
                    if (fileInfoLeft.getHash().equals(fileInfoRight.getHash()) && fileInfoLeft.getSize() == fileInfoRight.getSize()) {
                        hashStatusMap.get(fileInfoRight.getFilename()).setStatus(MATCH);
                    } else {
                        hashStatusMap.get(fileInfoRight.getFilename()).setStatus(MISMATCH);
                    }
                }
            } else {
                hashStatusMap.put(fileInfoRight.getFilename(), HashStatusTwo.buildWithRightHash(fileInfoRight.getFilename(), NEWFILE, fileInfoRight));
            }
        });
        log.debug("************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatusTwo::isNotMatched).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}   ->   {}", hashStatus.getLeft(), hashStatus.getRight());
        });
        log.debug("************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatusTwo::isMissingFile).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}", hashStatus.getLeft());
        });
        log.debug("************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatusTwo::isNewFile).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}", hashStatus.getRight());
        });
        log.debug("************************************************************************************************************************");
        long matchedFiles = hashStatusMap.values().stream().filter(HashStatusTwo::isMatched).count();
        long notMatchedFiles = hashStatusMap.values().stream().filter(HashStatusTwo::isNotMatched).count();
        long missingFiles = hashStatusMap.values().stream().filter(HashStatusTwo::isMissingFile).count();
        long newFiles = hashStatusMap.values().stream().filter(HashStatusTwo::isNewFile).count();
        log.info("Matched = {}, Not matched = {}, Missing files = {}, New files = {}", matchedFiles, notMatchedFiles, missingFiles, newFiles);
        log.debug("************************************************************************************************************************");

        return hashStatusMap;
    }

    private Map<String, HashStatusThree> compareLeftCenterRight(Algorithm algoSelected, Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosCenter,
                                                                Collection<FileInfo> listFileInfosRight) {
        Map<String, OneSide> leftOneSide = listFileInfosLeft.stream()
                .collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(),
                        fileInfo -> new OneSide("", fileInfo.getHash(), fileInfo.getSize(), fileInfo.getLastModified())));
        Map<String, OneSide> centerOneSide = listFileInfosCenter.stream()
                .collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(),
                        fileInfo -> new OneSide("", fileInfo.getHash(), fileInfo.getSize(), fileInfo.getLastModified())));
        Map<String, OneSide> rightOneSide = listFileInfosRight.stream()
                .collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(),
                        fileInfo -> new OneSide("", fileInfo.getHash(), fileInfo.getSize(), fileInfo.getLastModified())));
        log.info("HashStatus size left={} center={} right={}", leftOneSide.size(), centerOneSide.size(), rightOneSide.size());

        Map<String, HashStatusThree> hashStatusMap = new HashMap<>();
        leftOneSide.keySet().stream()
                .forEach(filename -> {
                    updateMapNewElement(hashStatusMap, filename);
                    hashStatusMap.get(filename).setLeft(leftOneSide.get(filename));
                });
        centerOneSide.keySet().stream()
                .forEach(filename -> {
                    updateMapNewElement(hashStatusMap, filename);
                    hashStatusMap.get(filename).setCenter(centerOneSide.get(filename));
                });
        rightOneSide.keySet().stream()
                .forEach(filename -> {
                    updateMapNewElement(hashStatusMap, filename);
                    hashStatusMap.get(filename).setRight(rightOneSide.get(filename));
                });
        log.trace("HashStatusMap size {}", hashStatusMap);
        hashStatusMap.keySet().stream()
                .map(filename -> hashStatusMap.get(filename))
                .forEach(hashStatusThree -> {
                    if (hashStatusThree.getLeft().exists()) {
                        log.trace("With left side file {}", hashStatusThree.getFilename());
                        OneSide leftSide = hashStatusThree.getLeft();
                        OneSide centerSide = hashStatusThree.getCenter();
                        OneSide rightSide = hashStatusThree.getRight();
                        if (centerSide.exists() && rightSide.exists()) {
                            if (algoSelected == null) {
                                if (leftSide.compareWithoutHash(centerSide) && leftSide.compareWithoutHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(INSYNC, MATCH, MATCH, MATCH);
                                } else if (leftSide.compareWithoutHash(centerSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MATCH, MISMATCH);
                                } else if (leftSide.compareWithoutHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MISMATCH, MATCH);
                                } else if (centerSide.compareWithoutHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MATCH, MATCH);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISMATCH, MISMATCH);
                                }
                            } else {
                                if (leftSide.compareWithHash(centerSide) && leftSide.compareWithHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(INSYNC, MATCH, MATCH, MATCH);
                                } else if (leftSide.compareWithHash(centerSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MATCH, MISMATCH);
                                } else if (leftSide.compareWithHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MISMATCH, MATCH);
                                } else if (centerSide.compareWithHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MATCH, MATCH);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISMATCH, MISMATCH);
                                }
                            }
                        } else if (centerSide.exists() && !rightSide.exists()) {
                            if (algoSelected == null) {
                                if (leftSide.compareWithoutHash(centerSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MATCH, MISSING);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISMATCH, MISSING);
                                }
                            } else {
                                if (leftSide.compareWithHash(centerSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MATCH, MISSING);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISMATCH, MISSING);
                                }
                            }
                        } else if (!centerSide.exists() && rightSide.exists()) {
                            if (algoSelected == null) {
                                if (leftSide.compareWithoutHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MISSING, MATCH);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISSING, MISMATCH);
                                }
                            } else {
                                if (leftSide.compareWithHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MISSING, MATCH);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISSING, MISMATCH);
                                }
                            }
                        } else if (!centerSide.exists() && !rightSide.exists()) {
                            hashStatusThree.updateSideStatus(NOTSYNCED, NEWFILE, MISSING, MISSING);
                        }
                    } else {
                        log.trace("Without left side file {}", hashStatusThree.getFilename());
                        OneSide leftSide = hashStatusThree.getLeft();
                        OneSide centerSide = hashStatusThree.getCenter();
                        OneSide rightSide = hashStatusThree.getRight();
                        if (centerSide.exists() && rightSide.exists()) {
                            if (algoSelected == null) {
                                if (centerSide.compareWithoutHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, MATCH, MATCH);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, MISMATCH, MISMATCH);
                                }
                            } else {
                                if (centerSide.compareWithHash(rightSide)) {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, MATCH, MATCH);
                                } else {
                                    hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, MISMATCH, MISMATCH);
                                }
                            }
                        } else if (centerSide.exists() && !rightSide.exists()) {
                            hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, NEWFILE, MISSING);
                        } else if (!centerSide.exists() && rightSide.exists()) {
                            hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, MISSING, NEWFILE);
                        }
                    }
                });

        return hashStatusMap;
    }

    private void updateMapNewElement(Map<String, HashStatusThree> hashStatusMap, String filename) {
        if (!hashStatusMap.containsKey(filename)) {
            HashStatusThree hashStatusThree = new HashStatusThree();
            hashStatusThree.setFilename(filename);
            hashStatusMap.put(filename, hashStatusThree);
        }
    }

    private void commonValidation(String hashAlgoValue, String outFileValue) {
        if (!StringUtils.isEmpty(hashAlgoValue) && !Algorithm.isValidAlgo(hashAlgoValue)) {
            throw new RuntimeException(format("incorrect hash algo parameter %s Supported algorithms [MD2, MD5, SHA, SHA224, SHA256, SHA384, SHA512]", hashAlgoValue));
        }
        if (!isValidFileExcelName(outFileValue)) {
            throw new RuntimeException("Out file excel not correct");
        }
        if (!FileUtil.ifFileWritable(outFileValue)) {
            throw new RuntimeException("Out file excel not writable");
        }
    }

    private void validateCreateHash(String hashAlgoValue, String inDirValue, String outFileValue) {
        commonValidation(hashAlgoValue, outFileValue);
        if (!isValidDirectoryOrDrive(inDirValue)) {
            throw new RuntimeException("In dir/drive value not correct");
        }
    }

    private void validateRecreateHash(String inDirValue, String inFileValue, String outFileValue) {

    }

    private void validateCompareHash(String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String outFileValue) {
        commonValidation(hashAlgoValue, outFileValue);

        if (!isValidFileOrDriectoryOrDrive(leftSideValue)) {
            throw new RuntimeException(format("incorrect left side dir/drive/file.xlsx parameter %s", leftSideValue));
        }

        if (centerSideValue != null && !isValidFileOrDriectoryOrDrive(centerSideValue)) {
            throw new RuntimeException(format("incorrect center side dir/drive/file.xlsx parameter %s", centerSideValue));
        }

        if (!isValidFileOrDriectoryOrDrive(rightSideValue)) {
            throw new RuntimeException(format("incorrect right side dir/drive/file.xlsx parameter %s", rightSideValue));
        }
    }

    private void validateRecompareHash(String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String inFileValue, String outFileValue) {
        commonValidation(hashAlgoValue, outFileValue);
        if (!isValidFileOrDriectoryOrDrive(leftSideValue)) {
            throw new RuntimeException(format("incorrect left side dir/drive/file.xlsx parameter %s", leftSideValue));
        }

        if (centerSideValue != null && !isValidFileOrDriectoryOrDrive(centerSideValue)) {
            throw new RuntimeException(format("incorrect center side dir/drive/file.xlsx parameter %s", centerSideValue));
        }

        if (!isValidFileOrDriectoryOrDrive(rightSideValue)) {
            throw new RuntimeException(format("incorrect right side dir/drive/file.xlsx parameter %s", rightSideValue));
        }
    }

}
