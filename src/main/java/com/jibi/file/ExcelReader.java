package com.jibi.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class ExcelReader {

    protected String filename;

    public ExcelReader(String filename) {
        this.filename = filename;
    }

    protected boolean isEncrypted(String path) {

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
