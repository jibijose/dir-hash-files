package com.jibi;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class DirHashFilesApplication {
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

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
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
        }
    }

    private void startCreateHash(String dirValue, String outFileValue) {
        try {
            Collection<File> files = getFiles(dirValue);
            //files.stream().forEach(System.out::println);

            HashMap<String, String> hashMapFiles = new HashMap<>();
            String dirValuePrefix = (dirValue + "\\").replaceAll("\\\\", "\\\\\\\\");
            files.parallelStream().forEach(file -> {
                hashMapFiles.put(file.toString().replaceFirst(dirValuePrefix, ""), getFileChecksum(file));
            });
            //System.out.println("**************************************************************************************************************************************************************************");
            //hashMapFiles.keySet().stream().forEach(el -> System.out.println(hashMapFiles.get(el) + "     " + el));
            //System.out.println("**************************************************************************************************************************************************************************");

            Path path = Path.of(outFileValue);
            Files.write(path, () -> hashMapFiles.entrySet().stream()
                    .<CharSequence>map(e -> e.getValue() + "=" + e.getKey())
                    .iterator());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void startCheckHash(String dirValue, String inFileValue) {
        try {
            Collection<File> files = getFiles(dirValue);
            //files.stream().forEach(System.out::println);

            HashMap<String, String> hashMapFiles = new HashMap<>();
            String dirValuePrefix = (dirValue + "\\").replaceAll("\\\\", "\\\\\\\\");
            files.parallelStream().forEach(file -> {
                hashMapFiles.put(file.toString().replaceFirst(dirValuePrefix, ""), getFileChecksum(file));
            });
            //System.out.println("**************************************************************************************************************************************************************************");
            //hashMapFiles.keySet().stream().forEach(el -> System.out.println(hashMapFiles.get(el) + "     " + el));
            //System.out.println("**************************************************************************************************************************************************************************");

            Map<String, String> sigMap = readKeyValueFile(inFileValue);

            AtomicLong matchedFiles = new AtomicLong(0);
            AtomicLong notMatchedFiles = new AtomicLong(0);
            AtomicLong newFiles = new AtomicLong(0);
            AtomicLong missingFiles = new AtomicLong(0);

            System.out.println("**************************************************************************************************************************************************************************");
            hashMapFiles.keySet().stream().forEach(file -> {
                if (sigMap.containsKey(file)) {
                    if (!hashMapFiles.get(file).equals(sigMap.get(file))) {
                        notMatchedFiles.incrementAndGet();
                        System.out.println(StringUtils.rightPad("Not Matched", 20) + StringUtils.rightPad(sigMap.get(file), 70) + file);
                    } else {
                        matchedFiles.incrementAndGet();
                        System.out.println(StringUtils.rightPad("Matched", 20) + StringUtils.rightPad(hashMapFiles.get(file), 70) + file);
                    }
                } else {
                    newFiles.incrementAndGet();
                    System.out.println(StringUtils.rightPad("New File", 20) + StringUtils.rightPad(hashMapFiles.get(file), 70) + file);
                }
            });

            sigMap.keySet().stream().forEach(file -> {
                if (!hashMapFiles.containsKey(file)) {
                    missingFiles.incrementAndGet();
                    System.out.println(StringUtils.rightPad("Missing File", 20) + StringUtils.rightPad(sigMap.get(file), 70) + file);
                }
            });
            System.out.println("**************************************************************************************************************************************************************************");
            System.out.println("Matched = " + matchedFiles + ", Not matched = " + notMatchedFiles + ", Missing files = " + missingFiles + ", New files = " + newFiles);
            System.out.println("**************************************************************************************************************************************************************************");


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

    private static String getFileChecksum(File file) {
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

            //return complete hash
            return sb.toString();
        } catch (IOException ioException) {
            return null;
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            return null;
        }
    }

    private Collection<File> getFiles(String dirValue) {
        File dir = new File(dirValue);
        Collection files = FileUtils.listFiles(dir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        //files.stream().forEach(System.out::println);
        return files;
    }
}
