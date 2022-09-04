package com.jibi;

import static org.apache.commons.lang3.StringUtils.rightPad;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class DirHashFilesApplication {

    private static int PAD_MARK = 14;
    private static int PAD_HASH = 66;


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
            //Collection<File> files = dirHashFilesApplication.getFiles(dirValue);
            //files.stream().forEach(System.out::println);
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

    private HashMap<String, String> mapDirFiles(String dir) {
        log.debug("**************************************************************************************************************************************************************************");
        Collection<File> files = getFiles(dir);
        HashMap<String, String> hashMapFiles = new HashMap<>();
        String dirValuePrefix = (dir + "\\").replaceAll("\\\\", "\\\\\\\\");
        long numTotalFiles = files.size();
        double blockSize = 1.0 * numTotalFiles / numOfPercentPrints;
        log.info("Number of files to hash {}", numTotalFiles);
        AtomicLong hashedFiles = new AtomicLong(0);
        files.parallelStream().forEach(file -> {
            String fileHash = getFileChecksum(file);
            hashedFiles.incrementAndGet();
            String relativeFilePath = file.toString().replaceFirst(dirValuePrefix, "");
            //log.debug("{} {} {}", StringUtils.rightPad("Hashed", PAD_MARK), StringUtils.rightPad(fileHash, PAD_HASH), relativeFilePath);
            hashMapFiles.put(relativeFilePath, fileHash);
            printHashingStatus(numTotalFiles, blockSize, hashedFiles.get());
        });
        return hashMapFiles;
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

    private void startCreateHash(String dirValue, String outFileValue) {
        try {
            Map<String, String> hashMapFiles = mapDirFiles(dirValue);

            Path path = Path.of(outFileValue);
            log.debug("**************************************************************************************************************************************************************************");
            log.info("Output file path {}", path.toFile().getAbsolutePath());
            Files.write(path, () -> hashMapFiles.entrySet().stream()
                    .<CharSequence>map(e -> e.getValue() + "=" + e.getKey())
                    .iterator());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void compareAndReportLeftRight(Map<String, String> hashMapFilesLeft, Map<String, String> hashMapFilesRight) {


        Map<String, HashStatus> hashStatusMap = new HashMap<>();

        hashMapFilesLeft.keySet().stream().forEach(file -> {
            hashStatusMap.put(file, HashStatus.buildWithLeftHash(file, "Missing File", hashMapFilesLeft.get(file)));
        });
        hashMapFilesRight.keySet().stream().forEach(file -> {
            if (hashMapFilesLeft.containsKey(file)) {
                String leftHash = hashMapFilesLeft.get((file));
                String rightHash = hashMapFilesRight.get(file);
                hashStatusMap.get(file).setRighthash(rightHash);
                if (leftHash.equals(rightHash)) {
                    hashStatusMap.get(file).setStatus("Matched");
                } else {
                    hashStatusMap.get(file).setStatus("Not Matched");
                }
            } else {
                hashStatusMap.put(file, new HashStatus(file, "New File", null, hashMapFilesRight.get(file)));
            }
        });
        log.debug("**************************************************************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isNotMatched).forEach(hashStatus -> {
            log.debug("{} {} {} {}", rightPad(hashStatus.getStatus(), PAD_MARK), rightPad(hashStatus.getLefthash(), PAD_HASH),
                    rightPad(hashStatus.getRighthash(), PAD_HASH), hashStatus.getFilename());
        });
        log.debug("**************************************************************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isMissingFile).forEach(hashStatus -> {
            log.debug("{} {} {} {}", rightPad(hashStatus.getStatus(), PAD_MARK), rightPad(hashStatus.getLefthash(), PAD_HASH),
                    rightPad(hashStatus.getRighthash(), PAD_HASH), hashStatus.getFilename());
        });
        log.debug("**************************************************************************************************************************************************************************");
        hashStatusMap.values().stream().filter(HashStatus::isNewFile).forEach(hashStatus -> {
            log.debug("{} {} {} {}", rightPad(hashStatus.getStatus(), PAD_MARK), rightPad(hashStatus.getLefthash(), PAD_HASH),
                    rightPad(hashStatus.getRighthash(), PAD_HASH), hashStatus.getFilename());
        });
        log.debug("**************************************************************************************************************************************************************************");
        long matchedFiles = hashStatusMap.values().stream().filter(HashStatus::isMatched).count();
        long notMatchedFiles = hashStatusMap.values().stream().filter(HashStatus::isNotMatched).count();
        long missingFiles = hashStatusMap.values().stream().filter(HashStatus::isMissingFile).count();
        long newFiles = hashStatusMap.values().stream().filter(HashStatus::isNewFile).count();
        log.info("Matched = {}, Not matched = {}, Missing files = {}, New files = {}", matchedFiles, notMatchedFiles, missingFiles, newFiles);
        log.debug("**************************************************************************************************************************************************************************");
    }

    private void startCheckHash(String dirValue, String inFileValue) {
        try {
            Map<String, String> hashMapFiles = mapDirFiles(dirValue);
            Map<String, String> sigMap = readKeyValueFile(inFileValue);

            compareAndReportLeftRight(sigMap, hashMapFiles);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void startCompareHash(String dirLeftValue, String dirRightValue) {
        try {
            Map<String, String> hashMapFilesLeft = mapDirFiles(dirLeftValue);
            Map<String, String> hashMapFilesRight = mapDirFiles(dirRightValue);

            compareAndReportLeftRight(hashMapFilesLeft, hashMapFilesRight);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Map<String, String> readKeyValueFile(String filePath) {
        Map<String, String> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.filter(line -> line.contains("="))
                    .forEach(line -> {
                        String[] keyValuePair = line.split("=", 2);
                        map.put(keyValuePair[1], keyValuePair[0]);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
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

    private Collection<File> getFilesOld(String dirValue) {
        File dir = new File(dirValue);
        File[] filesRootDir = dir.listFiles();
        for (File file : filesRootDir) {
            if (file.isDirectory()) continue;
        }

        Collection files = FileUtils.listFiles(dir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        return files;
    }
}
