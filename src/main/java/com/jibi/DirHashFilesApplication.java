package com.jibi;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.rightPad;

import com.jibi.common.Algorithm;
import com.jibi.common.HashOperation;
import com.jibi.concurrent.FileOperationPool;
import com.jibi.concurrent.MappingStatusPrint;
import com.jibi.file.FileInfoExcelReader;
import com.jibi.file.FileInfoExcelWriter;
import com.jibi.file.HashStatusExcelWriter;
import com.jibi.util.DateUtil;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import com.jibi.vo.HashStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Slf4j
public class DirHashFilesApplication {

    private static int PAD_MARK = 14;
    private static int PAD_HASH = 66;

    private static int FILE_PAD_HASH = 64;
    private static int FILE_PAD_SIZE = 15;
    private static int FILE_PAD_DATE = 36;

    public static void main(String[] args) {
        DirHashFilesApplication dirHashFilesApplication = new DirHashFilesApplication();
        Options options = new Options();

        Option mode = new Option("m", "mode", true, "Operation mode");
        mode.setRequired(true);
        options.addOption(mode);

        Option inDir = new Option("i", "indir", true, "In drive/dir");
        inDir.setRequired(false);
        options.addOption(inDir);

        Option hashAlgo = new Option("h", "hashalgo", true, "Hash algorithm");
        hashAlgo.setRequired(false);
        options.addOption(hashAlgo);

        Option outFile = new Option("o", "outfile", true, "Hash output file");
        outFile.setRequired(false);
        options.addOption(outFile);

        Option leftSide = new Option("l", "leftside", true, "Left side");
        leftSide.setRequired(false);
        options.addOption(leftSide);

        Option centerSide = new Option("c", "centerside", true, "Center side");
        centerSide.setRequired(false);
        options.addOption(centerSide);

        Option rightSide = new Option("r", "rightside", true, "Right side");
        rightSide.setRequired(false);
        options.addOption(rightSide);

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException parseException) {
            log.error("Parseexception", parseException);
            formatter.printHelp("Java directory hasher", options);
            System.exit(1);
            return;
        }

        String modeValue = cmd.getOptionValue("mode");
        String hashAlgoValue = cmd.getOptionValue("hashalgo");
        if (!StringUtils.isEmpty(hashAlgoValue) && !Algorithm.isValidAlgo(hashAlgoValue)) {
            throw new RuntimeException(format("incorrect hash algo parameter %s Supported algorithms [MD2, MD5, SHA, SHA224, SHA256, SHA384, SHA512]", hashAlgoValue));
        }
        String outFileValue = cmd.getOptionValue("outfile");
        if (StringUtils.isEmpty(outFileValue)) {
            throw new RuntimeException(format("incorrect out file.xlsx parameter %s", outFileValue));
        }

