package com.jibi.file;

import com.jibi.common.Algorithm;
import com.jibi.util.FileUtil;
import com.jibi.vo.HashStatusThree;
import com.jibi.vo.OneSide;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jibi.util.DateUtil.format;
import static com.jibi.util.FileUtil.*;

@Slf4j
public class HashStatusThreeExcelWriter extends ExcelWriter {

    public HashStatusThreeExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(String excelPassword, Algorithm algoSelected, Map<String, HashStatusThree> hashStatusMap) {
        int algoLength = 20;
        String algoValue = "NA";
        if (algoSelected != null) {
            algoLength = algoSelected.getLength();
            algoValue = algoSelected.getValue();
        }
        SortedMap<String, HashStatusThree> sortedHashStatusMap = new TreeMap<>();
        sortedHashStatusMap.putAll(hashStatusMap);

        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("HashStatus");
            sheet.createFreezePane(0, 1);

            setSheetWidths(sheet, algoLength);
            setCellStyles(workbook);

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
                addDataCells(dataRow, sortedHashStatusMap.get(hashStatus));
                if (sortedHashStatusMap.get(hashStatus).getFilename().length() > requiredFileNameWidth.get()) {
                    requiredFileNameWidth.set(sortedHashStatusMap.get(hashStatus).getFilename().length());
                }
            });
            sheet.setColumnWidth(13, ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)) * 256);
            log.info("HashStatus filename column width adjusted to {}", ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)));
            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, sheet.getRow(0).getLastCellNum()));
            Files.isWritable(Paths.get(filename));
            FileOutputStream fileStream = new FileOutputStream(filename);
            workbook.write(fileStream);
            if (excelPassword != null) {
                FileUtil.setExcelPassword(filename, excelPassword);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    protected void setSheetWidths(XSSFSheet sheet, int algoLength) {
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

    private void addDataCells(Row row, HashStatusThree hashStatusThree) {
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
        cell.setCellStyle(findHashCellStyle(hashStatusThree.getLeft(), hashStatusThree.getCenter(), hashStatusThree.getRight()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getSize() >= 0) {
            cell.setCellValue(hashStatusThree.getLeft().getSize());
        }
        cell.setCellStyle(findSizeCellStyle(hashStatusThree.getLeft(), hashStatusThree.getCenter(), hashStatusThree.getRight()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getLeft().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getLeft().getLastModified()));
        }
        cell.setCellStyle(findLastModifiedCellStyle(hashStatusThree.getLeft(), hashStatusThree.getCenter(), hashStatusThree.getRight()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getHash() != null) {
            cell.setCellValue(hashStatusThree.getCenter().getHash());
        }
        cell.setCellStyle(findHashCellStyle(hashStatusThree.getCenter(), hashStatusThree.getRight(), hashStatusThree.getLeft()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getSize() >= 0) {
            cell.setCellValue(hashStatusThree.getCenter().getSize());
        }
        cell.setCellStyle(findSizeCellStyle(hashStatusThree.getCenter(), hashStatusThree.getRight(), hashStatusThree.getLeft()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getCenter().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getCenter().getLastModified()));
        }
        cell.setCellStyle(findLastModifiedCellStyle(hashStatusThree.getCenter(), hashStatusThree.getRight(), hashStatusThree.getLeft()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getHash() != null) {
            cell.setCellValue(hashStatusThree.getRight().getHash());
        }
        cell.setCellStyle(findHashCellStyle(hashStatusThree.getRight(), hashStatusThree.getLeft(), hashStatusThree.getCenter()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getSize() >= 0) {
            cell.setCellValue(hashStatusThree.getRight().getSize());
        }
        cell.setCellStyle(findSizeCellStyle(hashStatusThree.getRight(), hashStatusThree.getLeft(), hashStatusThree.getCenter()));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusThree.getRight().getLastModified() != null) {
            cell.setCellValue(format(hashStatusThree.getRight().getLastModified()));
        }
        cell.setCellStyle(findLastModifiedCellStyle(hashStatusThree.getRight(), hashStatusThree.getLeft(), hashStatusThree.getCenter()));

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusThree.getFilename());
        cell.setCellStyle(cellStyles.get(DATAROWLEFTSTYLE));
    }

    private CellStyle findHashCellStyle(OneSide leftSide, OneSide centerSide, OneSide rightSide) {
        if (NEWFILE.equals(leftSide.getStatus())) {
            return cellStyles.get(DATAROWLEFTSTYLE);
        } else {
            if (leftSide.getHash() != null && (leftSide.getHash().equals(centerSide.getHash()) && leftSide.getHash().equals(rightSide.getHash()))) {
                return cellStyles.get(DATAROWLEFTSTYLE);
            } else if (leftSide.getHash() != null && (leftSide.getHash().equals(centerSide.getHash()) || leftSide.getHash().equals(rightSide.getHash()))) {
                return cellStyles.get(DATAROWLEFTSTYLE);
            } else if (centerSide.getHash() != null && centerSide.getHash().equals(rightSide.getHash())) {
                return cellStyles.get(DATAROWLEFTMAROONSTYLE);
            } else {
                return cellStyles.get(DATAROWLEFTMAROONSTYLE);
            }
        }
    }

    private CellStyle findSizeCellStyle(OneSide leftSide, OneSide centerSide, OneSide rightSide) {
        if (NEWFILE.equals(leftSide.getStatus())) {
            return cellStyles.get(DATAROWRIGHTSTYLE);
        } else {
            if (leftSide.getSize() >= 0 && (leftSide.getSize() == centerSide.getSize() && leftSide.getSize() == rightSide.getSize())) {
                return cellStyles.get(DATAROWRIGHTSTYLE);
            } else if (leftSide.getSize() >= 0 && (leftSide.getSize() == centerSide.getSize() || leftSide.getSize() == rightSide.getSize())) {
                return cellStyles.get(DATAROWRIGHTSTYLE);
            } else if (centerSide.getSize() >= 0 && centerSide.getSize() == rightSide.getSize()) {
                return cellStyles.get(DATAROWRIGHTMAROONSTYLE);
            } else {
                return cellStyles.get(DATAROWRIGHTMAROONSTYLE);
            }
        }
    }

    private CellStyle findLastModifiedCellStyle(OneSide leftSide, OneSide centerSide, OneSide rightSide) {
        if (NEWFILE.equals(leftSide.getStatus())) {
            return cellStyles.get(DATAROWCENTERSTYLE);
        } else {
            if (leftSide.getLastModified() != null && centerSide.getLastModified() != null && rightSide.getLastModified() != null
                    && (leftSide.getLastModified().compareTo(centerSide.getLastModified()) == 0 && leftSide.getLastModified().compareTo(rightSide.getLastModified()) == 0)) {
                return cellStyles.get(DATAROWCENTERSTYLE);
            } else if ((leftSide.getLastModified() != null && centerSide.getLastModified() != null && leftSide.getLastModified().compareTo(centerSide.getLastModified()) == 0)
                    || (leftSide.getLastModified() != null && rightSide.getLastModified() != null && leftSide.getLastModified().compareTo(rightSide.getLastModified()) == 0)) {
                return cellStyles.get(DATAROWCENTERSTYLE);
            } else if (centerSide.getLastModified() != null && rightSide.getLastModified() != null
                    && centerSide.getLastModified().compareTo(rightSide.getLastModified()) == 0) {
                return cellStyles.get(DATAROWCENTERMAROONSTYLE);
            } else {
                return cellStyles.get(DATAROWCENTERMAROONSTYLE);
            }
        }
    }
}
