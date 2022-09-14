package com.jibi.service;

import static com.jibi.util.FileUtil.MATCH;
import static com.jibi.util.FileUtil.MISMATCH;
import static com.jibi.util.FileUtil.MISSING;
import static com.jibi.util.FileUtil.NEWFILE;
import static com.jibi.util.FileUtil.INSYNC;
import static com.jibi.util.FileUtil.NOTSYNCED;
import static com.jibi.util.FileUtil.PAD_MARK;

import com.jibi.common.Algorithm;
import com.jibi.common.HashOperation;
import com.jibi.concurrent.FileOperationPool;
import com.jibi.concurrent.MappingStatusPrint;
import com.jibi.file.FileInfoExcelReader;
import com.jibi.file.FileInfoExcelWriter;
import com.jibi.file.HashStatusThreeExcelWriter;
import com.jibi.file.HashStatusTwoExcelWriter;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import com.jibi.vo.HashStatusThree;
import com.jibi.vo.HashStatusTwo;
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

    private void commonValidation(String hashAlgoValue, String outFileValue) {
        if (!StringUtils.isEmpty(hashAlgoValue) && !Algorithm.isValidAlgo(hashAlgoValue)) {
            throw new RuntimeException(format("incorrect hash algo parameter %s Supported algorithms [MD2, MD5, SHA, SHA224, SHA256, SHA384, SHA512]", hashAlgoValue));
        }
        if (StringUtils.isEmpty(outFileValue)) {
            throw new RuntimeException(format("incorrect out file.xlsx parameter %s", outFileValue));
        }
    }

    private void validateCreateHash(String hashAlgoValue, String inDirValue, String outFileValue) {
        commonValidation(hashAlgoValue, outFileValue);
        if (StringUtils.isEmpty(inDirValue)) {
            throw new RuntimeException(format("incorrect in dir/drive parameter %s", inDirValue));
        }
    }

    private void validateCompareHash(String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String outFileValue) {
        commonValidation(hashAlgoValue, outFileValue);
        if (!FileUtil.validDirDriveFileValue(leftSideValue)) {
            throw new RuntimeException(format("incorrect left side dir/drive/file.xlsx parameter %s", leftSideValue));
        }

        if (centerSideValue != null && !FileUtil.validDirDriveFileValue(centerSideValue)) {
            throw new RuntimeException(format("incorrect center side dir/drive/file.xlsx parameter %s", centerSideValue));
        }

        if (!FileUtil.validDirDriveFileValue(rightSideValue)) {
            throw new RuntimeException(format("incorrect right side dir/drive/file.xlsx parameter %s", rightSideValue));
        }
    }

    public void startCreateHash(String hashAlgoValue, String dirValue, String outFileValue) {
        validateCreateHash(hashAlgoValue, dirValue, outFileValue);
        try {
            Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
            Collection<FileInfo> listFileInfos = mapDirFiles(algoSelected, dirValue);
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(algoSelected, listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void startCompareHash(String hashAlgoValue, String leftSideValue, String centerSideValue, String rightSideValue, String outFileValue) {
        validateCompareHash(hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, outFileValue);
        try {
            Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
            Collection<FileInfo> listFileInfosLeft = null;
            if (FileUtil.isDriveOrFolder(leftSideValue)) {
                log.info("Left side {} is drive or folder", leftSideValue);
                listFileInfosLeft = mapDirFiles(algoSelected, leftSideValue);
            } else if (FileUtil.isFileInfoExcel(leftSideValue)) {
                log.info("Left side {} is FileInfo excel", leftSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(leftSideValue);
                listFileInfosLeft = fileInfoExcelReader.readExcel(algoSelected);
            }

            Collection<FileInfo> listFileInfosCenter = null;
            if (centerSideValue != null) {
                if (FileUtil.isDriveOrFolder(centerSideValue)) {
                    log.info("Center side {} is drive or folder", centerSideValue);
                    listFileInfosCenter = mapDirFiles(algoSelected, centerSideValue);
                } else if (FileUtil.isFileInfoExcel(centerSideValue)) {
                    log.info("Center side {} is FileInfo excel", centerSideValue);
                    FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(centerSideValue);
                    listFileInfosCenter = fileInfoExcelReader.readExcel(algoSelected);
                }
            }

            Collection<FileInfo> listFileInfosRight = null;
            if (FileUtil.isDriveOrFolder(rightSideValue)) {
                log.info("Right side {} is drive or folder", rightSideValue);
                listFileInfosRight = mapDirFiles(algoSelected, rightSideValue);
            } else if (FileUtil.isFileInfoExcel(rightSideValue)) {
                log.info("Right side {} is FileInfo excel", rightSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(rightSideValue);
                listFileInfosRight = fileInfoExcelReader.readExcel(algoSelected);
            }


            if (listFileInfosCenter == null) {
                Map<String, HashStatusTwo> hashStatusMap;
                hashStatusMap = compareLeftRight(listFileInfosLeft, listFileInfosRight);
                HashStatusTwoExcelWriter hashStatusTwoExcelWriter = new HashStatusTwoExcelWriter(outFileValue);
                hashStatusTwoExcelWriter.writeExcel(algoSelected, hashStatusMap);
            } else {
                Map<String, HashStatusThree> hashStatusMap;
                hashStatusMap = compareLeftCenterRight(listFileInfosLeft, listFileInfosCenter, listFileInfosRight);
                HashStatusThreeExcelWriter hashStatusThreeExcelWriter = new HashStatusThreeExcelWriter(outFileValue);
                hashStatusThreeExcelWriter.writeExcel(algoSelected, hashStatusMap);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Collection<FileInfo> mapDirFiles(Algorithm algoSelected, String dir) {
        log.debug("************************************************************************************************************************");
        Collection<File> files = getFiles(dir);
        long totalFiles = files.size();
        long totalFileSize = files.stream().mapToLong(File::length).sum();
        log.info("Got {} files of total size {} to process", totalFiles, totalFileSize);
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
                        String fileHash = hashOperation.getFileChecksum(file);
                        String relativeFilePath = file.toString().replaceFirst(dirValuePrefix, "");
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

    private Map<String, HashStatusTwo> compareLeftRight(Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosRight) {
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
                if (fileInfoLeft.getHash().equals(fileInfoRight.getHash()) && fileInfoLeft.getSize() == fileInfoRight.getSize()
                        && fileInfoLeft.getLastModified().compareTo(fileInfoRight.getLastModified()) == 0) {
                    hashStatusMap.get(fileInfoRight.getFilename()).setStatus(MATCH);
                } else {
                    hashStatusMap.get(fileInfoRight.getFilename()).setStatus(MISMATCH);
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

    private Map<String, HashStatusThree> compareLeftCenterRight(Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosCenter,
                                                                Collection<FileInfo> listFileInfosRight) {
        Map<String, HashStatusThree.OneSide> leftOneSide = listFileInfosLeft.stream()
                .collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(),
                        fileInfo -> new HashStatusThree.OneSide("", fileInfo.getHash(), fileInfo.getSize(), fileInfo.getLastModified())));
        Map<String, HashStatusThree.OneSide> centerOneSide = listFileInfosCenter.stream()
                .collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(),
                        fileInfo -> new HashStatusThree.OneSide("", fileInfo.getHash(), fileInfo.getSize(), fileInfo.getLastModified())));
        Map<String, HashStatusThree.OneSide> rightOneSide = listFileInfosRight.stream()
                .collect(Collectors.toMap(fileInfo -> fileInfo.getFilename(),
                        fileInfo -> new HashStatusThree.OneSide("", fileInfo.getHash(), fileInfo.getSize(), fileInfo.getLastModified())));
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
                        HashStatusThree.OneSide leftSide = hashStatusThree.getLeft();
                        HashStatusThree.OneSide centerSide = hashStatusThree.getCenter();
                        HashStatusThree.OneSide rightSide = hashStatusThree.getRight();
                        if (centerSide.exists() && rightSide.exists()) {
                            if (leftSide.compare(centerSide) && leftSide.compare(rightSide)) {
                                hashStatusThree.updateSideStatus(INSYNC, MATCH, MATCH, MATCH);
                            } else if (leftSide.compare(centerSide)) {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MATCH, MISMATCH);
                            } else if (leftSide.compare(rightSide)) {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MISMATCH, MATCH);
                            } else if (centerSide.compare(rightSide)) {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MATCH, MATCH);
                            } else {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISMATCH, MISMATCH);
                            }
                        } else if (centerSide.exists() && !rightSide.exists()) {
                            if (leftSide.compare(centerSide)) {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MATCH, MISSING);
                            } else {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISMATCH, MISSING);
                            }
                        } else if (!centerSide.exists() && rightSide.exists()) {
                            if (leftSide.compare(rightSide)) {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MATCH, MISSING, MATCH);
                            } else {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MISMATCH, MISSING, MISMATCH);
                            }
                        } else if (!centerSide.exists() && !rightSide.exists()) {
                            hashStatusThree.updateSideStatus(NOTSYNCED, NEWFILE, MISSING, MISSING);
                        }
                    } else {
                        log.trace("Without left side file {}", hashStatusThree.getFilename());
                        HashStatusThree.OneSide leftSide = hashStatusThree.getLeft();
                        HashStatusThree.OneSide centerSide = hashStatusThree.getCenter();
                        HashStatusThree.OneSide rightSide = hashStatusThree.getRight();
                        if (centerSide.exists() && rightSide.exists()) {
                            if (centerSide.compare(rightSide)) {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, MATCH, MATCH);
                            } else {
                                hashStatusThree.updateSideStatus(NOTSYNCED, MISSING, MISMATCH, MISMATCH);
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

    private static List<File> getFiles(final String directory) {
        if (directory == null) {
            return Collections.EMPTY_LIST;
        }
        List<File> fileList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        for (File element : files) {
            if (element.isDirectory()) {
                fileList.addAll(getFiles(element.getPath()));
            } else {
                fileList.add(element);
            }
        }
        return fileList;
    }

}