        if ("createhash".equals(modeValue)) {
            String inDirValue = cmd.getOptionValue("indir");
            if (StringUtils.isEmpty(inDirValue)) {
                throw new RuntimeException(format("incorrect in dir/drive parameter %s", inDirValue));
            }
            dirHashFilesApplication.startCreateHash(hashAlgoValue, inDirValue, outFileValue);
        } else if ("comparehash".equals(modeValue)) {
            String leftSideValue = cmd.getOptionValue("leftside");
            if (FileUtil.validDirDriveFileValue(leftSideValue)) {
                throw new RuntimeException(format("incorrect left side dir/drive/file.xlsx parameter %s", leftSideValue));
            }

            String rightSideValue = cmd.getOptionValue("rightside");
            if (FileUtil.validDirDriveFileValue(rightSideValue)) {
                throw new RuntimeException(format("incorrect right side dir/drive/file.xlsx parameter %s", rightSideValue));
            }
            dirHashFilesApplication.startCompareHash(hashAlgoValue, leftSideValue, rightSideValue, outFileValue);
        }
    }

    private void startCreateHash(String hashAlgoValue, String dirValue, String outFileValue) {
        try {
            Algorithm algoSelected = Algorithm.getAlgo(hashAlgoValue);
            Collection<FileInfo> listFileInfos = mapDirFiles(algoSelected, dirValue);
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(algoSelected, listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void startCompareHash(String hashAlgoValue, String leftSideValue, String rightSideValue, String outFileValue) {
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

            Collection<FileInfo> listFileInfosRight = null;
            if (FileUtil.isDriveOrFolder(rightSideValue)) {
                log.info("Right side {} is drive or folder", rightSideValue);
                listFileInfosRight = mapDirFiles(algoSelected, rightSideValue);
            } else if (FileUtil.isFileInfoExcel(rightSideValue)) {
                log.info("Right side {} is FileInfo excel", rightSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(rightSideValue);
                listFileInfosRight = fileInfoExcelReader.readExcel(algoSelected);
            }

            Map<String, HashStatus> hashStatusMap = compareLeftCenterRight(algoSelected, listFileInfosLeft, listFileInfosRight);

            HashStatusExcelWriter hashStatusExcelWriter = new HashStatusExcelWriter(outFileValue);
            hashStatusExcelWriter.writeExcel(algoSelected, hashStatusMap);
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

    private Map<String, HashStatus> compareLeftCenterRight(Algorithm algoSelected, Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosRight) {
        Map<String, HashStatus> hashStatusMap = new HashMap<>();

        listFileInfosLeft.stream().forEach(fileInfoLeft -> {
            hashStatusMap.put(fileInfoLeft.getFilename(), HashStatus.buildWithLeftHash(fileInfoLeft.getFilename(), "Missing File", fileInfoLeft));
        });
        listFileInfosRight.stream().forEach(fileInfoRight -> {
            if (listFileInfosLeft.contains(fileInfoRight)) {
                FileInfo fileInfoLeft = listFileInfosLeft.stream().filter(fileInfo -> fileInfo.equals(fileInfoRight)).findFirst().get();
                hashStatusMap.get(fileInfoRight.getFilename()).getRight().setHash(fileInfoRight.getHash());
                hashStatusMap.get(fileInfoRight.getFilename()).getRight().setSize(fileInfoRight.getSize());
                hashStatusMap.get(fileInfoRight.getFilename()).getRight().setLastModified(fileInfoRight.getLastModified());
                if (fileInfoLeft.getHash().equals(fileInfoRight.getHash()) && fileInfoLeft.getSize() == fileInfoRight.getSize()
                        && fileInfoLeft.getLastModified().compareTo(fileInfoRight.getLastModified()) == 0) {
                    hashStatusMap.get(fileInfoRight.getFilename()).setStatus("Matched");
                } else {
                    hashStatusMap.get(fileInfoRight.getFilename()).setStatus("Not Matched");
                }
            } else {
                hashStatusMap.put(fileInfoRight.getFilename(), HashStatus.buildWithRightHash(fileInfoRight.getFilename(), "New File", fileInfoRight));
            }
        });
        log.debug("************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isNotMatched).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}   ->   {}", hashStatus.getLeft(), hashStatus.getRight());
        });
        log.debug("************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isMissingFile).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}", hashStatus.getLeft());
        });
        log.debug("************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isNewFile).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}", hashStatus.getRight());
        });
        log.debug("************************************************************************************************************************");
        long matchedFiles = hashStatusMap.values().stream().filter(HashStatus::isMatched).count();
        long notMatchedFiles = hashStatusMap.values().stream().filter(HashStatus::isNotMatched).count();
        long missingFiles = hashStatusMap.values().stream().filter(HashStatus::isMissingFile).count();
        long newFiles = hashStatusMap.values().stream().filter(HashStatus::isNewFile).count();
        log.info("Matched = {}, Not matched = {}, Missing files = {}, New files = {}", matchedFiles, notMatchedFiles, missingFiles, newFiles);
        log.debug("************************************************************************************************************************");

        return hashStatusMap;
    }


    public Collection<FileInfo> readSignatureFile(String filePath) {
        Collection<FileInfo> listFileInfos = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.filter(line -> !line.trim().equals("")).forEach(line -> {
                String[] keyValuePair = line.split(" +", 3);
                if (keyValuePair.length == 3) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setHash(keyValuePair[0]);
                    fileInfo.setSize(Long.parseLong(keyValuePair[1]));
                    String[] keyValuePairInner = keyValuePair[2].split("   ", 2);
                    fileInfo.setLastModified(DateUtil.parse(keyValuePairInner[0]));
                    fileInfo.setFilename(keyValuePairInner[1]);
                    listFileInfos.add(fileInfo);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listFileInfos;
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
