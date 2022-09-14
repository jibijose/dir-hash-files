package com.jibi.file;

import static com.jibi.util.DateUtil.format;

import com.jibi.common.Algorithm;
import com.jibi.vo.HashStatusTwo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HashStatusTwoExcelWriter extends ExcelWriter {

    public HashStatusTwoExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(Algorithm algoSelected, Map<String, HashStatusTwo> hashStatusMap) {
        int algoLength = 20;
        String algoValue = "NA";
        if (algoSelected != null) {
            algoLength = algoSelected.getLength();
            algoValue = algoSelected.getValue();
        }

        try {
            FileOutputStream fileStream = new FileOutputStream(filename);
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("HashStatus");
            sheet.createFreezePane(0, 1);
            sheet.setColumnWidth(0, 16 * 256);
            sheet.setColumnWidth(1, (algoLength + 3) * 256);
            sheet.setColumnWidth(2, 16 * 256);
            sheet.setColumnWidth(3, 32 * 256);
            sheet.setColumnWidth(4, (algoLength + 3) * 256);
            sheet.setColumnWidth(5, 16 * 256);
            sheet.setColumnWidth(6, 32 * 256);
            sheet.setColumnWidth(7, 128 * 256);

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
            addStringCells(headerRow, List.of("Status", "Left-Hash (" + algoValue + ")", "Left-Size", "Left-Modified",
                    "Right-Hash (" + algoValue + ")", "Right-Size", "Right-Modified", "filename"), topRowStyle);

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

    private void addDataCells(Row row, HashStatusTwo hashStatusTwo, CellStyle style) {
        int colIndex = 0;
        Cell cell = null;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusTwo.getStatus());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getLeft().getHash() != null) {
            cell.setCellValue(hashStatusTwo.getLeft().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getLeft().getSize() >= 0) {
            cell.setCellValue(hashStatusTwo.getLeft().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getLeft().getLastModified() != null) {
            cell.setCellValue(format(hashStatusTwo.getLeft().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getRight().getHash() != null) {
            cell.setCellValue(hashStatusTwo.getRight().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getRight().getSize() >= 0) {
            cell.setCellValue(hashStatusTwo.getRight().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getRight().getLastModified() != null) {
            cell.setCellValue(format(hashStatusTwo.getRight().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusTwo.getFilename());
        cell.setCellStyle(style);
    }
}
