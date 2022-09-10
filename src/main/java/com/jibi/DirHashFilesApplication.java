package com.jibi;

import static org.apache.commons.lang3.StringUtils.rightPad;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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
        if ("createhash".equals(modeValue)) {
            String inDirValue = cmd.getOptionValue("indir");
            String outFileValue = cmd.getOptionValue("outfile");
            if (StringUtils.isEmpty(inDirValue) || StringUtils.isEmpty(outFileValue)) {
                throw new RuntimeException("incorrect parameters...");
            }
            dirHashFilesApplication.startCreateHash(inDirValue, outFileValue);
        } else if ("comparehash".equals(modeValue)) {
            String leftSideValue = cmd.getOptionValue("leftside");
            String rightSideValue = cmd.getOptionValue("rightside");
            String outFileValue = cmd.getOptionValue("outfile");
            if (StringUtils.isEmpty(leftSideValue) || StringUtils.isEmpty(rightSideValue) || StringUtils.isEmpty(outFileValue)) {
                throw new RuntimeException("incorrect parameters...");
            }
            dirHashFilesApplication.startCompareHash(leftSideValue, rightSideValue, outFileValue);
        }
    }

    private void startCreateHash(String dirValue, String outFileValue) {
        try {
            Collection<FileInfo> listFileInfos = mapDirFiles(dirValue);
            FileInfoExcelWriter fileInfoExcelWriter = new FileInfoExcelWriter(outFileValue);
            fileInfoExcelWriter.writeExcel(listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void startCompareHash(String leftSideValue, String rightSideValue, String outFileValue) {
        try {
            Collection<FileInfo> listFileInfosLeft;
            if (FileUtil.isDriveOrFolder(leftSideValue)) {
                log.info("Left side {} is drive or folder", leftSideValue);
                listFileInfosLeft = mapDirFiles(leftSideValue);
            } else if (FileUtil.isFileInfoExcel(leftSideValue)) {
                log.info("Left side {} is FileInfo excel", leftSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(leftSideValue);
                listFileInfosLeft = fileInfoExcelReader.readExcel();
            } else {
                log.error("Left side {} not correct", leftSideValue);
                throw new RuntimeException(String.format("Left side %s not correct", leftSideValue));
            }

            Collection<FileInfo> listFileInfosRight;
            if (FileUtil.isDriveOrFolder(rightSideValue)) {
                log.info("Right side {} is drive or folder", rightSideValue);
                listFileInfosRight = mapDirFiles(rightSideValue);
            } else if (FileUtil.isFileInfoExcel(rightSideValue)) {
                log.info("Right side {} is FileInfo excel", rightSideValue);
                FileInfoExcelReader fileInfoExcelReader = new FileInfoExcelReader(rightSideValue);
                listFileInfosRight = fileInfoExcelReader.readExcel();
            } else {
                log.error("Right side {} not correct", leftSideValue);
                throw new RuntimeException(String.format("Right side %s not correct", rightSideValue));
            }

            Map<String, HashStatus> hashStatusMap = compareLeftCenterRight(listFileInfosLeft, listFileInfosRight);

            HashStatusExcelWriter hashStatusExcelWriter = new HashStatusExcelWriter(outFileValue);
            hashStatusExcelWriter.writeExcel(hashStatusMap);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Collection<FileInfo> mapDirFiles(String dir) {
        log.debug("************************************************************************************************************************");
        Collection<File> files = getFiles(dir);
        log.info("Got {} files to process", files.size());

        Collection<FileInfo> listFileInfos = new ArrayList<>();
        String dirValuePrefix = (dir + "\\").replaceAll("\\\\", "\\\\\\\\");
        files.parallelStream().forEach(file -> {
            String fileHash = getFileChecksum(file);
            String relativeFilePath = file.toString().replaceFirst(dirValuePrefix, "");
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(relativeFilePath);
            fileInfo.setHash(fileHash);
            fileInfo.setSize(file.length());
            fileInfo.setLastModified(new Date(file.lastModified()));
            listFileInfos.add(fileInfo);
        });

        log.info("Mapped {} file hashes", listFileInfos.size());
        return listFileInfos;
    }

    private long numOfPercentPrints = 1000;
    private double lastBlockPrint = 0;

    private void printHashingStatus(long numTotalFiles, double blockSize, long hashedFiles) {
        if (hashedFiles >= blockSize * lastBlockPrint) {
            double percentCompleted = 100.0 * hashedFiles / numTotalFiles;
            lastBlockPrint = lastBlockPrint + blockSize;
            log.debug("Hashing Percent Completed={}   [{}/{}]  ", String.format("%.2f", percentCompleted), hashedFiles, numTotalFiles);
        }
    }


    private Map<String, HashStatus> compareLeftCenterRight(Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosRight) {
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

    private String getFileChecksum(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //Get file input stream for reading the file content
            FileInputStream fis = new FileInputStream(file);

            //Create byte array to read data in chunks
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            //Read file data and update in message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            ;

            //close the stream; We don't need it now.
            fis.close();

            //Get the hash's bytes
            byte[] bytes = digest.digest();

            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (IOException ioException) {
            return null;
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            return null;
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
