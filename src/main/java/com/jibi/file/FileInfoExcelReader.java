package com.jibi.file;

import static java.lang.String.format;
import static com.jibi.util.DateUtil.parse;

import com.jibi.common.Algorithm;
import com.jibi.vo.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;

@Slf4j
public class FileInfoExcelReader extends ExcelReader {


    public FileInfoExcelReader(String filename) {
        super(filename);
    }

    public Collection<FileInfo> readExcel(Algorithm algoSelected) {
        Collection<FileInfo> listFileInfos = new ArrayList<>();

        try (FileInputStream fileStream = new FileInputStream(filename)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
            XSSFSheet sheet = workbook.getSheet("FileInfo");

            String fileInfoAlgorithm = sheet.getRow(0).getCell(0).getStringCellValue();
            if (!algoSelected.getValue().equals(fileInfoAlgorithm)) {
                throw new RuntimeException(format("FileInfo hash %s not matching with hash %s", fileInfoAlgorithm, algoSelected.getValue()));
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
        } catch (IOException ioException) {
            throw new RuntimeException(format("Unable to open file %s", filename), ioException);
        }

        return listFileInfos;
    }

}
