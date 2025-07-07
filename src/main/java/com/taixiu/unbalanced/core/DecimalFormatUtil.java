package com.taixiu.unbalanced.core;

import lombok.Getter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecimalFormatUtil {
    private static final DecimalFormat df;

    static {
        df = new DecimalFormat();
        DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(Locale.getDefault());
        newSymbols.setDecimalSeparator(',');
        newSymbols.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(newSymbols);
    }

    public static String format(long value) {
        return df.format(value);
    }

    public static String format(long value, GroupingSeparator separator) {
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setGroupingSeparator(separator.getSeparator());
        format.setDecimalFormatSymbols(formatSymbols);
        return format.format(value);
    }

    @Getter
    public enum GroupingSeparator {
        COMMA(','), DOT('.');

        private final char separator;

        GroupingSeparator(char separator) {
            this.separator = separator;
        }

    }
}