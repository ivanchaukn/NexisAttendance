package com.nexis.AttendanceView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nexis.NexisApplication;
import com.nexis.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private View v;

    private List<AttendanceItem> mData;
    private int mSelectedPosition;
    private int mTouchedPosition = -1;

    public boolean devVal = NexisApplication.getDev();
    public boolean commiVal = NexisApplication.getCommi();

    public AttendanceAdapter(List<AttendanceItem> data) {
        mData = data;
    }

    @Override
    public AttendanceAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attendance_card, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AttendanceAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.fText.setText(mData.get(i).getFellowshipText());
        viewHolder.sText.setText(mData.get(i).getServiceText());
        viewHolder.cText.setText(mData.get(i).getCollegeText());
        viewHolder.nText.setText(mData.get(i).getNewComerText());
        viewHolder.dateText.setText(mData.get(i).getDateText());
        viewHolder.yearText.setText(mData.get(i).getYearText());

        DateTime nextDate = new DateTime(DateTimeZone.UTC);
        nextDate = nextDate.minusWeeks(1).withTimeAtStartOfDay();

        if (devVal||commiVal||mData.get(i).getDate().isAfter(nextDate)) {
            viewHolder.editButton.setBackgroundResource(R.drawable.ic_action_edit);
            viewHolder.editButton.setOnClickListener(mData.get(i).getListener());
        } else {
            viewHolder.editButton.setBackgroundResource(R.drawable.ic_action_edit_disabled);
        }

        viewHolder.editButton.setOnTouchListener(new View.OnTouchListener() {
                                                     @Override
                                                     public boolean onTouch(View v, MotionEvent event) {
                                                        touchPosition(i);
                                                        return false;
                                                     }
                                                 }
        );

        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
                                                   @Override
                                                   public boolean onTouch(View v, MotionEvent event) {

                                                       switch (event.getAction()) {
                                                           case MotionEvent.ACTION_DOWN:
                                                               touchPosition(i);
                                                               return false;
                                                           case MotionEvent.ACTION_CANCEL:
                                                               touchPosition(-1);
                                                               return false;
                                                           case MotionEvent.ACTION_MOVE:
                                                               return false;
                                                           case MotionEvent.ACTION_UP:
                                                               touchPosition(-1);
                                                               return false;
                                                       }
                                                       return true;
                                                   }
                                               }
        );
    }

    private void touchPosition(int position) {
        int lastPosition = mTouchedPosition;
        mTouchedPosition = position;
        if (lastPosition >= 0)
            notifyItemChanged(lastPosition);
        if (position >= 0)
            notifyItemChanged(position);
    }

    public void selectPosition(int position) {
        int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(lastPosition);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public int getmTouchedPosition()
    {
        return mTouchedPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fText;
        public TextView sText;
        public TextView cText;
        public TextView nText;
        public TextView dateText;
        public TextView yearText;

        public ImageButton editButton;

        public ViewHolder(View itemView) {
            super(itemView);
            fText = (TextView) itemView.findViewById(R.id.fellowshipText);
            sText = (TextView) itemView.findViewById(R.id.serviceText);
            cText = (TextView) itemView.findViewById(R.id.collegeText);
            nText = (TextView) itemView.findViewById(R.id.newComerText);
            dateText = (TextView) itemView.findViewById(R.id.cardDateText);
            yearText = (TextView) itemView.findViewById(R.id.cardYearText);

            editButton = (ImageButton) itemView.findViewById(R.id.cardEditButton);
        }
    }
}
