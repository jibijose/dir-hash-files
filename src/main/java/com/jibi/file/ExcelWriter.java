package com.jibi.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

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

    protected void setCellStyles(Workbook workbook) {
        Map<String, CellStyle> cellStyles = new HashMap<>();
        int colorWeight = 255;
        int otherWeight = 50;
        Color colorRed = new XSSFColor(new java.awt.Color(colorWeight, otherWeight, otherWeight), null);
        Color colorGreen = new XSSFColor(new java.awt.Color(otherWeight, colorWeight, otherWeight), null);
        Color colorBlue = new XSSFColor(new java.awt.Color(otherWeight, otherWeight, colorWeight), null);
        Color colorMaroon = new XSSFColor(new java.awt.Color(255, 127, 127), null);

        // Create a header row describing what the columns mean
        CellStyle topRowStyle = workbook.createCellStyle();
        Font fontTop = workbook.createFont();
        fontTop.setBold(true);
        topRowStyle.setFont(fontTop);
        topRowStyle.setAlignment(HorizontalAlignment.CENTER);
        topRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        topRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        topRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyles.put(TOPROWSTYLE, topRowStyle);

        CellStyle dataRowLeftStyle = workbook.createCellStyle();
        dataRowLeftStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTSTYLE, dataRowLeftStyle);

        CellStyle dataRowCenterStyle = workbook.createCellStyle();
        dataRowCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERSTYLE, dataRowCenterStyle);

        CellStyle dataRowRightStyle = workbook.createCellStyle();
        dataRowRightStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTSTYLE, dataRowRightStyle);

        CellStyle dataRowLeftRedStyle = workbook.createCellStyle();
        dataRowLeftRedStyle.setFillForegroundColor(colorRed);
        dataRowLeftRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftRedStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTREDSTYLE, dataRowLeftRedStyle);

        CellStyle dataRowCenterRedStyle = workbook.createCellStyle();
        dataRowCenterRedStyle.setFillForegroundColor(colorRed);
        dataRowCenterRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterRedStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERREDSTYLE, dataRowCenterRedStyle);

        CellStyle dataRowRightRedStyle = workbook.createCellStyle();
        dataRowRightRedStyle.setFillForegroundColor(colorRed);
        dataRowRightRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightRedStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTREDSTYLE, dataRowRightRedStyle);

        CellStyle dataRowLeftGreenStyle = workbook.createCellStyle();
        dataRowLeftGreenStyle.setFillForegroundColor(colorGreen);
        dataRowLeftGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftGreenStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTGREENSTYLE, dataRowLeftGreenStyle);

        CellStyle dataRowCenterGreenStyle = workbook.createCellStyle();
        dataRowCenterGreenStyle.setFillForegroundColor(colorGreen);
        dataRowCenterGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterGreenStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERGREENSTYLE, dataRowCenterGreenStyle);

        CellStyle dataRowRightGreenStyle = workbook.createCellStyle();
        dataRowRightGreenStyle.setFillForegroundColor(colorGreen);
        dataRowRightGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightGreenStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTGREENSTYLE, dataRowRightGreenStyle);

        CellStyle dataRowLeftBlueStyle = workbook.createCellStyle();
        dataRowLeftBlueStyle.setFillForegroundColor(colorBlue);
        dataRowLeftBlueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftBlueStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTBLUESTYLE, dataRowLeftBlueStyle);

        CellStyle dataRowCenterBlueStyle = workbook.createCellStyle();
        dataRowCenterBlueStyle.setFillForegroundColor(colorBlue);
        dataRowCenterBlueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterBlueStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERBLUESTYLE, dataRowCenterBlueStyle);

        CellStyle dataRowRightBlueStyle = workbook.createCellStyle();
        dataRowRightBlueStyle.setFillForegroundColor(colorBlue);
        dataRowRightBlueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightBlueStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTBLUESTYLE, dataRowRightBlueStyle);

        CellStyle dataRowLeftMaroonStyle = workbook.createCellStyle();
        dataRowLeftMaroonStyle.setFillForegroundColor(colorMaroon);
        dataRowLeftMaroonStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowLeftMaroonStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put(DATAROWLEFTMAROONSTYLE, dataRowLeftMaroonStyle);

        CellStyle dataRowCenterMaroonStyle = workbook.createCellStyle();
        dataRowCenterMaroonStyle.setFillForegroundColor(colorMaroon);
        dataRowCenterMaroonStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowCenterMaroonStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put(DATAROWCENTERMAROONSTYLE, dataRowCenterMaroonStyle);

        CellStyle dataRowRightMaroonStyle = workbook.createCellStyle();
        dataRowRightMaroonStyle.setFillForegroundColor(colorMaroon);
        dataRowRightMaroonStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataRowRightMaroonStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put(DATAROWRIGHTMAROONSTYLE, dataRowRightMaroonStyle);

        this.cellStyles = cellStyles;
    }

    protected abstract void setSheetWidths(Sheet sheet, int algoLength);
}
