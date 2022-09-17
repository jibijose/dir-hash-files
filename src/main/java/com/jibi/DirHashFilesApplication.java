package com.jibi;

import static java.lang.String.format;

import com.jibi.service.HashService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.util.Scanner;

@Slf4j
public class DirHashFilesApplication {

    public static void main(String[] args) {
        DirHashFilesApplication dirHashFilesApplication = new DirHashFilesApplication();
        dirHashFilesApplication.startApplication(args);
    }

    private void startApplication(String[] args) {
        HashService hashService = new HashService();
        CommandLine commandLine = formatOptions(args);

        String modeValue = commandLine.getOptionValue("mode");
        boolean passFlag = Boolean.parseBoolean(commandLine.getOptionValue("passFlag"));

        String hashAlgoValue = commandLine.getOptionValue("hashalgo");
        String outFileValue = commandLine.getOptionValue("outfile");

        if ("create".equals(modeValue)) {
            String inDirValue = commandLine.getOptionValue("indir");
            hashService.startCreate(passFlag, hashAlgoValue, inDirValue, outFileValue);
        } else if ("compare".equals(modeValue)) {
            String leftSideValue = commandLine.getOptionValue("leftside");
            String centerSideValue = commandLine.getOptionValue("centerside");
            String rightSideValue = commandLine.getOptionValue("rightside");
            hashService.startCompare(passFlag, hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, outFileValue);
        }
    }

    private String getUserInputFilePassword(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(format("Enter %s : ", message));
        return scanner.nextLine();
    }

    private CommandLine formatOptions(String[] args) {
        Options options = new Options();

        Option mode = new Option("m", "mode", true, "Operation mode");
        mode.setRequired(true);
        options.addOption(mode);

        Option passFlag = new Option("p", "passFlag", true, "Password Flag");
        passFlag.setRequired(true);
        options.addOption(passFlag);

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
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException parseException) {
            log.error("Parseexception", parseException);
            formatter.printHelp("Java directory hasher", options);
            throw new RuntimeException("Exceution error", parseException);
        }
        return commandLine;
    }

}
