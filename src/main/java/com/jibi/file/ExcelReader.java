package com.jibi.file;

import com.jibi.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import static java.lang.String.format;

@Slf4j
public class ExcelReader {

    protected String filename;
    protected String excelPassword = null;

    public ExcelReader(String filename, String excelPassword) {
        this.filename = filename;
        this.excelPassword = excelPassword;
    }

    public static String getExcelPassword(String filename) {
        String excelPassword = null;
        try {
            if (isEncrypted(filename)) {
                POIFSFileSystem filesystem = new POIFSFileSystem(new FileInputStream(filename));
                EncryptionInfo info = new EncryptionInfo(filesystem);
                Decryptor decryptor = Decryptor.getInstance(info);
                do {
                    excelPassword = FileUtil.getUserPasswordHidden(true, filename);
                } while (!decryptor.verifyPassword(excelPassword));
                log.info("Verified password for file {}", filename);
            }
        } catch (IOException | GeneralSecurityException fileException) {
            log.warn("file exception for file {}", filename, fileException);
            throw new RuntimeException(format("file exception for file %s", filename));
        }
        return excelPassword;
    }

    protected static boolean isEncrypted(String filename) {

        try {
            try {
                new POIFSFileSystem(new FileInputStream(filename));
            } catch (IOException ioException) {
                log.error("IOException with file name {}", filename, ioException);
            }
            return true;
        } catch (OfficeXmlFileException officeXmlFileException) {
            return false;
        }
    }

    public static boolean hasSheet(String sheetName, String filename, String excelPassword) {
        try (InputStream fileStream = getExcelInputStream(filename, excelPassword); XSSFWorkbook workbook = new XSSFWorkbook(fileStream);) {
            XSSFSheet sheet = workbook.getSheet(sheetName);
            if (sheet != null) {
                return true;
            }
        } catch (IOException fileException) {
            log.warn("file exception for file {}", filename, fileException);
            throw new RuntimeException(format("file exception for file %s", filename));
        }
        return false;
    }

    protected static InputStream getExcelInputStream(String filename, String excelPassword) {
        InputStream fileStream = null;
        try {
            if (excelPassword != null) {
                POIFSFileSystem filesystem = new POIFSFileSystem(new FileInputStream(filename));
                EncryptionInfo info = new EncryptionInfo(filesystem);
                Decryptor decryptor = Decryptor.getInstance(info);
                decryptor.verifyPassword(excelPassword);
                fileStream = decryptor.getDataStream(filesystem);
            } else {
                fileStream = new FileInputStream(filename);
            }
        } catch (IOException | GeneralSecurityException fileException) {
            log.warn("file exception for file {}", filename, fileException);
            throw new RuntimeException(format("file exception for file %s", filename));
        }
        return fileStream;
    }


}
