package com.jibi.file;

import static com.jibi.util.DateUtil.format;

import com.jibi.common.Algorithm;
import com.jibi.vo.HashStatus;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HashStatusExcelWriter extends ExcelWriter {

    public HashStatusExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(Algorithm algoSelected, Map<String, HashStatus> hashStatusMap) {
        try {
            FileOutputStream fileStream = new FileOutputStream(filename);
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("HashStatus");
            sheet.createFreezePane(0, 1);
            sheet.setColumnWidth(0, 16 * 256);
            sheet.setColumnWidth(1, (algoSelected.getLength() + 3) * 256);
            sheet.setColumnWidth(2, 16 * 256);
            sheet.setColumnWidth(3, 32 * 256);
            sheet.setColumnWidth(4, (algoSelected.getLength() + 3) * 256);
            sheet.setColumnWidth(5, 16 * 256);
            sheet.setColumnWidth(6, 32 * 256);
            sheet.setColumnWidth(7, 64 * 256);

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
            addStringCells(headerRow, List.of("Status", "Left-Hash (" + algoSelected.getValue() + ")", "Left-Size", "Left-Modified",
                    "Right-Hash (" + algoSelected.getValue() + ")", "Right-Size", "Right-Modified", "filename"), topRowStyle);

            AtomicInteger rowIndex = new AtomicInteger(1);
            hashStatusMap.keySet().stream().forEach(hashStatus -> {
                XSSFRow dataRow = sheet.createRow(rowIndex.getAndIncrement());
                addDataCells(dataRow, hashStatusMap.get(hashStatus), dataRowStyle);
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

    private void addDataCells(Row row, HashStatus hashStatus, CellStyle style) {
        int colIndex = 0;
        Cell cell = null;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatus.getStatus());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getLeft().getHash() != null) {
            cell.setCellValue(hashStatus.getLeft().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getLeft().getSize() != 0) {
            cell.setCellValue(hashStatus.getLeft().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getLeft().getLastModified() != null) {
            cell.setCellValue(format(hashStatus.getLeft().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getRight().getHash() != null) {
            cell.setCellValue(hashStatus.getRight().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getRight().getSize() != 0) {
            cell.setCellValue(hashStatus.getRight().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatus.getRight().getLastModified() != null) {
            cell.setCellValue(format(hashStatus.getRight().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatus.getFilename());
        cell.setCellStyle(style);
    }
}
