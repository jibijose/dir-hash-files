package com.jibi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jibi.common.Algorithm;
import com.jibi.util.SystemUtil;
import com.jibi.vo.FileInfo;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

public class HashServiceTest {

    HashService hashService = new HashService();

    private String getLeftDir() {
        if (SystemUtil.isWindowsSystem()) {
            return ".\\src\\test\\resources\\testfiles\\leftdir";
        } else {
            return "./src/test/resources/testfiles/leftdir";
        }
    }

    private String getCenterDir() {
        if (SystemUtil.isWindowsSystem()) {
            return ".\\src\\test\\resources\\testfiles\\centerdir";
        } else {
            return "./src/test/resources/testfiles/centerdir";
        }
    }

    private String getRightDir() {
        if (SystemUtil.isWindowsSystem()) {
            return ".\\src\\test\\resources\\testfiles\\rightdir";
        } else {
            return "./src/test/resources/testfiles/rightdir";
        }
    }

    @Test
    public void mapDirFilesFromDirCountTest() {
        Collection<FileInfo> listLeftFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, getLeftDir());
        Collection<FileInfo> listCenterFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, getCenterDir());
        Collection<FileInfo> listRightFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, getRightDir());

        assertEquals(20, listLeftFileInfosMd5.size());
        assertEquals(20, listCenterFileInfosMd5.size());
        assertEquals(20, listRightFileInfosMd5.size());

        Collection<FileInfo> listLeftFileInfosNull = hashService.mapDirFiles(null, getLeftDir());
        Collection<FileInfo> listCenterFileInfosNull = hashService.mapDirFiles(null, getCenterDir());
        Collection<FileInfo> listRightFileInfosNull = hashService.mapDirFiles(null, getRightDir());

        assertEquals(20, listLeftFileInfosNull.size());
        assertEquals(20, listCenterFileInfosNull.size());
        assertEquals(20, listRightFileInfosNull.size());

    }

    @Test
    public void mapDirFilesFromDirCollectionFileWindowsTest() {
        Collection<FileInfo> listLeftFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, getLeftDir());

        Optional<FileInfo> optionalFileInfoLCR = listLeftFileInfosMd5.stream().filter(fileinfo -> fileinfo.getFilename().equals("L-C-R.xml")).findFirst();
        assertNotNull(optionalFileInfoLCR.get());
        FileInfo fileInfoLCR = optionalFileInfoLCR.get();
        assertEquals("L-C-R.xml", fileInfoLCR.getFilename());
        assertEquals("4fd36076075c937f8d031af6d7403996", fileInfoLCR.getHash());
        assertEquals(5, fileInfoLCR.getSize());
    }

    @Test
    public void mapDirFilesFromDirCollectionInnerFileTest() {
        Collection<FileInfo> listLeftFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, getLeftDir());

        if (SystemUtil.isWindowsSystem()) {
            Optional<FileInfo> optionalFileInfoLCRInner = listLeftFileInfosMd5.stream().filter(fileinfo -> fileinfo.getFilename().equals("inner\\L-C-R.xml")).findFirst();
            assertNotNull(optionalFileInfoLCRInner.get());
            FileInfo fileInfoLCR = optionalFileInfoLCRInner.get();
            assertEquals("inner\\L-C-R.xml", fileInfoLCR.getFilename());
            assertEquals("4fd36076075c937f8d031af6d7403996", fileInfoLCR.getHash());
            assertEquals(5, fileInfoLCR.getSize());
        } else {
            Optional<FileInfo> optionalFileInfoLCRInner = listLeftFileInfosMd5.stream().filter(fileinfo -> fileinfo.getFilename().equals("inner/L-C-R.xml")).findFirst();
            assertNotNull(optionalFileInfoLCRInner.get());
            FileInfo fileInfoLCR = optionalFileInfoLCRInner.get();
            assertEquals("inner/L-C-R.xml", fileInfoLCR.getFilename());
            assertEquals("4fd36076075c937f8d031af6d7403996", fileInfoLCR.getHash());
            assertEquals(5, fileInfoLCR.getSize());
        }
    }

}
