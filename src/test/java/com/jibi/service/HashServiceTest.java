package com.jibi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jibi.common.Algorithm;
import com.jibi.vo.FileInfo;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

public class HashServiceTest {

    HashService hashService = new HashService();

    @Test
    public void mapDirFilesFromDirCountTest() {
        Collection<FileInfo> listLeftFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\leftdir");
        Collection<FileInfo> listCenterFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\centerdir");
        Collection<FileInfo> listRightFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\rightdir");

        assertEquals(20, listLeftFileInfosMd5.size());
        assertEquals(20, listCenterFileInfosMd5.size());
        assertEquals(20, listRightFileInfosMd5.size());

        Collection<FileInfo> listLeftFileInfosNull = hashService.mapDirFiles(null, ".\\src\\test\\resources\\testfiles\\leftdir");
        Collection<FileInfo> listCenterFileInfosNull = hashService.mapDirFiles(null, ".\\src\\test\\resources\\testfiles\\centerdir");
        Collection<FileInfo> listRightFileInfosNull = hashService.mapDirFiles(null, ".\\src\\test\\resources\\testfiles\\rightdir");

        assertEquals(20, listLeftFileInfosNull.size());
        assertEquals(20, listCenterFileInfosNull.size());
        assertEquals(20, listRightFileInfosNull.size());
    }

    @Test
    public void mapDirFilesFromDirCollectionFileWindowsTest() {
        Collection<FileInfo> listLeftFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\leftdir");

        Optional<FileInfo> optionalFileInfoLCR = listLeftFileInfosMd5.stream().filter(fileinfo -> fileinfo.getFilename().equals("L-C-R.xml")).findFirst();
        assertNotNull(optionalFileInfoLCR.get());
        FileInfo fileInfoLCR = optionalFileInfoLCR.get();
        assertEquals("L-C-R.xml", fileInfoLCR.getFilename());
        assertEquals("4fd36076075c937f8d031af6d7403996", fileInfoLCR.getHash());
        assertEquals(5, fileInfoLCR.getSize());
    }

    @Test
    public void mapDirFilesFromDirCollectionInnerFileTest() {
        Collection<FileInfo> listLeftFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\leftdir");

        Optional<FileInfo> optionalFileInfoLCRInner = listLeftFileInfosMd5.stream().filter(fileinfo -> fileinfo.getFilename().equals("inner\\L-C-R.xml")).findFirst();
        assertNotNull(optionalFileInfoLCRInner.get());
        FileInfo fileInfoLCR = optionalFileInfoLCRInner.get();
        assertEquals("inner\\L-C-R.xml", fileInfoLCR.getFilename());
        assertEquals("4fd36076075c937f8d031af6d7403996", fileInfoLCR.getHash());
        assertEquals(5, fileInfoLCR.getSize());
    }

}
