package com.nexis.ExcelReports;

import android.content.Context;
import com.nexis.UIDialog;
import com.parse.ParseObject;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ContactForm {

    Context context;
    String filePath;
    List<String> titleList;
    List<ParseObject> userList;

    HSSFWorkbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet();

    public ContactForm(Context actv, String path, List<ParseObject> users)
    {
        context = actv;
        filePath = path;
        userList = users;
        titleList = Arrays.asList("First Name", "Last Name",  "Gender", "Birthday", "Email", "Phone",
                                  "School", "School Year", "Christian", "Year Accepted", "Baptized",
                                  "Year Baptized");
    }

    public void genReport()
    {
        populateSheet();
    }

    private String convertFieldName(String title)
    {
        String[] parsed = title.split(" ");

        String newString;
        if (parsed.length > 1) newString = parsed[0].toLowerCase() + parsed[1];
        else newString = title.toLowerCase();

        return newString;
    }

	private void populateSheet() {

        try {
            wb.setSheetName(wb.getSheetIndex(sheet), "Contact List");
            FileOutputStream fileOut = new FileOutputStream(new File(filePath));

            HSSFPalette palette = wb.getCustomPalette();
            palette.setColorAtIndex(HSSFColor.BLUE.index, (byte) 83, (byte) 142, (byte) 213);

            Font BoldFont10 = wb.createFont();
            BoldFont10.setFontHeightInPoints((short) 10);
            BoldFont10.setBoldweight(Font.BOLDWEIGHT_BOLD);

            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFillForegroundColor(HSSFColor.BLUE.index);
            titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
            titleStyle.setFont(BoldFont10);
            titleStyle.setBorderBottom(CellStyle.BORDER_THICK);

            Row titleRow = sheet.createRow(0);
            for (int i = 0; i < titleList.size(); i++) {
                Cell labelCell = titleRow.createCell(i);
                labelCell.setCellValue(titleList.get(i));
                labelCell.setCellStyle(titleStyle);
            }

            for (int j = 0; j < userList.size(); j++)
            {
                Row userRow = sheet.createRow(j + 1);

                ParseObject user = userList.get(j);

                for(int k = 0; k < titleList.size(); k++)
                {
                    CellStyle dataStyle = wb.createCellStyle();
                    dataStyle.setAlignment(CellStyle.ALIGN_CENTER);

                    Cell dataCell = userRow.createCell(k);

                    if (titleList.get(k).equals("Birthday")) {
                        CreationHelper createHelper = wb.getCreationHelper();
                        dataStyle.setDataFormat(createHelper.createDataFormat().getFormat("MMM-dd-yyyy"));

                        DateTime date = new DateTime((user.get(convertFieldName(titleList.get(k)))));
                        date = date.toDateTime(DateTimeZone.UTC).withTimeAtStartOfDay();
                        dataCell.setCellValue(date.toCalendar(Locale.CANADA));
                    } else {
                        dataCell.setCellValue((String)user.get(convertFieldName(titleList.get(k))));
                    }

                    dataCell.setCellStyle(dataStyle);
                }
            }

            wb.write(fileOut);
            fileOut.close();
        }
        catch (Exception e)
        {
            UIDialog.onCreateErrorDialog(context, e.toString());
        }
    }
}
