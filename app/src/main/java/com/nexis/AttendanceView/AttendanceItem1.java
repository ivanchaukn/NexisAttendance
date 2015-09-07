package com.nexis.AttendanceView;

import android.support.v4.util.ArrayMap;
import android.view.View;
import com.nexis.Constants;
import org.joda.time.DateTime;
import java.util.HashMap;
import java.util.List;

public class AttendanceItem1 {

    private ArrayMap<String, List<String>> rawData;
    private HashMap<String, Integer> atdNum;

    private DateTime date;
    private View.OnClickListener buttonListener;

    public AttendanceItem1(ArrayMap<String, List<String>> dataPoints, DateTime dt, View.OnClickListener listener) {
        rawData = dataPoints;
        atdNum = new HashMap<>();
        aggregateData(dataPoints);

        date = dt;
        buttonListener = listener;
    }

    private void aggregateData(ArrayMap<String, List<String>> data)
    {
        for (int i = 0; i < Constants.CATEGORY_LIST.size(); i++)
        {
            List<String> members = data.get(Constants.CATEGORY_LIST.get(i));
            atdNum.put(Constants.CATEGORY_LIST.get(i), members.size());
        }
    }

    public String getFellowshipText() {
        return Integer.toString(atdNum.get("Fellowship"));
    }

    public String getServiceText() {
        return Integer.toString(atdNum.get("Service"));
    }

    public String getCollegeText() {
        return Integer.toString(atdNum.get("College"));
    }

    public String getNewComerText() {
        return Integer.toString(atdNum.get("NewComer"));
    }

    public String getDateText() {
        return date.toString("MMM dd");
    }

    public DateTime getDate() {
        return date;
    }

    public String getYearText() {
        return date.toString("YYYY");
    }

    public View.OnClickListener getListener() {
        return buttonListener;
    }
}