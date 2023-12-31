package it.uniba.dib.sms232412.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;
    public DatePickerFragment(DatePickerDialog.OnDateSetListener listener){
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar mCalendar = Calendar.getInstance();

        int year = mCalendar.get(Calendar.YEAR);

        int month = mCalendar.get(Calendar.MONTH);

        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(

                getActivity(),

                listener,

                year, month, dayOfMonth);
    }
}
