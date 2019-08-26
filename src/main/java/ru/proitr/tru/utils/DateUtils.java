package ru.proitr.tru.utils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class DateUtils {
    public static final DateFormat df = new SimpleDateFormat("yyyyMMdd");

    public static Date getDateFromCalendar(XMLGregorianCalendar date) {
        if (date == null) {
            return null;
        }
        return date.toGregorianCalendar().getTime();
    }

    public static LocalDate getLocalDateFromCalendar(XMLGregorianCalendar date) {
        if (date == null) {
            return null;
        }
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    public static String getStringFromDate(Date date) {
        return date == null ? null : df.format(date);
    }
}
