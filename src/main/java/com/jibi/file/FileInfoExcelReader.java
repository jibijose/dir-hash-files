package com.jibi.file;

import static java.lang.String.format;
import static com.jibi.util.DateUtil.parse;

import com.jibi.common.Algorithm;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
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

@Slf4j
public class FileInfoExcelReader extends ExcelReader {


    public FileInfoExcelReader(String filename) {
        super(filename);
    }

    public Collection<FileInfo> readExcel(Algorithm algoSelected) {
        Collection<FileInfo> listFileInfos = Collections.synchronizedList(new ArrayList<>());
        InputStream fileStream = null;
        try {
            if (isEncrypted(filename)) {
                POIFSFileSystem filesystem = new POIFSFileSystem(new FileInputStream(filename));
                EncryptionInfo info = new EncryptionInfo(filesystem);
                Decryptor decryptor = Decryptor.getInstance(info);

                String excelPassword;
                do {
                    excelPassword = FileUtil.getUserInputFilePassword(format("password for %s", filename));
                } while (!decryptor.verifyPassword(excelPassword));
                log.info("Verified password for file {}", filename);

                fileStream = decryptor.getDataStream(filesystem);
            } else {
                fileStream = new FileInputStream(filename);
            }

            XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
            XSSFSheet sheet = workbook.getSheet("FileInfo");

            String fileInfoAlgorithm = sheet.getRow(0).getCell(0).getStringCellValue();
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
                FileInfo fileInfo = new FileInfo(row.getCell(3).getStringCellValue(),
                        (long) row.getCell(1).getNumericCellValue(),
                        row.getCell(0).getStringCellValue(),
                        parse(row.getCell(2).getStringCellValue()));
                listFileInfos.add(fileInfo);
            });
        } catch (IOException | GeneralSecurityException fileException) {
            log.warn("file exception for file {}", filename, fileException);
            throw new RuntimeException(format("file exception for file %s", filename));
        }

        return listFileInfos;
    }

    public boolean isEncrypted(String path) {

        try {
            try {
                new POIFSFileSystem(new FileInputStream(path));
            } catch (IOException ioException) {
                log.error("IOException with file name {}", filename, ioException);
            }
            return true;
        } catch (OfficeXmlFileException officeXmlFileException) {
            return false;
        }
    }

}
