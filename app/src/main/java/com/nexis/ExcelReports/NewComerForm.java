package com.nexis.ExcelReports;

import android.content.Context;
import com.nexis.UIDialog;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class NewComerForm {

    Context context;
    String filePath;
    List<String> labelList, dataList;

    HSSFWorkbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet();

    public NewComerForm(Context actv, String path, List<String> lbList, List<String> ioList)
    {
        context = actv;
        filePath = path;
        labelList = lbList;
        dataList = ioList;
    }

    public void genReport()
    {
        populateSheet();
    }

	private void populateSheet() {

        try {
            wb.setSheetName(wb.getSheetIndex(sheet), "New Comer");
            FileOutputStream fileOut = new FileOutputStream(new File(filePath));

            Row titleRow = sheet.createRow(0);
            Row dataRow = sheet.createRow(1);

            for (int i = 0; i < labelList.size(); i++) {
                Cell labelCell = titleRow.createCell(i);
                labelCell.setCellValue(labelList.get(i));

                Cell dataCell = dataRow.createCell(i);
                dataCell.setCellValue(dataList.get(i));
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
