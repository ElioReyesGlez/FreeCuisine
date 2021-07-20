package com.erg.freecuisine.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;


import com.erg.freecuisine.R;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeHelper {

    public static final int TIME_OUT =  30000;
    public static final int DIALOG_DELAY =  2500;
    public static final int DELAY =  7000;

    public static long getDifferenceInMinutes(long date1, long date2) {
        Date past = new Date();
        past.setTime(date1);
        Date today = new Date();
        today.setTime(date2);
        return Minutes.minutesBetween(new DateTime(past), new DateTime(today)).getMinutes();
    }

    public static long getDifferenceInDays(long pastLong, long todayLong) {
        Date past = new Date();
        past.setTime(pastLong);

        Date today = new Date();
        today.setTime(todayLong);


        return Days.daysBetween(new DateTime(past), new DateTime(today)).getDays();
    }

    public static int getWeekNumber(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }


    public static void setLocale(Activity context, String langCode) {
        Locale locale = new Locale(langCode);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        Locale.setDefault(locale);
        config.setLocale(locale);
        context.getBaseContext().getResources().updateConfiguration(config,
                context.getBaseContext().getResources().getDisplayMetrics());
    }

    public static String getDisplayableTime(Context context, long date)
    {
        long difference;
        long currentTime = System.currentTimeMillis();

        if(currentTime > date)
        {
            difference = currentTime - date;
            final long seconds = difference/1000;
            final long minutes = seconds/60;
            final long hours = minutes/60;
            final long days = hours/24;
//            final long months = days/31;
//            final long years = days/365;

            if (seconds < 0)
            {
                return "not yet";
            }
            else if (seconds < 60)
            {
                return seconds <= 1 ? context.getString(R.string.one_second_ago)
                        : seconds + context.getString(R.string.seconds_ago);
            }
            else if (seconds < 120)
            {
                return context.getString(R.string.a_minute_ago);
            }
            else if (seconds < 2700) // 45 * 60
            {
                return minutes + context.getString(R.string.minutes_ago);
            }
            else if (seconds < 5400) // 90 * 60
            {
                return context.getString(R.string.an_hour_ago);
            }
            else if (seconds < 86400) // 24 * 60 * 60
            {
                return hours + context.getString(R.string.hours_ago);
            }
            else if (seconds < 172800) // 48 * 60 * 60
            {
                return context.getString(R.string.yesterday);
            }
            else if (seconds < 2592000) // 30 * 24 * 60 * 60
            {
                return days + context.getString(R.string.days_ago);
            }
//            else if (seconds < 31104000) // 12 * 30 * 24 * 60 * 60
//            {
//
//                return months <= 1 ? "one month ago" : days + " months ago";
//            }
//            else
//            {
//
//                return years <= 1 ? "one year ago" : years + " years ago";
//            }
        }
        return dateFormatterShort(date);
    }


    public static String dateFormatterMedium(Long dateLong) {
        Date date = new Date(dateLong);
        Locale locale = Locale.getDefault();
        DateFormat dateFormat = DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
        return dateFormat.format(date);
    }

    public static String dateFormatterShort(Long dateLong) {
        Date date = new Date(dateLong);
        Locale locale = Locale.getDefault();
        DateFormat dateFormat = DateFormat
                .getDateInstance(DateFormat.MEDIUM, locale);
        return dateFormat.format(date);
    }

    public static String getUntil(Date untilDate) {
        Locale locale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat = new
                SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", locale);
        Calendar calendar = Calendar.getInstance();

        // where untilDate is a date instance of your choice,for example 30/01/2012
        calendar.setTime(untilDate);
        // if you want the event until 30/01/2012 we add one day from our day
        // because UNTIL in RRule sets events Before the last day want for event
        calendar.add(Calendar.DATE, 1);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static ArrayList<String> getCurrentWeekDays() {

        Locale locale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE", locale);

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        ArrayList<String> days = new ArrayList<>();
        for (int i = 0; i < 7; i++)
        {
            days.add(simpleDateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    public static int getCurrentDayOfWeekInNumber() {
        Locale locale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE", locale);

        Calendar calendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 1; i <= 7; i++)
        {
            if (simpleDateFormat.format(calendar.getTime())
                    .equals(simpleDateFormat.format(currentCalendar.getTime()))) {
                return i;
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return 0;
    }

    public static int getDayOfTheYear() {
        Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
        localCalendar.setTimeInMillis(System.currentTimeMillis());
        return localCalendar.get(Calendar.DAY_OF_YEAR);
    }
}
