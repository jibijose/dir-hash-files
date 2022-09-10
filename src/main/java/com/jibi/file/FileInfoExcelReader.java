package com.jibi.file;

import static com.jibi.util.DateUtil.parse;

import com.jibi.vo.FileInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;

public class FileInfoExcelReader extends ExcelReader {


    public FileInfoExcelReader(String filename) {
        super(filename);
    }

    public Collection<FileInfo> readExcel() {
        Collection<FileInfo> listFileInfos = new ArrayList<>();
        try {
            FileInputStream fileStream = new FileInputStream(filename);
            XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
            XSSFSheet sheet = workbook.getSheet("FileInfo");

            int numOfRows = sheet.getLastRowNum();
            IntStream.rangeClosed(1, numOfRows).forEach(rowNum -> {
                Row row = sheet.getRow(rowNum);
                FileInfo fileInfo = new FileInfo(row.getCell(3).getStringCellValue(),
                        (long) row.getCell(1).getNumericCellValue(),
                        row.getCell(0).getStringCellValue(),
                        parse(row.getCell(2).getStringCellValue()));
                listFileInfos.add(fileInfo);
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return listFileInfos;
    }

}
