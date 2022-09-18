package com.jibi.file;

import static com.jibi.util.FileUtil.MATCH;
import static com.jibi.util.FileUtil.NEWFILE;
import static com.jibi.util.FileUtil.NOTSYNCED;

import com.jibi.common.Algorithm;
import com.jibi.util.FileUtil;
import com.jibi.vo.HashStatusThree;
import com.jibi.vo.HashStatusTwo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jibi.util.DateUtil.format;

@Slf4j
public class HashStatusThreeExcelWriter extends ExcelWriter {

    public HashStatusThreeExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(boolean passFlag, Algorithm algoSelected, Map<String, HashStatusThree> hashStatusMap) {
        int algoLength = 20;
        String algoValue = "NA";
        if (algoSelected != null) {
            algoLength = algoSelected.getLength();
            algoValue = algoSelected.getValue();
        }
        SortedMap<String, HashStatusThree> sortedHashStatusMap = new TreeMap<>();
        sortedHashStatusMap.putAll(hashStatusMap);

        try {
            FileOutputStream fileStream = new FileOutputStream(filename);
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("HashStatus");
            sheet.createFreezePane(0, 1);

            setSheetWidths(sheet, algoLength);
            Map<String, CellStyle> cellStyles = createCellStyles(workbook);

            XSSFRow headerRow = sheet.createRow(0);
            addStringCells(headerRow, List.of("Status", "Left", "Center", "Right",
                    "Left-Hash (" + algoValue + ")", "Left-Size", "Left-Modified",
                    "Center-Hash (" + algoValue + ")", "Center-Size", "Center-Modified",
                    "Right-Hash (" + algoValue + ")", "Right-Size", "Right-Modified",
                    "filename"), cellStyles.get(TOPROWSTYLE));

            AtomicInteger rowIndex = new AtomicInteger(1);
            AtomicInteger requiredFileNameWidth = new AtomicInteger(0);
            sortedHashStatusMap.keySet().stream().forEach(hashStatus -> {
                XSSFRow dataRow = sheet.createRow(rowIndex.getAndIncrement());
                addDataCells(dataRow, sortedHashStatusMap.get(hashStatus), cellStyles);
                if (sortedHashStatusMap.get(hashStatus).getFilename().length() > requiredFileNameWidth.get()) {
                    requiredFileNameWidth.set(sortedHashStatusMap.get(hashStatus).getFilename().length());
                }
            });
            sheet.setColumnWidth(13, ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)) * 256);
            log.info("HashStatus filename column width adjusted to {}", ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)));
            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, sheet.getRow(0).getLastCellNum()));
            workbook.write(fileStream);
            if (passFlag) {
                FileUtil.setExcelPassword(filename);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private final static String TOPROWSTYLE = "TopRowStyle";
    private final static String DATAROWLEFTSTYLE = "DataRowLeftStyle";
    private final static String DATAROWCENTERSTYLE = "DataRowCenterStyle";
    private final static String DATAROWRIGHTSTYLE = "DataRowRightStyle";
    private final static String DATAROWLEFTREDSTYLE = "DataRowLeftRedStyle";
    private final static String DATAROWCENTERREDSTYLE = "DataRowCenterRedStyle";
    private final static String DATAROWRIGHTREDSTYLE = "DataRowRightRedStyle";

    private final static String DATAROWLEFTBLUESTYLE = "DataRowLeftBlueStyle";
    private final static String DATAROWCENTERBLUESTYLE = "DataRowCenterBlueStyle";
    private final static String DATAROWRIGHTBLUESTYLE = "DataRowRightBlueStyle";

    private final static String DATAROWLEFTGREENSTYLE = "DataRowLeftGreenStyle";
    private final static String DATAROWCENTERGREENSTYLE = "DataRowCenterGreenStyle";
    private final static String DATAROWRIGHTGREENSTYLE = "DataRowRightGreenStyle";

    private Map<String, CellStyle> createCellStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> cellStyles = new HashMap<>();
        int colorWeight = 255;
        int otherWeight = 50;
        XSSFColor colorRed = new XSSFColor(new java.awt.Color(colorWeight, otherWeight, otherWeight), null);
        XSSFColor colorGreen = new XSSFColor(new java.awt.Color(otherWeight, colorWeight, otherWeight), null);
        XSSFColor colorBlue = new XSSFColor(new java.awt.Color(otherWeight, otherWeight, colorWeight), null);

        // Create a header row describing what the columns mean
        XSSFCellStyle topRowStyle = workbook.createCellStyle();
        var fontTop = workbook.createFont();
        fontTop.setBold(true);
        topRowStyle.setFont(fontTop);
        topRowStyle.setAlignment(HorizontalAlignment.CENTER);
        topRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        topRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        topRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyles.put(TOPROWSTYLE, topRowStyle);

        XSSFCellStyle dataRowLeftStyle = workbook.createCellStyle();
        dataRowLeftStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTSTYLE, dataRowLeftStyle);

        XSSFCellStyle dataRowCenterStyle = workbook.createCellStyle();
        dataRowCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERSTYLE, dataRowCenterStyle);

        XSSFCellStyle dataRowRightStyle = workbook.createCellStyle();
        dataRowRightStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTSTYLE, dataRowRightStyle);

        XSSFCellStyle dataRowLeftRedStyle = workbook.createCellStyle();
        dataRowLeftRedStyle.setFillForegroundColor(colorRed);
        dataRowLeftRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftRedStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTREDSTYLE, dataRowLeftRedStyle);

        XSSFCellStyle dataRowCenterRedStyle = workbook.createCellStyle();
        dataRowCenterRedStyle.setFillForegroundColor(colorRed);
        dataRowCenterRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterRedStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERREDSTYLE, dataRowCenterRedStyle);

        XSSFCellStyle dataRowRightRedStyle = workbook.createCellStyle();
        dataRowRightRedStyle.setFillForegroundColor(colorRed);
        dataRowRightRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightRedStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTREDSTYLE, dataRowRightRedStyle);

        XSSFCellStyle dataRowLeftGreenStyle = workbook.createCellStyle();
        dataRowLeftGreenStyle.setFillForegroundColor(colorGreen);
        dataRowLeftGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftGreenStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTGREENSTYLE, dataRowLeftGreenStyle);

        XSSFCellStyle dataRowCenterGreenStyle = workbook.createCellStyle();
        dataRowCenterGreenStyle.setFillForegroundColor(colorGreen);
        dataRowCenterGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterGreenStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERGREENSTYLE, dataRowCenterGreenStyle);

        XSSFCellStyle dataRowRightGreenStyle = workbook.createCellStyle();
        dataRowRightGreenStyle.setFillForegroundColor(colorGreen);
        dataRowRightGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightGreenStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTGREENSTYLE, dataRowRightGreenStyle);

        XSSFCellStyle dataRowLeftBlueStyle = workbook.createCellStyle();
        dataRowLeftBlueStyle.setFillForegroundColor(colorBlue);
        dataRowLeftBlueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftBlueStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTBLUESTYLE, dataRowLeftBlueStyle);

        XSSFCellStyle dataRowCenterBlueStyle = workbook.createCellStyle();
        dataRowCenterBlueStyle.setFillForegroundColor(colorBlue);
        dataRowCenterBlueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterBlueStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERBLUESTYLE, dataRowCenterBlueStyle);

        XSSFCellStyle dataRowRightBlueStyle = workbook.createCellStyle();
        dataRowRightBlueStyle.setFillForegroundColor(colorBlue);
        dataRowRightBlueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightBlueStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTBLUESTYLE, dataRowRightBlueStyle);

        return cellStyles;
    }

    private void setSheetWidths(XSSFSheet sheet, int algoLength) {
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
    }

    private void addStringCells(Row row, List<String> strings, CellStyle style) {
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(strings.get(i));
            cell.setCellStyle(style);
        }
    }

    private void addDataCells(Row row, HashStatusThree hashStatusThree, Map<String, CellStyle> cellStyles) {
        int colIndex = 0;
        Cell cell = null;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getStatus());
        if (NOTSYNCED.equals(hashStatusThree.getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERREDSTYLE));
        } else {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERGREENSTYLE));
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getLeft().getStatus());
        if (MATCH.equals(hashStatusThree.getLeft().getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERBLUESTYLE));
        } else if (NEWFILE.equals(hashStatusThree.getLeft().getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERGREENSTYLE));
        } else {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERREDSTYLE));
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getCenter().getStatus());
        if (MATCH.equals(hashStatusThree.getCenter().getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERBLUESTYLE));
        } else if (NEWFILE.equals(hashStatusThree.getCenter().getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERGREENSTYLE));
        } else {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERREDSTYLE));
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getRight().getStatus());
        if (MATCH.equals(hashStatusThree.getRight().getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERBLUESTYLE));
        } else if (NEWFILE.equals(hashStatusThree.getRight().getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERGREENSTYLE));
        } else {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERREDSTYLE));
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getHash() != null) {
            cell.setCellValue(hashStatusThree.getLeft().getHash());
        }
        cell.setCellStyle(cellStyles.get(DATAROWCENTERSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getSize() >= 0) {
            cell.setCellValue(hashStatusThree.getLeft().getSize());
        }
        cell.setCellStyle(cellStyles.get(DATAROWRIGHTSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getLeft().getLastModified()));
        }
        cell.setCellStyle(cellStyles.get(DATAROWCENTERSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getHash() != null) {
            cell.setCellValue(hashStatusThree.getCenter().getHash());
        }
        cell.setCellStyle(cellStyles.get(DATAROWCENTERSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getSize() >= 0) {
            cell.setCellValue(hashStatusThree.getCenter().getSize());
        }
        cell.setCellStyle(cellStyles.get(DATAROWRIGHTSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getCenter().getLastModified()));
        }
        cell.setCellStyle(cellStyles.get(DATAROWCENTERSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getHash() != null) {
            cell.setCellValue(hashStatusThree.getRight().getHash());
        }
        cell.setCellStyle(cellStyles.get(DATAROWCENTERSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getSize() >= 0) {
            cell.setCellValue(hashStatusThree.getRight().getSize());
        }
        cell.setCellStyle(cellStyles.get(DATAROWRIGHTSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getRight().getLastModified()));
        }
        cell.setCellStyle(cellStyles.get(DATAROWCENTERSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getFilename());
        cell.setCellStyle(cellStyles.get(DATAROWLEFTSTYLE));
    }
}
