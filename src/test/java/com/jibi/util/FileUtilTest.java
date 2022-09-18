package com.jibi.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collection;

@Slf4j
public class FileUtilTest {

    @Test
    public void getFilesTest() {
        Collection<File> files = null;

        if (SystemUtil.isWindowsSystem()) {
            files = FileUtil.getFiles(null);
            assertNotNull(files);
            log.debug("null has {} files", files.size());

            files = FileUtil.getFiles("F:");
            assertNotNull(files);
            log.debug("F: has {} files", files.size());

            files = FileUtil.getFiles("F:\\");
            assertNotNull(files);
            log.debug("F:\\ has {} files", files.size());

            files = FileUtil.getFiles("F:/");
            assertNotNull(files);
            log.debug("F:/ has {} files", files.size());

            files = FileUtil.getFiles("F:\\Desktop");
            assertNotNull(files);
            log.debug("F:\\Desktop has {} files", files.size());

            files = FileUtil.getFiles("F:\\Desktop\\");
            assertNotNull(files);
            log.debug("F:\\Desktop\\ has {} files", files.size());

            files = FileUtil.getFiles("F:/Desktop");
            assertNotNull(files);
            log.debug("F:/Desktop has {} files", files.size());

            files = FileUtil.getFiles("F:/Desktop/");
            assertNotNull(files);
            log.debug("F:/Desktop/ has {} files", files.size());
        } else if (SystemUtil.isUnixSystem()) {

        }
    }
}
