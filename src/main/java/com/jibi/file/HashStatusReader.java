package com.jibi.file;

import com.jibi.common.Algorithm;
import com.jibi.util.DateUtil;
import com.jibi.util.FileUtil;
import com.jibi.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

import static com.jibi.util.DateUtil.parse;
import static java.lang.String.format;
import static java.lang.Long.parseLong;

@Slf4j
public class HashStatusReader extends ExcelReader {

    public HashStatusReader(String filename) {
        super(filename);
    }

    public Collection<HashStatus> readExcel(Algorithm algoSelected) {
        Collection<HashStatus> listFileInfos = Collections.synchronizedList(new ArrayList<>());
        InputStream fileStream = null;
        try {
            if (isEncrypted(filename)) {
                POIFSFileSystem filesystem = new POIFSFileSystem(new FileInputStream(filename));
                EncryptionInfo info = new EncryptionInfo(filesystem);
                Decryptor decryptor = Decryptor.getInstance(info);

                String excelPassword;
                do {
                    excelPassword = FileUtil.getUserPasswordHidden(true, filename);
                } while (!decryptor.verifyPassword(excelPassword));
                log.info("Verified password for file {}", filename);

                fileStream = decryptor.getDataStream(filesystem);
            } else {
                fileStream = new FileInputStream(filename);
            }

            XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
            XSSFSheet sheet = workbook.getSheet("HashStatus");
            boolean containsHashStatusTwo = false;
            if (sheet.getRow(0).getLastCellNum() == 8) {
                log.info("File {} contains hash status two data", filename);
                containsHashStatusTwo = true;
            } else if (sheet.getRow(0).getLastCellNum() == 14) {
                log.info("File {} contains hash status three data", filename);
                containsHashStatusTwo = false;
            } else {
                throw new RuntimeException("Sheet not correct");
            }

            if (containsHashStatusTwo) {
                String fileInfoAlgorithmFull = sheet.getRow(0).getCell(1).getStringCellValue();
                String fileInfoAlgorithm = fileInfoAlgorithmFull.split("[()]")[1];

                if (algoSelected == null && fileInfoAlgorithm.equals("NA")) {
                    log.info("File has no hash and no algorithm specified");
                } else if (algoSelected != null && fileInfoAlgorithm.equals(algoSelected.getValue())) {
                    log.info("File has {} hash and {} algorithm specified", fileInfoAlgorithm, algoSelected.getValue());
                } else {
                    throw new RuntimeException(format("FileInfo hash %s not matching with hash %s",
                            fileInfoAlgorithm, algoSelected == null ? algoSelected : algoSelected.getValue()));
                }

                int numOfRows = sheet.getLastRowNum();
                IntStream.rangeClosed(1, numOfRows).forEach(rowNum -> {
                    Row row = sheet.getRow(rowNum);
                    HashStatusTwo hashStatusTwo = new HashStatusTwo(row.getCell(7).getStringCellValue(), row.getCell(0).getStringCellValue());
                    if (row.getCell(3).getStringCellValue().isEmpty()) {
                        hashStatusTwo.setLeft(new OneSide());
                    } else {
                        hashStatusTwo.setLeft(new OneSide(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(),
                                (long) row.getCell(2).getNumericCellValue(), parse(row.getCell(3).getStringCellValue())));
                    }
                    if (row.getCell(6).getStringCellValue().isEmpty()) {
                        hashStatusTwo.setRight(new OneSide());
                    } else {
                        hashStatusTwo.setRight(new OneSide(row.getCell(0).getStringCellValue(), row.getCell(4).getStringCellValue(),
                                (long) row.getCell(5).getNumericCellValue(), parse(row.getCell(6).getStringCellValue())));
                    }
                    listFileInfos.add(hashStatusTwo);
                });
            } else {
                String fileInfoAlgorithmFull = sheet.getRow(0).getCell(4).getStringCellValue();
                String fileInfoAlgorithm = fileInfoAlgorithmFull.split("[()]")[1];

                if (algoSelected == null && fileInfoAlgorithm.equals("NA")) {
                    log.info("File has no hash and no algorithm specified");
                } else if (algoSelected != null && fileInfoAlgorithm.equals(algoSelected.getValue())) {
                    log.info("File has {} hash and {} algorithm specified", fileInfoAlgorithm, algoSelected.getValue());
                } else {
                    throw new RuntimeException(format("FileInfo hash %s not matching with hash %s",
                            fileInfoAlgorithm, algoSelected == null ? algoSelected : algoSelected.getValue()));
                }

                int numOfRows = sheet.getLastRowNum();
                IntStream.rangeClosed(1, numOfRows).forEach(rowNum -> {
                    Row row = sheet.getRow(rowNum);
                    HashStatusThree hashStatusThree = new HashStatusThree(row.getCell(13).getStringCellValue(), row.getCell(0).getStringCellValue());
                    if (row.getCell(6).getStringCellValue().isEmpty()) {
                        hashStatusThree.setLeft(new OneSide());
                    } else {
                        hashStatusThree.setLeft(new OneSide(row.getCell(1).getStringCellValue(), row.getCell(4).getStringCellValue(),
                                (long) row.getCell(5).getNumericCellValue(), parse(row.getCell(6).getStringCellValue())));
                    }
                    if (row.getCell(9).getStringCellValue().isEmpty()) {
                        hashStatusThree.setCenter(new OneSide());
                    } else {
                        hashStatusThree.setCenter(new OneSide(row.getCell(2).getStringCellValue(), row.getCell(7).getStringCellValue(),
                                (long) row.getCell(8).getNumericCellValue(), parse(row.getCell(9).getStringCellValue())));
                    }
                    if (row.getCell(12).getStringCellValue().isEmpty()) {
                        hashStatusThree.setRight(new OneSide());
                    } else {
                        hashStatusThree.setRight(new OneSide(row.getCell(3).getStringCellValue(), row.getCell(10).getStringCellValue(),
                                (long) row.getCell(11).getNumericCellValue(), parse(row.getCell(12).getStringCellValue())));
                    }

                    listFileInfos.add(hashStatusThree);
                });
            }

        } catch (IOException | GeneralSecurityException fileException) {
            log.warn("file exception for file {}", filename, fileException);
            throw new RuntimeException(format("file exception for file %s", filename));
        }

        return listFileInfos;
    }
}
