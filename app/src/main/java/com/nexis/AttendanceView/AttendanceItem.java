package com.nexis.AttendanceView;

import android.view.View;

import org.joda.time.DateTime;

import java.util.List;

public class AttendanceItem {

    private int fInt;
    private int sInt;
    private int cInt;
    private int nInt;

    private DateTime date;
    private View.OnClickListener buttonListener;

    public AttendanceItem(List<Integer> dataPoints, DateTime dt, View.OnClickListener listener) {

        fInt = dataPoints.get(0);
        sInt = dataPoints.get(1);
        cInt = dataPoints.get(2);
        nInt = dataPoints.get(3);
        date = dt;
        buttonListener = listener;
    }

    public Integer getFellowshipValue() {
        return fInt;
    }

    public Integer getServiceValue() {
        return sInt;
    }

    public Integer getCollegeValue() {
        return cInt;
    }

    public Integer getNewComerValue() {
        return nInt;
    }

    public String getFellowshipText() {
        return Integer.toString(fInt);
    }

    public String getServiceText() {
        return Integer.toString(sInt);
    }

    public String getCollegeText() {
        return Integer.toString(cInt);
    }

    public String getNewComerText() {
        return Integer.toString(nInt);
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