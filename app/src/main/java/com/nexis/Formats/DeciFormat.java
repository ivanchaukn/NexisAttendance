package com.nexis.Formats;

import com.github.mikephil.charting.utils.ValueFormatter;
import java.text.DecimalFormat;

public final class DeciFormat implements ValueFormatter {

    private DecimalFormat mFormat;

    public DeciFormat() {
        mFormat = new DecimalFormat("###,###,##0");
    }

    @Override
    public String getFormattedValue(float value) {
        return mFormat.format(value);
    }
}