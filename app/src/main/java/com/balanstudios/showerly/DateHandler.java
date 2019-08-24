package com.balanstudios.showerly;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nonnull;

class DateHandler {

    private Calendar cal;
    private Date date;
    private static SimpleDateFormat dateFormatDayOfWeek = new SimpleDateFormat("EEE");
    private static SimpleDateFormat dateFormatMonthShort = new SimpleDateFormat("MMM");
    private static SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy");
    private static SimpleDateFormat dateFormatCondensed = new SimpleDateFormat("M/d");
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("h:mm a");
    private static SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");

    //current date information
    static String currentDateString = dateFormat.format(Calendar.getInstance().getTime());

    static String getCurrentDateCondensedString = dateFormatCondensed.format(Calendar.getInstance().getTime());

    static String currentMonthStringShort = dateFormatMonthShort.format(Calendar.getInstance().getTime());

    static String currentMonthString = dateFormatMonth.format(Calendar.getInstance().getTime());

    static String currentYearString = dateFormatYear.format(Calendar.getInstance().getTime());

    static String currentDayOfWeek = dateFormatDayOfWeek.format(Calendar.getInstance().getTime());

    static int currentMonthCode = Calendar.getInstance().get(Calendar.MONTH) + 1;

    static int currentDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

    static int currentYear = Calendar.getInstance().get(Calendar.YEAR);

    static int currentWeekCode = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);

    static ArrayList<String> dayNames = new ArrayList<>(Arrays.asList( "Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"));

    static ArrayList<String> hours = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"));

    static ArrayList<String> hoursReadable = new ArrayList<>(Arrays.asList("12 AM", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12 PM", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00"));

    static ArrayList<String> monthNames = new ArrayList<>(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));

    static ArrayList<String> monthNamesReadable = new ArrayList<>(Arrays.asList("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"));


    //instance date information
    private int dayOfMonth;

    private int dayOfWeekCode;

    private String dayOfWeekString;

    private int dayOfYear;

    private int monthCode;

    private String monthString;

    private int year;

    private int weekCode;

    private String dateString;

    private int timeHour;

    private String timeString;


    //date bounds information
    static int daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
    static int daysInYear = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR);

    DateHandler() {
        cal = Calendar.getInstance();
        date = cal.getTime();
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        dayOfWeekCode = cal.get(Calendar.DAY_OF_WEEK);
        dayOfWeekString = dateFormatDayOfWeek.format(date);
        dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        monthCode = cal.get(Calendar.MONTH) + 1; //starts at 0 so + 1 is necessary
        monthString = dateFormatMonthShort.format(date);
        year = cal.get(Calendar.YEAR);
        weekCode = cal.get(Calendar.WEEK_OF_YEAR);
        dateString = dateFormat.format(date);
        timeHour = cal.get(Calendar.HOUR_OF_DAY);
        timeString = dateTimeFormat.format(date);
    }

    boolean isInDay() {
        return (dayOfYear == currentDayOfYear && isInYear());
    }

    boolean isInDay(DateHandler dateHandler) {
        return (dayOfYear == dateHandler.dayOfYear && isInYear(dateHandler));
    }

    boolean isInWeek() {
        return (weekCode == currentWeekCode && isInYear());
    }

    boolean isInWeek(DateHandler weekDateHandler) {
        return (weekCode == weekDateHandler.weekCode && isInYear(weekDateHandler));
    }

    boolean isInMonth() {
        return (monthCode == currentMonthCode && isInYear());
    }

    boolean isInMonth(DateHandler monthDateHandler) {
        return (monthCode == monthDateHandler.monthCode && isInYear(monthDateHandler));
    }

    boolean isInYear() {
        return year == currentYear;
    }

    boolean isInYear(DateHandler yearDateHandler) {
        return year == yearDateHandler.year;
    }

    @Nonnull
    public String toString() {
        return "Date Handler: " + getDateString() + " " + getTimeString();
    }


    //getters and setters
    Date getDate() {
        return date;
    }

    int getDayOfMonth() {
        return dayOfMonth;
    }

    int getDayOfWeekCode() {
        return dayOfWeekCode;
    }

    String getDayOfWeekString() {
        return dayOfWeekString;
    }

    int getMonthCode() {
        return monthCode;
    }

    String getMonthString() {
        return monthString;
    }

    int getYear() {
        return year;
    }

    int getWeekCode() {
        return weekCode;
    }

    int getDaysInMonth() {
        return daysInMonth;
    }

    int getDaysInYear() {
        return daysInYear;
    }

    String getDateString() {
        return dateString;
    }

    String getTimeString() {
        return timeString;
    }

    int getTimeHour() {
        return timeHour;
    }
}
