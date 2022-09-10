package com.jibi;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.rightPad;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public class DirHashFilesApplication {

    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss SSS zzz";
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

        Option dir = new Option("d", "dir", true, "Directory");
        dir.setRequired(false);
        options.addOption(dir);

        Option outfile = new Option("o", "outfile", true, "Hash output file");
        outfile.setRequired(false);
        options.addOption(outfile);

        Option infile = new Option("i", "infile", true, "Hash input file");
        infile.setRequired(false);
        options.addOption(infile);

        Option dirLeft = new Option("dl", "dirleft", true, "Directory left");
        dirLeft.setRequired(false);
        options.addOption(dirLeft);

        Option dirRight = new Option("dr", "dirright", true, "Directory right");
        dirRight.setRequired(false);
        options.addOption(dirRight);

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
            String dirValue = cmd.getOptionValue("dir");
            String outFileValue = cmd.getOptionValue("outfile");
            if (StringUtils.isEmpty(dirValue) || StringUtils.isEmpty(outFileValue)) {
                throw new RuntimeException("incorrect parameters...");
            }
            dirHashFilesApplication.startCreateHash(dirValue, outFileValue);
        } else if ("checkhash".equals(modeValue)) {
            String dirValue = cmd.getOptionValue("dir");
            String inFileValue = cmd.getOptionValue("infile");
            if (StringUtils.isEmpty(dirValue) || StringUtils.isEmpty(inFileValue)) {
                throw new RuntimeException("incorrect parameters...");
            }
            dirHashFilesApplication.startCheckHash(dirValue, inFileValue);
        } else if ("comparehash".equals(modeValue)) {
            String dirLeftValue = cmd.getOptionValue("dirleft");
            String dirRightValue = cmd.getOptionValue("dirright");
            if (StringUtils.isEmpty(dirLeftValue) || StringUtils.isEmpty(dirRightValue)) {
                throw new RuntimeException("incorrect parameters...");
            }
            dirHashFilesApplication.startCompareHash(dirLeftValue, dirRightValue);
        }
    }

    private void startCreateHash(String dirValue, String outFileValue) {
        try {
            Collection<FileInfo> listFileInfos = mapDirFiles(dirValue);
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

            Path path = Path.of(outFileValue);
            log.debug("**************************************************************************************************************************************************************************");
            log.info("Output file path {}", path.toFile().getAbsolutePath());
            Files.write(path, () -> listFileInfos.stream().<CharSequence>map(e -> format("%1$" + FILE_PAD_HASH + "s",
                    e.getHash()) + format("%1$" + FILE_PAD_SIZE + "s", e.getSize()) + format("%1$" + FILE_PAD_DATE + "s",
                    formatter.format(e.getLastModified())) + "   " + e.getFilename()).iterator());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void startCheckHash(String dirValue, String inFileValue) {
        try {
            Collection<FileInfo> listFileInfosSignature = readSignatureFile(inFileValue);
            Collection<FileInfo> listFileInfos = mapDirFiles(dirValue);

            compareAndReportLeftRight(listFileInfosSignature, listFileInfos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void startCompareHash(String dirLeftValue, String dirRightValue) {
        try {
            Collection<FileInfo> listFileInfosLeft = mapDirFiles(dirLeftValue);
            Collection<FileInfo> listFileInfosRight = mapDirFiles(dirRightValue);

            compareAndReportLeftRight(listFileInfosLeft, listFileInfosRight);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Collection<FileInfo> mapDirFiles(String dir) {
        log.debug("**************************************************************************************************************************************************************************");
        Collection<File> files = getFiles(dir);

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


    private void compareAndReportLeftRight(Collection<FileInfo> listFileInfosLeft, Collection<FileInfo> listFileInfosRight) {
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
        log.debug("**************************************************************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isNotMatched).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}   ->   {}", hashStatus.getLeft(), hashStatus.getRight());
        });
        log.debug("**************************************************************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isMissingFile).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}", hashStatus.getLeft());
        });
        log.debug("**************************************************************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isNewFile).forEach(hashStatus -> {
            log.debug("{} {}", rightPad(hashStatus.getStatus(), PAD_MARK), hashStatus.getFilename());
            log.debug("{}", hashStatus.getRight());
        });
        log.debug("**************************************************************************************************************************************************************************");
        long matchedFiles = hashStatusMap.values().stream().filter(HashStatus::isMatched).count();
        long notMatchedFiles = hashStatusMap.values().stream().filter(HashStatus::isNotMatched).count();
        long missingFiles = hashStatusMap.values().stream().filter(HashStatus::isMissingFile).count();
        long newFiles = hashStatusMap.values().stream().filter(HashStatus::isNewFile).count();
        log.info("Matched = {}, Not matched = {}, Missing files = {}, New files = {}", matchedFiles, notMatchedFiles, missingFiles, newFiles);
        log.debug("**************************************************************************************************************************************************************************");

        createExcelReport(hashStatusMap);
    }


    public Collection<FileInfo> readSignatureFile(String filePath) {
        Collection<FileInfo> listFileInfos = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.filter(line -> !line.trim().equals("")).forEach(line -> {
                try {
                    String[] keyValuePair = line.split(" +", 3);
                    if (keyValuePair.length == 3) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setHash(keyValuePair[0]);
                        fileInfo.setSize(Long.parseLong(keyValuePair[1]));
                        String[] keyValuePairInner = keyValuePair[2].split("   ", 2);
                        fileInfo.setLastModified(new SimpleDateFormat(DATE_FORMAT).parse(keyValuePairInner[0]));
                        fileInfo.setFilename(keyValuePairInner[1]);
                        listFileInfos.add(fileInfo);
                    }
                } catch (java.text.ParseException parseException) {
                    log.warn("Parsing error [{}]", line, parseException);
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

    private void createExcelReport(Map<String, HashStatus> hashStatusMap) {
        try {
            var fileStream = new FileOutputStream("jjtrial.xlsx");
            var workbook = new XSSFWorkbook();
            var sheet = workbook.createSheet("Test People Sheet");
            sheet.setColumnWidth(0, 16 * 256);
            sheet.setColumnWidth(1, 70 * 256);
            sheet.setColumnWidth(2, 16 * 256);
            sheet.setColumnWidth(3, 32 * 256);
            sheet.setColumnWidth(4, 70 * 256);
            sheet.setColumnWidth(5, 16 * 256);
            sheet.setColumnWidth(6, 32 * 256);
            sheet.setColumnWidth(7, 200 * 256);

            // Create a header row describing what the columns mean
            CellStyle topRowStyle = workbook.createCellStyle();
            var font = workbook.createFont();
            font.setBold(true);
            topRowStyle.setFont(font);
            //topRowStyle.setFillForegroundColor((short) 10000);
            //topRowStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd-MMM-yyyy"));

            XSSFRow headerRow = sheet.createRow(0);
            addStringCells(headerRow, List.of("Status", "Left-Hash", "Left-Size", "Left-Modified", "Right-Hash", "Right-Size", "Right-Modified", "filename"), topRowStyle);

            AtomicInteger rowIndex = new AtomicInteger(1);
            hashStatusMap.keySet().stream().forEach(hashStatus -> {
                XSSFRow dataRow = sheet.createRow(rowIndex.getAndIncrement());
                addDataCells(dataRow, hashStatusMap.get(hashStatus), topRowStyle);
            });

            workbook.write(fileStream);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void addStringCells(Row row, List<String> strings, CellStyle style) {
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(strings.get(i));
            cell.setCellStyle(style);
        }
    }

    private void addDataCells(Row row, HashStatus hashStatus, CellStyle style) {
        int colIndex = 0;
        Cell cell = null;
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatus.getStatus());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getLeft().getHash() != null) {
            cell.setCellValue(hashStatus.getLeft().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getLeft().getSize() != 0) {
            cell.setCellValue(hashStatus.getLeft().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getLeft().getLastModified() != null) {
            cell.setCellValue(formatter.format(hashStatus.getLeft().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getRight().getHash() != null) {
            cell.setCellValue(hashStatus.getRight().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getRight().getSize() != 0) {
            cell.setCellValue(hashStatus.getRight().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getRight().getLastModified() != null) {
            cell.setCellValue(formatter.format(hashStatus.getRight().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatus.getFilename());
        cell.setCellStyle(style);
    }
}
