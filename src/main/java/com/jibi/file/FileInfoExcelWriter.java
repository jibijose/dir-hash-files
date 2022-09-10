package com.jibi.file;

import static com.jibi.util.DateUtil.format;

import com.jibi.FileInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FileInfoExcelWriter extends ExcelWriter {


    public FileInfoExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(Collection<FileInfo> listFileInfos) {
        try {
            FileOutputStream fileStream = new FileOutputStream(filename);
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("FileInfo");
            sheet.createFreezePane(0, 1);
            sheet.setColumnWidth(0, 70 * 256);
            sheet.setColumnWidth(1, 16 * 256);
            sheet.setColumnWidth(2, 32 * 256);
            sheet.setColumnWidth(3, 64 * 256);

            // Create a header row describing what the columns mean
            CellStyle topRowStyle = workbook.createCellStyle();
            var fontTop = workbook.createFont();
            fontTop.setBold(true);
            topRowStyle.setFont(fontTop);
            topRowStyle.setAlignment(HorizontalAlignment.CENTER);
            topRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            topRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            topRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dataRowStyle = workbook.createCellStyle();
            var fontData = workbook.createFont();
            dataRowStyle.setFont(fontData);

            XSSFRow headerRow = sheet.createRow(0);
            addStringCells(headerRow, List.of("Hash", "Size", "Date Modified", "File Name"), topRowStyle);

            AtomicInteger rowIndex = new AtomicInteger(1);
            listFileInfos.stream().forEach(fileInfo -> {
                XSSFRow dataRow = sheet.createRow(rowIndex.getAndIncrement());
                addDataCells(dataRow, fileInfo, dataRowStyle);
            });

            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, sheet.getRow(0).getLastCellNum()));
            workbook.write(fileStream);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void addStringCells(Row row, List<String> strings, CellStyle style) {
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(strings.get(i));
            cell.setCellStyle(style);
        }
    }

    private void addDataCells(Row row, FileInfo fileInfo, CellStyle style) {
        int colIndex = 0;
        Cell cell = null;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(fileInfo.getHash());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(fileInfo.getSize());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(format(fileInfo.getLastModified()));
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(fileInfo.getFilename());
        cell.setCellStyle(style);
    }

}
