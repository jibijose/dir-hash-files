package com.jibi.file;

import com.jibi.common.Algorithm;
import com.jibi.vo.HashStatusThree;
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

import static com.jibi.util.DateUtil.format;

public class HashStatusThreeExcelWriter extends ExcelWriter {

    public HashStatusThreeExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(Algorithm algoSelected, Map<String, HashStatusThree> hashStatusMap) {
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
            sheet.setColumnWidth(1, 16 * 256);
            sheet.setColumnWidth(2, 16 * 256);
            sheet.setColumnWidth(3, 16 * 256);

            sheet.setColumnWidth(4, (algoLength + 3) * 256);
            sheet.setColumnWidth(5, 16 * 256);
            sheet.setColumnWidth(6, 32 * 256);

            sheet.setColumnWidth(7, (algoLength + 3) * 256);
            sheet.setColumnWidth(8, 16 * 256);
            sheet.setColumnWidth(9, 32 * 256);

            sheet.setColumnWidth(10, (algoLength + 3) * 256);
            sheet.setColumnWidth(11, 16 * 256);
            sheet.setColumnWidth(12, 32 * 256);

            sheet.setColumnWidth(13, 128 * 256);

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
            addStringCells(headerRow, List.of("Status", "Left", "Center", "Right",
                    "Left-Hash (" + algoValue + ")", "Left-Size", "Left-Modified",
                    "Center-Hash (" + algoValue + ")", "Center-Size", "Center-Modified",
                    "Right-Hash (" + algoValue + ")", "Right-Size", "Right-Modified",
                    "filename"), topRowStyle);

            AtomicInteger rowIndex = new AtomicInteger(1);
            hashStatusMap.keySet().stream().forEach(hashStatusThree -> {
                XSSFRow dataRow = sheet.createRow(rowIndex.getAndIncrement());
                addDataCells(dataRow, hashStatusMap.get(hashStatusThree), dataRowStyle);
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

    private void addDataCells(Row row, HashStatusThree hashStatusThree, CellStyle style) {
        int colIndex = 0;
        Cell cell = null;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getStatus());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getLeft().getStatus());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getCenter().getStatus());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getRight().getStatus());
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getHash() != null) {
            cell.setCellValue(hashStatusThree.getLeft().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getSize() != 0) {
            cell.setCellValue(hashStatusThree.getLeft().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getLeft().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getHash() != null) {
            cell.setCellValue(hashStatusThree.getCenter().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getSize() != 0) {
            cell.setCellValue(hashStatusThree.getCenter().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getCenter().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getHash() != null) {
            cell.setCellValue(hashStatusThree.getRight().getHash());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getSize() != 0) {
            cell.setCellValue(hashStatusThree.getRight().getSize());
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getRight().getLastModified()));
        }
        cell.setCellStyle(style);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getFilename());
        cell.setCellStyle(style);
    }
}
