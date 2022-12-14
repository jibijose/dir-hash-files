package com.jibi.file;

import static com.jibi.util.DateUtil.format;

import com.jibi.common.Algorithm;
import com.jibi.common.Constants;
import com.jibi.util.FileUtil;
import com.jibi.vo.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class FileInfoExcelWriter extends ExcelWriter {

    public FileInfoExcelWriter(String filename) {
        super(filename);
    }

    public void writeExcel(String excelPassword, Algorithm algoSelected, Collection<FileInfo> listFileInfos) {
        int algoLength = 20;
        String algoValue = "NA";
        if (algoSelected != null) {
            algoLength = algoSelected.getLength();
            algoValue = algoSelected.getValue();
        }
        listFileInfos = listFileInfos.stream()
                .sorted(Comparator.comparing(FileInfo::getFilename))
                .collect(Collectors.toList());

        try {
            XSSFWorkbook workbookTemplate = new XSSFWorkbook();
            SXSSFWorkbook workbook = new SXSSFWorkbook(workbookTemplate);
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("FileInfo");
            ((SXSSFSheet) sheet).setRandomAccessWindowSize(100);
            sheet.createFreezePane(0, 1);
            setSheetWidths(sheet, algoLength);
            setCellStyles(workbook);

            CellStyle topRowStyle = workbook.createCellStyle();
            Font fontTop = workbook.createFont();
            fontTop.setBold(true);
            topRowStyle.setFont(fontTop);
            topRowStyle.setAlignment(HorizontalAlignment.CENTER);
            topRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            topRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            topRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dataRowStyle = workbook.createCellStyle();
            Font fontData = workbook.createFont();
            dataRowStyle.setFont(fontData);

            Row headerRow = sheet.createRow(0);
            List<String> rowHeaders = Arrays.asList(algoValue, "Size", "Date Modified", "File Name");
            addStringCells(headerRow, rowHeaders, cellStyles.get(TOPROWSTYLE));

            AtomicInteger rowIndex = new AtomicInteger(1);
            AtomicInteger requiredFileNameWidth = new AtomicInteger(0);
            listFileInfos.stream().forEach(fileInfo -> {
                Row dataRow = sheet.createRow(rowIndex.getAndIncrement());
                addDataCells(dataRow, fileInfo);
                if (fileInfo.getFilename().length() > requiredFileNameWidth.get()) {
                    requiredFileNameWidth.set(fileInfo.getFilename().length());
                }
            });
            sheet.setColumnWidth(3, ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)) * 256);
            log.info("Fileinfo filename column width adjusted to {}", ((requiredFileNameWidth.get() + 3) > 255 ? 255 : (requiredFileNameWidth.get() + 3)));
            sheet.setAutoFilter(new CellRangeAddress(0, rowIndex.get() - 1, 0, rowHeaders.size() - 1));

            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            if (excelPassword != null) {
                FileUtil.setExcelPassword(filename, excelPassword);
            }
            fileOutputStream.close();
            workbook.close();
        } catch (Exception exception) {
            log.error("Excel writing error for file {}", filename, exception);
        }
    }

    protected void setSheetWidths(Sheet sheet, int algoLength) {
        sheet.createFreezePane(0, 1);
        sheet.setColumnWidth(0, (algoLength + 3) * 256);
        sheet.setColumnWidth(1, 16 * 256);
        sheet.setColumnWidth(2, 32 * 256);
        sheet.setColumnWidth(3, 64 * 256);
    }

    private void addStringCells(Row row, List<String> strings, CellStyle style) {
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(strings.get(i));
            cell.setCellStyle(style);
        }
    }

    private void addDataCells(Row row, FileInfo fileInfo) {
        int colIndex = 0;
        Cell cell = null;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(fileInfo.getHash());
        if (Constants.CORRUPTED.equals(fileInfo.getHash())) {
            cell.setCellStyle(cellStyles.get(DATAROWLEFTREDSTYLE));
        } else {
            cell.setCellStyle(cellStyles.get(DATAROWLEFTSTYLE));
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(fileInfo.getSize());
        cell.setCellStyle(cellStyles.get(DATAROWRIGHTSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(format(fileInfo.getLastModified()));
        cell.setCellStyle(cellStyles.get(DATAROWCENTERSTYLE));

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue(fileInfo.getFilename());
        cell.setCellStyle(cellStyles.get(DATAROWLEFTSTYLE));
    }

}
