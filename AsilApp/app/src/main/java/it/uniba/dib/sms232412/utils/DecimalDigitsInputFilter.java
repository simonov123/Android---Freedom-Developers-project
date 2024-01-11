package it.uniba.dib.sms232412.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {
    private Pattern mPattern;
    public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero) + "}+((\\.[0-9]{0," + (digitsAfterZero) + "})?)||(\\.)?");
    }
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String originalText = dest.toString();
        String newText = originalText.substring(0, dstart) + source.subSequence(start, end) + originalText.substring(dend);
        Matcher matcher = mPattern.matcher(newText);
        if (!matcher.matches())
            return "";
        if (source.equals(".") && digitsAfterDot(newText) == 0) {
            return ".";
        }
        return null;
    }

    private int digitsAfterDot(String text) {
        int dotIndex = text.indexOf(".");
        if (dotIndex != -1) {
            return text.length() - dotIndex - 1;
        }
        return 0;
    }
}
