package com.nexis.ExcelReports;

import android.content.Context;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.UIDialog;
import com.parse.ParseObject;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class WeeklyReport {

    Context context;
    String filePath;

    int categoryListSize, nexcellListSize, columnsNum;

    List<String> nexcellList;
    List<ParseObject> nexcellObject;

    List<DateTime> dateList = new ArrayList<>();
    List<List<Integer>> mainList = new ArrayList<>();
    List<List<Integer>> excelDataList = new ArrayList<>();

    HSSFWorkbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet();

    public WeeklyReport(Context actv, String path)
    {
        context = actv;
        filePath = path;
    }

    public void genReport()
    {
        initializeDateList();

        initialize();
        parseData();

        if (mainList.size() == 0) UIDialog.onCreateInvalidDialog(context, "No data are found! Report cannot be generated");

        reorderRawData();
        populateSheet();
    }

    private void initialize()
    {
        nexcellList =  new ArrayList<>(Data.NEXCELL_LIST);
        nexcellList.addAll(Constants.NEXCELL_CATEGORY_LIST);

        categoryListSize = Constants.CATEGORY_LIST.size();
        nexcellListSize = nexcellList.size();
        columnsNum = categoryListSize * nexcellListSize;
    }

    private void initializeDateList()
    {
        DateTime sDate = new DateTime(Constants.NEXIS_START_DATE);
        sDate = sDate.withZone(DateTimeZone.UTC).withDayOfWeek(DateTimeConstants.FRIDAY);

        while(sDate.isBefore(DateTime.now()))
        {
            dateList.add(sDate);
            sDate = sDate.plusWeeks(1);
        }
    }

    private void parseData()
    {
        for(DateTime i: dateList)
        {
            List<Integer> row = new ArrayList<>();
            List<Integer> hsRow = Arrays.asList(0, 0, 0, 0);
            List<Integer> uniRow = Arrays.asList(0, 0, 0, 0);
            List<Integer> nexisRow = Arrays.asList(0, 0, 0, 0);

            for(String j: Data.NEXCELL_ACTIVE_LIST)
            {
                for (int k = 0 ; k < Constants.CATEGORY_LIST.size(); k++)
                {
                    nexcellObject = ParseOperation.getNexcellData(j, Constants.CATEGORY_LIST.get(k), i, true, context);
                    int members = nexcellObject.size();

                    if (Data.getNexcellStage(j).equals(Constants.HS_STRING)) hsRow.set(k, hsRow.get(k) + members);
                    else uniRow.set(k, uniRow.get(k) + members);

                    nexisRow.set(k, nexisRow.get(k) + members);
                    row.add(members);
                }
            }

            row.addAll(hsRow);
            row.addAll(uniRow);
            row.addAll(nexisRow);
            mainList.add(row);
        }
    }

    private void reorderRawData()
    {
        for(int i = 0; i < dateList.size(); i++)
        {
            List<Integer> subDataList = new ArrayList<>();

            double excelDate = DateUtil.getExcelDate(dateList.get(i).toCalendar(Locale.CANADA), false);

            subDataList.add((int) Math.floor(excelDate)); //Add date to the row

            subDataList.addAll(mainList.get(i)); //Append the entire row to subDataList

            excelDataList.add(subDataList);
        }
    }

	private void populateSheet()
	{
        try
        {
            wb.setSheetName(wb.getSheetIndex(sheet), "Attendance Summary");
			FileOutputStream fileOut = new FileOutputStream(new File(filePath));
			
			//FONT STYLE
			Font whiteFont12 = wb.createFont();
			whiteFont12.setFontHeightInPoints((short)12);
			whiteFont12.setColor(IndexedColors.WHITE.index);

			Font Font11 = wb.createFont();
			Font11.setFontHeightInPoints((short)11);

			Font Font10 = wb.createFont();
			Font10.setFontHeightInPoints((short)10);

			Font whiteBoldFont12 = wb.createFont();
			whiteBoldFont12.setFontHeightInPoints((short)12);
			whiteBoldFont12.setColor(IndexedColors.WHITE.index);
			whiteBoldFont12.setBoldweight(Font.BOLDWEIGHT_BOLD);
			
			HSSFPalette palette = wb.getCustomPalette();
			palette.setColorAtIndex(HSSFColor.BLUE_GREY.index, (byte) 55, (byte) 96, (byte) 145);
			palette.setColorAtIndex(HSSFColor.BLUE.index, (byte) 83, (byte) 142, (byte) 213);
			palette.setColorAtIndex(HSSFColor.ORANGE.index, (byte) 228, (byte) 109, (byte) 10);
			palette.setColorAtIndex(HSSFColor.LIGHT_ORANGE.index, (byte) 252, (byte) 213, (byte) 180);
			palette.setColorAtIndex(HSSFColor.TURQUOISE.index, (byte) 182, (byte) 221, (byte) 232);
			palette.setColorAtIndex(HSSFColor.LIGHT_TURQUOISE.index, (byte) 219, (byte) 229, (byte) 241);
			palette.setColorAtIndex(HSSFColor.PLUM.index, (byte) 204, (byte) 192, (byte) 218);
			
			//Table Title Cell Style
			CellStyle tableTitleStyle = wb.createCellStyle();
			tableTitleStyle.setFillForegroundColor(HSSFColor.BLACK.index);
			
			//Nexcell DARK BLUE Title Cell Style
			CellStyle nexcellDarkBlueStyle = wb.createCellStyle();
			nexcellDarkBlueStyle.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
			
			//Nexcell LIGHT BLUE Title Cell Style
			CellStyle nexcellLightBlueStyle = wb.createCellStyle();
			nexcellLightBlueStyle.setFillForegroundColor(HSSFColor.BLUE.index);
			
			//Nexcell ORANGE Title Cell Style
			CellStyle nexcellOrangeStyle = wb.createCellStyle();
			nexcellOrangeStyle.setFillForegroundColor(HSSFColor.ORANGE.index);
			
			//Nexcell RED Title Cell Style
			CellStyle nexcellRedStyle = wb.createCellStyle();
			nexcellRedStyle.setFillForegroundColor(HSSFColor.RED.index);
			
			//fellowship Title Cell Style
			CellStyle fellowshipCellStyle = wb.createCellStyle();
			fellowshipCellStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
			
			//service Title Cell Style
			CellStyle serviceCellStyle = wb.createCellStyle();
			serviceCellStyle.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
			
			//college Title Cell Style
			CellStyle collegeCellStyle = wb.createCellStyle();
			collegeCellStyle.setFillForegroundColor(HSSFColor.TURQUOISE.index);
			
			//newComer Title Cell Style
			CellStyle newComerCellStyle = wb.createCellStyle();
			newComerCellStyle.setFillForegroundColor(HSSFColor.PLUM.index);
			
			//Date Title Cell Style
			CellStyle dateTitleStyle = wb.createCellStyle();
			dateTitleStyle.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
			
			//Date Cell Style
			CellStyle dateStyle = wb.createCellStyle();
			dateStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
			
			//Data BLUE Cell Style
			CellStyle dataBlueStyle = wb.createCellStyle();
			dataBlueStyle.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
			
			//Data Cell Style
			CellStyle dataStyle = wb.createCellStyle();
			dataStyle.setFillForegroundColor(IndexedColors.WHITE.index);
			
			List<CellStyle> allCellStyles = Arrays.asList(tableTitleStyle, dateTitleStyle, nexcellDarkBlueStyle, nexcellRedStyle, nexcellOrangeStyle, nexcellLightBlueStyle,
														  collegeCellStyle, serviceCellStyle, fellowshipCellStyle, newComerCellStyle, dateTitleStyle, dateStyle, dataBlueStyle, dataStyle);
			
			List<CellStyle> nexcellStyles = Arrays.asList(nexcellDarkBlueStyle, nexcellRedStyle, nexcellOrangeStyle, nexcellDarkBlueStyle, nexcellLightBlueStyle);
			
			List<CellStyle> categoryStyles = Arrays.asList(collegeCellStyle, serviceCellStyle, fellowshipCellStyle, newComerCellStyle);
			
			for(CellStyle cs: allCellStyles)
			{
				cs.setFont(Font11);
				cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
				cs.setAlignment(CellStyle.ALIGN_CENTER);

				cs.setBorderLeft(CellStyle.BORDER_THIN);
				cs.setBorderLeft(CellStyle.BORDER_THIN);
				cs.setBorderBottom(CellStyle.BORDER_THIN);
				cs.setBorderTop(CellStyle.BORDER_THIN);
			}
			
			tableTitleStyle.setBorderBottom(CellStyle.BORDER_NONE);
			tableTitleStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			tableTitleStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
			
			for(CellStyle cs: nexcellStyles)
			{
				cs.setFont(whiteFont12);
				cs.setBorderTop(CellStyle.BORDER_NONE);
				cs.setBorderLeft(CellStyle.BORDER_MEDIUM);
				cs.setBorderRight(CellStyle.BORDER_MEDIUM);
			}
			
			for(CellStyle cs: categoryStyles)
			{
				cs.setFont(Font10);
			}
			
			tableTitleStyle.setFont(whiteBoldFont12);
			
			newComerCellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			fellowshipCellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
			dateStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			
			dateTitleStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			dateTitleStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
			dateTitleStyle.setFont(whiteFont12);
			
			CreationHelper createHelper = wb.getCreationHelper();
			dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("MMM-dd"));
			
			Row titleRow = sheet.createRow(0);
			Cell titleCell = titleRow.createCell(1);
			titleCell.setCellStyle(tableTitleStyle);

			titleCell.setCellValue("Nexis Attendance Summary");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, columnsNum));

            Row nexcellRow = sheet.createRow(1);

            Row categoryRow = sheet.createRow(2);
            Cell dateTitleCell = categoryRow.createCell(0);

            dateTitleCell.setCellValue("Date");
            dateTitleCell.setCellStyle(dateTitleStyle);

            for(int i = 0; i < nexcellListSize; i++)
            {
                String nexcellName = nexcellList.get(i);

                Cell nCell = nexcellRow.createCell(1 + i * categoryListSize);
                nCell.setCellValue(nexcellName);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, (1 + i * categoryListSize), (i + 1) * categoryListSize));

                //Assign Nexcell level title cell style
                if (nexcellName == "HighSchool" || nexcellName == "University") nCell.setCellStyle(nexcellOrangeStyle);
                else if (nexcellName == "Nexis") nCell.setCellStyle(nexcellRedStyle);
                else if (i%2 == 0) nCell.setCellStyle(nexcellDarkBlueStyle);
                else nCell.setCellStyle(nexcellLightBlueStyle);

                for(int j = 0; j < categoryListSize; j++)
                {
                    Cell cCell = categoryRow.createCell(1 + i * categoryListSize + j);
                    cCell.setCellValue(Constants.CATEGORY_LIST.get(j));

                    //Assign category level title cell style
                    if (j == 3) cCell.setCellStyle(newComerCellStyle);
                    else if (j == 2) cCell.setCellStyle(collegeCellStyle);
                    else if (j == 1) cCell.setCellStyle(serviceCellStyle);
                    else cCell.setCellStyle(fellowshipCellStyle);
                }
			}
		
			int numTitleRows = 3;
			
			for(int i = 0; i < excelDataList.size(); i++)
			{
				Row dataRow = sheet.createRow(i + numTitleRows);
				List<Integer> subList = excelDataList.get(i);
				
				for(int j = 0; j < subList.size(); j++)
				{
					Cell dataCell = dataRow.createCell(j);
					dataCell.setCellValue(subList.get(j));
					
					CellStyle currentStyle = wb.createCellStyle();
					
					//Assign data level cell style
					if (j == 0) currentStyle.cloneStyleFrom(dateStyle);
					else if (i%2 == 0) currentStyle.cloneStyleFrom(dataStyle);
					else currentStyle.cloneStyleFrom(dataBlueStyle);

					//data level cell borders
					if ((j-1)%4 == 0) currentStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
					else if (j%4 == 0) currentStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
					
					if (i == excelDataList.size() - 1) currentStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
					
					dataCell.setCellStyle(currentStyle);
				}
			}
			
		    wb.write(fileOut);
		    fileOut.close();
		}
		catch(Exception e)
		{
			UIDialog.onCreateErrorDialog(context, e.toString());
		}
	}
}
