package com.nexis.AttendanceView;

public class AttendanceItem {
    private int fInt;
    private int sInt;
    private int cInt;
    private int nInt;


    public AttendanceItem(int fT, int sT, int cT, int nT) {
        fInt = fT;
        sInt = sT;
        cInt = cT;
        nInt = nT;
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
}