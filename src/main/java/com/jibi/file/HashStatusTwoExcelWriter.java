package com.jibi.file;

import static com.jibi.util.DateUtil.format;
import static com.jibi.util.FileUtil.MATCH;
import static com.jibi.util.FileUtil.NEWFILE;

import com.jibi.common.Algorithm;
import com.jibi.util.FileUtil;
import com.jibi.vo.HashStatusTwo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HashStatusTwoExcelWriter extends ExcelWriter {

    public HashStatusTwoExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(String excelPassword, Algorithm algoSelected, final Map<String, HashStatusTwo> hashStatusMap) {
        int algoLength = 20;
        String algoValue = "NA";
        if (algoSelected != null) {
            algoLength = algoSelected.getLength();
            algoValue = algoSelected.getValue();
        }
        SortedMap<String, HashStatusTwo> sortedHashStatusMap = new TreeMap<>();
        sortedHashStatusMap.putAll(hashStatusMap);

        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("HashStatus");
            sheet.createFreezePane(0, 1);

            setSheetWidths(sheet, algoLength);
            setCellStyles(workbook);

            XSSFRow headerRow = sheet.createRow(0);
            addStringCells(headerRow, Arrays.asList("Status", "Left-Hash (" + algoValue + ")", "Left-Size", "Left-Modified",
                    "Right-Hash (" + algoValue + ")", "Right-Size", "Right-Modified", "filename"), cellStyles.get(TOPROWSTYLE));

            AtomicInteger rowIndex = new AtomicInteger(1);
            AtomicInteger requiredFileNameWidth = new AtomicInteger(0);
            sortedHashStatusMap.keySet().stream().forEach(hashStatus -> {
                XSSFRow dataRow = sheet.createRow(rowIndex.getAndIncrement());
                addDataCells(dataRow, sortedHashStatusMap.get(hashStatus));
                if (sortedHashStatusMap.get(hashStatus).getFilename().length() > requiredFileNameWidth.get()) {
                    requiredFileNameWidth.set(sortedHashStatusMap.get(hashStatus).getFilename().length());
                }
            });
            sheet.setColumnWidth(7, ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)) * 256);
            log.info("HashStatus filename column width adjusted to {}", ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)));
            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, sheet.getRow(0).getLastCellNum()));

            FileOutputStream fileStream = new FileOutputStream(filename);
            workbook.write(fileStream);
            if (excelPassword != null) {
                FileUtil.setExcelPassword(filename, excelPassword);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    protected void setSheetWidths(Sheet sheet, int algoLength) {
        sheet.setColumnWidth(0, 16 * 256);
        sheet.setColumnWidth(1, (algoLength + 3) * 256);
        sheet.setColumnWidth(2, 16 * 256);
        sheet.setColumnWidth(3, 32 * 256);
        sheet.setColumnWidth(4, (algoLength + 3) * 256);
        sheet.setColumnWidth(5, 16 * 256);
        sheet.setColumnWidth(6, 32 * 256);
        sheet.setColumnWidth(7, 128 * 256);
    }

    private void addStringCells(Row row, List<String> strings, CellStyle style) {
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(strings.get(i));
            cell.setCellStyle(style);
        }
    }

    private void addDataCells(Row row, HashStatusTwo hashStatusTwo) {
        int colIndex = 0;
        Cell cell = null;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusTwo.getStatus());
        if (MATCH.equals(hashStatusTwo.getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERBLUESTYLE));
        } else if (NEWFILE.equals(hashStatusTwo.getStatus())) {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERGREENSTYLE));
        } else {
            cell.setCellStyle(cellStyles.get(DATAROWCENTERREDSTYLE));
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getLeft().getHash() != null) {
            cell.setCellValue(hashStatusTwo.getLeft().getHash());
        }
        cell.setCellStyle(findHashCellStyle(hashStatusTwo));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getLeft().getSize() >= 0) {
            cell.setCellValue(hashStatusTwo.getLeft().getSize());
        }
        cell.setCellStyle(findSizeCellStyle(hashStatusTwo));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getLeft().getLastModified() != null) {
            cell.setCellValue(format(hashStatusTwo.getLeft().getLastModified()));
        }
        cell.setCellStyle(findLastModifiedCellStyle(hashStatusTwo));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getRight().getHash() != null) {
            cell.setCellValue(hashStatusTwo.getRight().getHash());
        }
        cell.setCellStyle(findHashCellStyle(hashStatusTwo));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getRight().getSize() >= 0) {
            cell.setCellValue(hashStatusTwo.getRight().getSize());
        }
        cell.setCellStyle(findSizeCellStyle(hashStatusTwo));

        cell = row.createCell(colIndex++, CellType.STRING);
        if (hashStatusTwo.getRight().getLastModified() != null) {
            cell.setCellValue(format(hashStatusTwo.getRight().getLastModified()));
        }
        cell.setCellStyle(findLastModifiedCellStyle(hashStatusTwo));

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(hashStatusTwo.getFilename());
        cell.setCellStyle(cellStyles.get(DATAROWLEFTSTYLE));
    }

    private CellStyle findHashCellStyle(HashStatusTwo hashStatusTwo) {
        if (hashStatusTwo.getLeft().getHash() == null || hashStatusTwo.getRight().getHash() == null) {
            return cellStyles.get(DATAROWLEFTSTYLE);
        } else if (hashStatusTwo.getLeft().getHash().equals(hashStatusTwo.getRight().getHash())) {
            return cellStyles.get(DATAROWLEFTSTYLE);
        } else {
            return cellStyles.get(DATAROWLEFTMAROONSTYLE);
        }
    }

    private CellStyle findSizeCellStyle(HashStatusTwo hashStatusTwo) {
        if (hashStatusTwo.getLeft().getSize() <= 0 || hashStatusTwo.getRight().getSize() <= 0) {
            return cellStyles.get(DATAROWRIGHTSTYLE);
        } else if (hashStatusTwo.getLeft().getSize() == hashStatusTwo.getRight().getSize()) {
            return cellStyles.get(DATAROWRIGHTSTYLE);
        } else {
            return cellStyles.get(DATAROWRIGHTMAROONSTYLE);
        }
    }

    private CellStyle findLastModifiedCellStyle(HashStatusTwo hashStatusTwo) {
        if (hashStatusTwo.getLeft().getLastModified() == null || hashStatusTwo.getRight().getLastModified() == null) {
            return cellStyles.get(DATAROWCENTERSTYLE);
        } else if (hashStatusTwo.getLeft().getLastModified().compareTo(hashStatusTwo.getRight().getLastModified()) == 0) {
            return cellStyles.get(DATAROWCENTERSTYLE);
        } else {
            return cellStyles.get(DATAROWCENTERMAROONSTYLE);
        }
    }
}
