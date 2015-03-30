package com.nexis.Fragments.AttendancePackage;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.nexis.AttendanceView.AttendanceItem;
import com.nexis.Constants;
import com.nexis.R;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SubmitDialog extends DialogFragment {

    private View rootView;
    private int fellNum, servNum, collNum, newcNum = 0;

    public static SubmitDialog newInstance(LayoutInflater inflater, List<Integer> item) {
        SubmitDialog fragment = new SubmitDialog();
        if (item != null) fragment.updateValue(item);
        fragment.onCreateView(inflater, null, null);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.attendance_dialog, container, false);

        NumberPicker npF = (NumberPicker) rootView.findViewById(R.id.fellowshipNumberPicker);
        NumberPicker npC = (NumberPicker) rootView.findViewById(R.id.serviceNumberPicker);
        NumberPicker npS = (NumberPicker) rootView.findViewById(R.id.collegeNumberPicker);
        NumberPicker npN = (NumberPicker) rootView.findViewById(R.id.newcomerNumberPicker);

        initializeNumberPickers(npF);
        initializeNumberPickers(npC);

        initializeNumberPickers(npS);
        initializeNumberPickers(npN);

        setPickerValue();

        return rootView;
    }

    private void updateValue(List<Integer> item)
    {
        fellNum = item.get(0);
        servNum = item.get(1);
        collNum = item.get(2);
        newcNum = item.get(3);
    }

    private void initializeNumberPickers(NumberPicker np)
    {
        np.setMaxValue(Constants.NUMBERPICKER_MAX);
        np.setMinValue(Constants.NUMBERPICKER_MIN);
        np.setOnValueChangedListener(valueChangeListener);
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        setNumberPickerTextColor(np, Color.BLACK);
    }

    private void setPickerValue()
    {
        ((NumberPicker) rootView.findViewById(R.id.fellowshipNumberPicker)).setValue(fellNum);
        ((NumberPicker) rootView.findViewById(R.id.serviceNumberPicker)).setValue(servNum);
        ((NumberPicker) rootView.findViewById(R.id.collegeNumberPicker)).setValue(collNum);
        ((NumberPicker) rootView.findViewById(R.id.newcomerNumberPicker)).setValue(newcNum);
    }

    private OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {

        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            if (picker.getId() == R.id.fellowshipNumberPicker) fellNum = picker.getValue();
            if (picker.getId() == R.id.collegeNumberPicker) collNum = picker.getValue();
            if (picker.getId() == R.id.serviceNumberPicker) servNum = picker.getValue();
            if (picker.getId() == R.id.newcomerNumberPicker) newcNum = picker.getValue();
        }
    };

    private static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNPTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNPTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNPTextColor", e);
                }
            }
        }
        return false;
    }

    public View getView()
    {
        return rootView;
    }

    public List<Integer> getData()
    {
        return Arrays.asList(fellNum, servNum, collNum, newcNum);
    }
}
