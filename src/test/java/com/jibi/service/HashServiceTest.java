package com.jibi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jibi.common.Algorithm;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class HashServiceTest {

    @Test
    public void mapDirFilesFromDirCountTest() {
        HashService hashService = new HashService();
        Collection<FileInfo> listLeftFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\leftdir");
        Collection<FileInfo> listCenterFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\centerdir");
        Collection<FileInfo> listRightFileInfosMd5 = hashService.mapDirFiles(Algorithm.MD5, ".\\src\\test\\resources\\testfiles\\rightdir");

        assertEquals(10, listLeftFileInfosMd5.size());
        assertEquals(10, listCenterFileInfosMd5.size());
        assertEquals(10, listRightFileInfosMd5.size());

        Collection<FileInfo> listLeftFileInfosNull = hashService.mapDirFiles(null, ".\\src\\test\\resources\\testfiles\\leftdir");
        Collection<FileInfo> listCenterFileInfosNull = hashService.mapDirFiles(null, ".\\src\\test\\resources\\testfiles\\centerdir");
        Collection<FileInfo> listRightFileInfosNull = hashService.mapDirFiles(null, ".\\src\\test\\resources\\testfiles\\rightdir");

        assertEquals(10, listLeftFileInfosNull.size());
        assertEquals(10, listCenterFileInfosNull.size());
        assertEquals(10, listRightFileInfosNull.size());
    }

}
