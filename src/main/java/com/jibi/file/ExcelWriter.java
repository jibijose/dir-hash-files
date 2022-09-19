package com.jibi.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Map;

public abstract class ExcelWriter {

    public final static String TOPROWSTYLE = "TopRowStyle";
    public final static String DATAROWLEFTSTYLE = "DataRowLeftStyle";
    public final static String DATAROWCENTERSTYLE = "DataRowCenterStyle";
    public final static String DATAROWRIGHTSTYLE = "DataRowRightStyle";
    public final static String DATAROWLEFTREDSTYLE = "DataRowLeftRedStyle";
    public final static String DATAROWCENTERREDSTYLE = "DataRowCenterRedStyle";
    public final static String DATAROWRIGHTREDSTYLE = "DataRowRightRedStyle";

    public final static String DATAROWLEFTBLUESTYLE = "DataRowLeftBlueStyle";
    public final static String DATAROWCENTERBLUESTYLE = "DataRowCenterBlueStyle";
    public final static String DATAROWRIGHTBLUESTYLE = "DataRowRightBlueStyle";

    public final static String DATAROWLEFTGREENSTYLE = "DataRowLeftGreenStyle";
    public final static String DATAROWCENTERGREENSTYLE = "DataRowCenterGreenStyle";
    public final static String DATAROWRIGHTGREENSTYLE = "DataRowRightGreenStyle";

    public final static String DATAROWLEFTMAROONSTYLE = "DataRowLeftMaroonStyle";
    public final static String DATAROWCENTERMAROONSTYLE = "DataRowCenterMaroonStyle";
    public final static String DATAROWRIGHTMAROONSTYLE = "DataRowRightMaroonStyle";

    protected String filename;
    protected Map<String, CellStyle> cellStyles;

    public ExcelWriter(String filename) {
        this.filename = filename;
    }

    protected void setCellStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> cellStyles = new HashMap<>();
        int colorWeight = 255;
        int otherWeight = 50;
        XSSFColor colorRed = new XSSFColor(new java.awt.Color(colorWeight, otherWeight, otherWeight), null);
        XSSFColor colorGreen = new XSSFColor(new java.awt.Color(otherWeight, colorWeight, otherWeight), null);
        XSSFColor colorBlue = new XSSFColor(new java.awt.Color(otherWeight, otherWeight, colorWeight), null);
        XSSFColor colorMaroon = new XSSFColor(new java.awt.Color(255, 127, 127), null);

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

        XSSFCellStyle dataRowLeftMaroonStyle = workbook.createCellStyle();
        dataRowLeftMaroonStyle.setFillForegroundColor(colorMaroon);
        dataRowLeftMaroonStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftMaroonStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTMAROONSTYLE, dataRowLeftMaroonStyle);

        XSSFCellStyle dataRowCenterMaroonStyle = workbook.createCellStyle();
        dataRowCenterMaroonStyle.setFillForegroundColor(colorMaroon);
        dataRowCenterMaroonStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterMaroonStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERMAROONSTYLE, dataRowCenterMaroonStyle);

        XSSFCellStyle dataRowRightMaroonStyle = workbook.createCellStyle();
        dataRowRightMaroonStyle.setFillForegroundColor(colorMaroon);
        dataRowRightMaroonStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightMaroonStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTMAROONSTYLE, dataRowRightMaroonStyle);

        this.cellStyles = cellStyles;
    }

    protected abstract void setSheetWidths(XSSFSheet sheet, int algoLength);
}
