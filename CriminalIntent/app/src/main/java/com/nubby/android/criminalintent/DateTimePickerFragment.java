package com.nubby.android.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DateTimePickerFragment extends DialogFragment {

    private static final String ARG_DATE = "date";
    public static final String EXTRA_DATE = "com.nubby.android.criminalintent.date";
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;

    public static DateTimePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        DateTimePickerFragment fragment = new DateTimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int requestCode = getTargetRequestCode();
        View v;

        AlertDialog.Builder alertBuilder =
                new AlertDialog.Builder(getActivity()).setTitle(R.string.date_picker_title);

        if (requestCode == RequestCodes.REQUEST_DATE) {
            v  = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);
            mDatePicker = v.findViewById(R.id.crime_date_picker);
            mDatePicker.init(year, month, day, null);
            return alertBuilder
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int year = mDatePicker.getYear();
                            int month = mDatePicker.getMonth();
                            int day = mDatePicker.getDayOfMonth();
                            Date date = new GregorianCalendar(year, month, day, hour, minute).getTime();
                            sendResult(Activity.RESULT_OK, date);
                        }
                    })
                    .setView(v)
                    .create();

        } else if (requestCode == RequestCodes.REQUEST_TIME) {
            v  = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);
            mTimePicker = v.findViewById(R.id.crime_time_picker);
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
            return alertBuilder
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int hour = mTimePicker.getCurrentHour();
                            int minute = mTimePicker.getCurrentMinute();
                            Date date = new GregorianCalendar(year, month, day, hour, minute).getTime();
                            sendResult(Activity.RESULT_OK, date);
                        }
                    })
                    .setView(v)
                    .create();
        } else {
            return alertBuilder.create();
        }
    }

    private void sendResult(int result, Date date) {
        if (getTargetFragment() == null) return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);
        getTargetFragment().onActivityResult(getTargetRequestCode(), result, intent);
    }
}
