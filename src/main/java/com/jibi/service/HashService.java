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
import static com.jibi.util.FileUtil.isValidDirectoryOrDrive;
import static com.jibi.util.ValidationUtil.validateMergeFiles;
import static com.jibi.util.ValidationUtil.validateCreateHash;
import static com.jibi.util.ValidationUtil.validateRecreateHash;
import static com.jibi.util.ValidationUtil.validateCompareHash;
import static com.jibi.util.ValidationUtil.validateRecompareHash;

import com.jibi.common.Algorithm;
import com.jibi.common.HashOperation;
import com.jibi.concurrent.FileOperationPool;
import com.jibi.concurrent.HashingTaskExecutor;
import com.jibi.concurrent.MappingStatusPrint;
import com.jibi.file.*;
import com.jibi.util.FileUtil;
import com.jibi.vo.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.rightPad;

@Slf4j
public class HashService {

    public void startMerge(boolean passFlag, String hashAlgoValue, String files, String outFileValue) {
        validateMergeFiles(hashAlgoValue, files, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        List<String> listFiles = Arrays.asList(files.split(",", -1));
        Map<String, FileInfoExcelReader> mapFileInfoReaders = Collections.synchronizedMap(new HashMap<>());
        Map<String, HashStatusReader> mapHashStatusReaders = Collections.synchronizedMap(new HashMap<>());
        try {
            listFiles.stream().forEach(filename -> {
                String excelPasswword = ExcelReader.getExcelPassword(filename);
                if (ExcelReader.hasSheet("FileInfo", filename, excelPasswword)) {
                    FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(filename, excelPasswword);
                    mapFileInfoReaders.put(filename, fileInfoExcelReader);
                }
                if (ExcelReader.hasSheet("HashStatus", filename, excelPasswword)) {
                    HashStatusReader hashStatusReader = new HashStatusReader(filename, excelPasswword);
                    mapHashStatusReaders.put(filename, hashStatusReader);
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (listFiles.size() == mapFileInfoReaders.size()) {
            String excelPassword = FileUtil.getUserPasswordHidden(passFlag, outFileValue);
            Collection<FileInfo> listMergedFileInfos = Collections.synchronizedList(new ArrayList<>());
            mapFileInfoReaders.keySet().stream().forEach(filename -> {
                FileInfoExcelReader fileInfoExcelReader = mapFileInfoReaders.get(filename);
                listMergedFileInfos.addAll(fileInfoExcelReader.readExcel(algoSelected));
                if (findDuplicatesFileInfo(listMergedFileInfos).size() > 0) {
                    throw new RuntimeException(String.format("Duplicate file info filenames found in %s with another", filename));
                }
            });
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(excelPassword, algoSelected, listMergedFileInfos);
        } else if (listFiles.size() == mapHashStatusReaders.size()) {
            String excelPassword = FileUtil.getUserPasswordHidden(passFlag, outFileValue);
            Collection<HashStatus> listMergedHashStatus = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger numOfHashStatusTwoFiles = new AtomicInteger(0);
            AtomicInteger numOfHashStatusThreeFiles = new AtomicInteger(0);
            mapHashStatusReaders.keySet().stream().forEach(filename -> {
                HashStatusReader hashStatusReader = mapHashStatusReaders.get(filename);
                Collection<HashStatus> listHashStatusRead = hashStatusReader.readExcel(algoSelected);
                if (listHashStatusRead.isEmpty()) {
                    numOfHashStatusTwoFiles.incrementAndGet();
                    numOfHashStatusThreeFiles.incrementAndGet();
                } else if (listHashStatusRead.stream().findFirst().get() instanceof HashStatusThree) {
                    numOfHashStatusThreeFiles.incrementAndGet();
                } else if (listHashStatusRead.stream().findFirst().get() instanceof HashStatusTwo) {
                    numOfHashStatusTwoFiles.incrementAndGet();
                } else {
                    throw new RuntimeException("Unknown HashStatus object");
                }
                listMergedHashStatus.addAll(hashStatusReader.readExcel(algoSelected));
                if (findDuplicatesHashStatus(listMergedHashStatus).size() > 0) {
                    throw new RuntimeException(String.format("Duplicate hash status filenames found in %s with another", filename));
                }
            });
            if (mapHashStatusReaders.size() == numOfHashStatusTwoFiles.get()) {
                HashStatusTwoExcelWriter hashStatusTwoExcelWriter = new HashStatusTwoExcelWriter(outFileValue);
                Map<String, HashStatusTwo> hashStatusMap = listMergedHashStatus.stream().collect(Collectors.toMap(hashStatus -> hashStatus.getFilename(), hashStatus -> (HashStatusTwo) hashStatus));
                hashStatusTwoExcelWriter.writeExcel(excelPassword, algoSelected, hashStatusMap);
            } else if (mapHashStatusReaders.size() == numOfHashStatusThreeFiles.get()) {
                HashStatusThreeExcelWriter hashStatusThreeExcelWriter = new HashStatusThreeExcelWriter(outFileValue);
                Map<String, HashStatusThree> hashStatusMap = listMergedHashStatus.stream().collect(Collectors.toMap(hashStatus -> hashStatus.getFilename(), hashStatus -> (HashStatusThree) hashStatus));
                hashStatusThreeExcelWriter.writeExcel(excelPassword, algoSelected, hashStatusMap);
            } else {
                throw new RuntimeException("HashStatus two and three files mixed");
            }
        } else {
            log.warn("All files should be either fileinfo or hash status excel files.");
        }
    }

    public void startCreate(boolean passFlag, String hashAlgoValue, String dirValue, String outFileValue) {
        dirValue = FileUtil.adjustDirectoryOrDrive(dirValue);
        validateCreateHash(hashAlgoValue, dirValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = FileUtil.getUserPasswordHidden(passFlag, outFileValue);

        try {
            Collection<FileInfo> listFileInfos = mapDirFiles(algoSelected, dirValue);
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(excelPassword, algoSelected, listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startRecreate(boolean passFlag, String hashAlgoValue, String dirValue, String inFileValue, String outFileValue) {
        dirValue = FileUtil.adjustDirectoryOrDrive(dirValue);
        validateRecreateHash(hashAlgoValue, dirValue, inFileValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = FileUtil.getUserPasswordHidden(passFlag, outFileValue);

        try {
            Collection<FileInfo> listFileInfos = mapDirFiles(algoSelected, dirValue, inFileValue);
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(excelPassword, algoSelected, listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startCompare(boolean passFlag, String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String outFileValue) {
        leftSideValue = FileUtil.adjustDirectoryOrDrive(leftSideValue);
        centerSideValue = FileUtil.adjustDirectoryOrDrive(centerSideValue);
        rightSideValue = FileUtil.adjustDirectoryOrDrive(rightSideValue);
        validateCompareHash(hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = FileUtil.getUserPasswordHidden(passFlag, outFileValue);

        try {
            Collection<FileInfo> listFileInfosLeft = null;
            if (isValidDirectoryOrDrive(leftSideValue)) {
                log.info("Left side {} is drive or folder", leftSideValue);
                listFileInfosLeft = mapDirFiles(algoSelected, leftSideValue);
            } else if (isValidFileExcel(leftSideValue)) {
                log.info("Left side {} is FileInfo excel", leftSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(leftSideValue, ExcelReader.getExcelPassword(leftSideValue));
                listFileInfosLeft = fileInfoExcelReader.readExcel(algoSelected);
            }

            Collection<FileInfo> listFileInfosCenter = null;
            if (centerSideValue != null) {
                if (isValidDirectoryOrDrive(centerSideValue)) {
                    log.info("Center side {} is drive or folder", centerSideValue);
                    listFileInfosCenter = mapDirFiles(algoSelected, centerSideValue);
                } else if (isValidFileExcel(centerSideValue)) {
                    log.info("Center side {} is FileInfo excel", centerSideValue);
                    FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(centerSideValue, ExcelReader.getExcelPassword(centerSideValue));
                    listFileInfosCenter = fileInfoExcelReader.readExcel(algoSelected);
                }
            }

            Collection<FileInfo> listFileInfosRight = null;
            if (isValidDirectoryOrDrive(rightSideValue)) {
                log.info("Right side {} is drive or folder", rightSideValue);
                listFileInfosRight = mapDirFiles(algoSelected, rightSideValue);
            } else if (isValidFileExcel(rightSideValue)) {
                log.info("Right side {} is FileInfo excel", rightSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(rightSideValue, ExcelReader.getExcelPassword(rightSideValue));
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
        leftSideValue = FileUtil.adjustDirectoryOrDrive(leftSideValue);
        centerSideValue = FileUtil.adjustDirectoryOrDrive(centerSideValue);
        rightSideValue = FileUtil.adjustDirectoryOrDrive(rightSideValue);
        validateRecompareHash(hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, inFileValue, outFileValue);
        Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
        String excelPassword = FileUtil.getUserPasswordHidden(passFlag, outFileValue);

        HashStatusReader hashStatusReader = new HashStatusReader(inFileValue, ExcelReader.getExcelPassword(inFileValue));
        Collection<HashStatus> listExistingHashStatus = hashStatusReader.readExcel(algoSelected);
        Map<String, HashStatus> mapExistingHashStatus = listExistingHashStatus.stream()
                .collect(Collectors.toMap(hashStatus -> hashStatus.getFilename(), hashStatus -> hashStatus));
        log.info("Existing hashstatus files {}", mapExistingHashStatus.size());

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
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(leftSideValue, ExcelReader.getExcelPassword(leftSideValue));
                listFileInfosLeft = fileInfoExcelReader.readExcel(algoSelected);
            }

            Collection<FileInfo> listFileInfosCenter = null;
            if (centerSideValue != null) {
                if (isValidDirectoryOrDrive(centerSideValue)) {
                    log.info("Center side {} is drive or folder", centerSideValue);
                    listFileInfosCenter = mapDirFiles(algoSelected, centerSideValue, mapExistingCenterOneSide);
                } else if (isValidFileExcel(centerSideValue)) {
                    log.info("Center side {} is FileInfo excel", centerSideValue);
                    FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(centerSideValue, ExcelReader.getExcelPassword(centerSideValue));
                    listFileInfosCenter = fileInfoExcelReader.readExcel(algoSelected);
                }
            }

            Collection<FileInfo> listFileInfosRight = null;
            if (isValidDirectoryOrDrive(rightSideValue)) {
                log.info("Right side {} is drive or folder", rightSideValue);
                listFileInfosRight = mapDirFiles(algoSelected, rightSideValue, mapExistingRightOneSide);
            } else if (isValidFileExcel(rightSideValue)) {
                log.info("Right side {} is FileInfo excel", rightSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(rightSideValue, ExcelReader.getExcelPassword(rightSideValue));
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
        Map<String, FileInfo> mapExistingFileInfos = Collections.EMPTY_MAP;
        return mapDirFilesInternal(algoSelected, dir, mapExistingFileInfos);
    }

    public Collection<FileInfo> mapDirFiles(Algorithm algoSelected, String dir, String inFileInfo) {
        FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(inFileInfo, ExcelReader.getExcelPassword(inFileInfo));
        Collection<FileInfo> listExistingFileInfos = fileInfoExcelReader.readExcel(algoSelected);
        Map<String, FileInfo> mapExistingFileInfos = listExistingFileInfos.stream().collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(), fileInfo -> fileInfo));
        long existingFileSize = mapExistingFileInfos.keySet().stream().mapToLong(filename -> mapExistingFileInfos.get(filename).getSize()).sum();
        log.info("Input fileinfo has {} files with total size {}", formatCommasInNumber(mapExistingFileInfos.size()), formatCommasInNumber(existingFileSize));
        return mapDirFilesInternal(algoSelected, dir, mapExistingFileInfos);
    }

    public Collection<FileInfo> mapDirFiles(Algorithm algoSelected, String dir, Map<String, OneSide> mapExistingOneSide) {
        Map<String, FileInfo> mapExistingFileInfos = mapExistingOneSide.keySet().stream()
                .collect(Collectors.toMap(filename -> filename, filename -> {
                    OneSide oneSide = mapExistingOneSide.get(filename);
                    return new FileInfo(filename, oneSide.getSize(), oneSide.getHash(), oneSide.getLastModified());
                }));
        long existingFileSize = mapExistingFileInfos.keySet().stream().mapToLong(filename -> mapExistingFileInfos.get(filename).getSize()).sum();
        log.info("Input fileinfo has {} files with total size {}", formatCommasInNumber(mapExistingFileInfos.size()), formatCommasInNumber(existingFileSize));
        return mapDirFilesInternal(algoSelected, dir, mapExistingFileInfos);
    }

    private Collection<FileInfo> mapDirFilesInternal(Algorithm algoSelected, String dir, Map<String, FileInfo> mapExistingFileInfos) {
        log.debug("************************************************************************************************************************");
        Collection<File> files = FileUtil.getFiles(dir);
        long totalFiles = files.size();
        long totalFileSize = files.stream().mapToLong(File::length).sum();
        log.info("Got {} files of total size {} to process", formatCommasInNumber(totalFiles), formatCommasInNumber(totalFileSize));
        AtomicLong processedFiles = new AtomicLong(0);
        AtomicLong processedFileSize = new AtomicLong(0);

        final Phaser phaser = new Phaser();
        log.debug("MAIN Registered = {}, Arrived = {}, Unarrived = {}", phaser.getRegisteredParties(), phaser.getArrivedParties(), phaser.getUnarrivedParties());
        phaser.register();
        log.debug("MAIN Registered = {}, Arrived = {}, Unarrived = {}", phaser.getRegisteredParties(), phaser.getArrivedParties(), phaser.getUnarrivedParties());
        MappingStatusPrint mappingStatusPrint = new MappingStatusPrint(totalFiles, totalFileSize);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(mappingStatusPrint);

        Collection<FileInfo> listFileInfos = Collections.synchronizedList(new ArrayList<>());
        String dirValuePrefix = FileUtil.getDirValuePrefix(dir);

        FileOperationPool fileOperationPool = new FileOperationPool();
        HashOperation hashOperation = new HashOperation(algoSelected);

        HashingTaskExecutor.getFiles(fileOperationPool, phaser, dir, dirValuePrefix, mappingStatusPrint, processedFiles, processedFileSize,
                mapExistingFileInfos, listFileInfos, hashOperation);
        try {
            phaser.arriveAndAwaitAdvance();
            executorService.shutdownNow();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            log.warn("Interuppted phaser", interruptedException);
        }

        log.info("Mapped {} file hashes out of {} files", listFileInfos.size(), totalFiles);
        return listFileInfos;
    }

    private Map<String, HashStatusTwo> compareLeftRight(Algorithm algoSelected, Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosRight) {
        int expectedMaxSizeMap = listFileInfosLeft.size() + listFileInfosRight.size();
        Map<String, HashStatusTwo> hashStatusMap = new HashMap<>(expectedMaxSizeMap);

        listFileInfosLeft.stream().forEach(fileInfoLeft -> {
            hashStatusMap.put(fileInfoLeft.getFilename(), HashStatusTwo.buildWithLeftHash(fileInfoLeft.getFilename(), MISSING, fileInfoLeft));
        });
        Map<String, FileInfo> hashFileInfosLeft = listFileInfosLeft.stream().collect(Collectors.toMap(fileInfoLeft -> fileInfoLeft.getFilename(), fileInfoLeft -> fileInfoLeft));
        listFileInfosRight.stream().forEach(fileInfoRight -> {
            if (hashFileInfosLeft.containsKey(fileInfoRight.getFilename())) {
                FileInfo fileInfoLeft = hashFileInfosLeft.get(fileInfoRight.getFilename());
                HashStatusTwo hashStatusTwo = hashStatusMap.get(fileInfoRight.getFilename());
                hashStatusTwo.getRight().setHash(fileInfoRight.getHash());
                hashStatusTwo.getRight().setSize(fileInfoRight.getSize());
                hashStatusTwo.getRight().setLastModified(fileInfoRight.getLastModified());

                if (algoSelected == null) {
                    if (fileInfoLeft.getSize() == fileInfoRight.getSize() && fileInfoLeft.getLastModified().compareTo(fileInfoRight.getLastModified()) == 0) {
                        hashStatusTwo.setStatus(MATCH);
                    } else {
                        hashStatusTwo.setStatus(MISMATCH);
                    }
                } else {
                    if (fileInfoLeft.getHash().equals(fileInfoRight.getHash()) && fileInfoLeft.getSize() == fileInfoRight.getSize()) {
                        hashStatusTwo.setStatus(MATCH);
                    } else {
                        hashStatusTwo.setStatus(MISMATCH);
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

    private Set<String> findDuplicatesFileInfo(Collection<FileInfo> collection) {
        Collection<String> collectionFilenames = collection.stream()
                .map(fileInfo -> fileInfo.getFilename())
                .collect(Collectors.toList());
        Set<String> duplicates = new LinkedHashSet<>();
        Set<String> uniques = new HashSet<>();
        for (String str : collectionFilenames) {
            if (!uniques.add(str)) {
                duplicates.add(str);
            }
        }
        return duplicates;
    }

    private Set<String> findDuplicatesHashStatus(Collection<HashStatus> collection) {
        Collection<String> collectionFilenames = collection.stream()
                .map(hashStatus -> hashStatus.getFilename())
                .collect(Collectors.toList());
        Set<String> duplicates = new LinkedHashSet<>();
        Set<String> uniques = new HashSet<>();
        for (String str : collectionFilenames) {
            if (!uniques.add(str)) {
                duplicates.add(str);
            }
        }
        return duplicates;
    }

    private void updateMapNewElement(Map<String, HashStatusThree> hashStatusMap, String filename) {
        if (!hashStatusMap.containsKey(filename)) {
            HashStatusThree hashStatusThree = new HashStatusThree();
            hashStatusThree.setFilename(filename);
            hashStatusMap.put(filename, hashStatusThree);
        }
    }
}
