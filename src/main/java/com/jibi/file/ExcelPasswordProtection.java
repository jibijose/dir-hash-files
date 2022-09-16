package com.jibi.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.security.GeneralSecurityException;

@Slf4j
public class ExcelPasswordProtection {

    public void encryptWorkbook(File file, String password) throws IOException, GeneralSecurityException, InvalidFormatException {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            try (OPCPackage opc = OPCPackage.open(file, PackageAccess.READ_WRITE);
                 OutputStream os = getEncryptingOutputStream(fs, password)) {
                opc.save(os);
            }

            // Write out the encrypted version
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fs.writeFilesystem(fos);
            }
        }
    }

    public void decryptWorkbook(File file, String password) throws IOException, GeneralSecurityException {
        try (POIFSFileSystem fs = new POIFSFileSystem(file)) {
            InputStream fis = getDecryptingInputStream(fs, password);

            File targetFile = new File("./jj.xlsx");
            OutputStream outStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(outStream);
        }
    }

    private void encryptWorkbookOld(String password, String filePath) throws IOException, GeneralSecurityException {
        if (password == null || StringUtils.isEmpty(password.trim())) {
            log.info("File {} skipped since password is not provided");
            return;
        }

        try (POIFSFileSystem fileSystem = new POIFSFileSystem()) {
            try (Workbook wb = WorkbookFactory.create(new File(filePath));
                 OutputStream out = getEncryptingOutputStream(fileSystem, password);) {
                wb.write(out);
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                fileSystem.writeFilesystem(fileOutputStream);
            }
        }
    }

    private void decryptWorkbookOld(String password, String filePath) throws IOException, GeneralSecurityException {
        if (password == null || StringUtils.isEmpty(password.trim())) {
            log.info("File {} skipped since password is not provided");
            return;
        }

        try (POIFSFileSystem fileSystem = new POIFSFileSystem()) {
            try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                Workbook workbook = new XSSFWorkbook(fileInputStream);
            }
            try (Workbook wb = WorkbookFactory.create(new File(filePath));
                 OutputStream out = getEncryptingOutputStream(fileSystem, password);) {
                wb.write(out);
            }

        }
    }

    private OutputStream getEncryptingOutputStream(POIFSFileSystem fileSystem, String password) throws IOException, GeneralSecurityException {
        EncryptionInfo encryptionInfo = new EncryptionInfo(EncryptionMode.standard);
        Encryptor encryptor = encryptionInfo.getEncryptor();
        encryptor.confirmPassword(password);
        return encryptor.getDataStream(fileSystem);
    }

    private InputStream getDecryptingInputStream(POIFSFileSystem fileSystem, String password) throws IOException, GeneralSecurityException {
        EncryptionInfo encryptionInfo = new EncryptionInfo(fileSystem);
        Decryptor decryptor = encryptionInfo.getDecryptor();
        decryptor.verifyPassword(password);
        return decryptor.getDataStream(fileSystem);
    }
}
