package com.jibi;

import com.jibi.service.HashService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

@Slf4j
public class DirHashFilesApplication {



    public static void main(String[] args) {
        HashService hashService = new HashService();
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
        String outFileValue = cmd.getOptionValue("outfile");

        if ("createhash".equals(modeValue)) {
            String inDirValue = cmd.getOptionValue("indir");
            hashService.startCreateHash(hashAlgoValue, inDirValue, outFileValue);
        } else if ("comparehash".equals(modeValue)) {
            String leftSideValue = cmd.getOptionValue("leftside");
            String centerSideValue = cmd.getOptionValue("centerside");
            String rightSideValue = cmd.getOptionValue("rightside");
            hashService.startCompareHash(hashAlgoValue, leftSideValue, centerSideValue, rightSideValue, outFileValue);
        }
    }


}
