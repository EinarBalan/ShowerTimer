package com.balanstudios.showerly;

import android.util.Log;

import com.google.android.gms.common.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class ShowerDataOrganizer {
    private Map<String, Double> dayMap = new HashMap<>(); // String is hour, Double is total shower timer per hour
    private Map<String, Double> weekMap = new HashMap<>(); // String is day name, Double is total shower timer per day
    private Map<String, Double> monthMap = new HashMap<>(); // String is date "m/d", Double is total shower timer per day
    private Map<String, Double> yearMap = new HashMap<>(); // String is month name, Double is total shower timer per month

    private ArrayList<Shower> userShowers;

    ShowerDataOrganizer() {

    }

    ShowerDataOrganizer(ArrayList<Shower> userShowers) {
        this.userShowers = userShowers;
        populate();
    }

    private void populateDayMap() {
        Double[] showerLengthsPerDay = new Double[24];
        Arrays.fill(showerLengthsPerDay, 0.0);

        for (Shower s: userShowers) {
            if (s.getDateHandler().isInDay()) {
                showerLengthsPerDay[s.getDateHandler().getTimeHour()] += s.getShowerLengthMinutes(); //add to total shower length at the days index in the array if in current week
            }
        }

        for (int i = 0; i < DateHandler.hours.size(); i++ ) {
            dayMap.put(DateHandler.hours.get(i), showerLengthsPerDay[i]);
        }
    }

    private void populateWeekMap() {
        Double[] showerLengthsPerDay = new Double[7];
        Arrays.fill(showerLengthsPerDay, 0.0);

        for (Shower s: userShowers) {
            if (s.getDateHandler().isInWeek()) {
                showerLengthsPerDay[s.getDateHandler().getDayOfWeekCode() - 1] += s.getShowerLengthMinutes(); //add to total shower length at the days index in the array if in current week
            }
        }

        for (int i = 0; i < DateHandler.dayNames.size(); i++ ) {
            weekMap.put(DateHandler.dayNames.get(i), showerLengthsPerDay[i]);
        }
    }

    private void populateMonthMap() {
        Double[] showerLengthsPerDay = new Double[DateHandler.daysInMonth];
        Arrays.fill(showerLengthsPerDay, 0.0);

        for (Shower s: userShowers) {
            if (s.getDateHandler().isInMonth()) {
                showerLengthsPerDay[s.getDateHandler().getDayOfMonth() - 1] += s.getShowerLengthMinutes(); //add to total shower length at the days index in the array if in current week
            }
        }

        for (int day = 1; day <= DateHandler.daysInMonth; day++ ) {
            monthMap.put(DateHandler.currentMonthCode + "/" + day, showerLengthsPerDay[day - 1]);
        }
    }

    private void populateYearMap() {
        ArrayList<ArrayList<Double>> monthData = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            monthData.add(new ArrayList<Double>());
        }

        for (Shower s: userShowers) { //sort showers into separate month data to prepare for average
            if (s.getDateHandler().isInYear()) {
                int monthIndex = s.getDateHandler().getMonthCode() - 1;
                ArrayList<Double> singleMonthData = monthData.get(monthIndex);
                singleMonthData.add(s.getShowerLengthMinutes());
            }
        }

        for (int i = 0; i < DateHandler.monthNames.size(); i++) { //put average lengths into map
            yearMap.put(DateHandler.monthNames.get(i), getAverage(monthData.get(i)));
        }

    }

    void populate() {
        populateDayMap();
        populateWeekMap();
        populateMonthMap();
        populateYearMap();
    }

    Map<String, Double> getDayMap() {
        return dayMap;
    }

    Map<String, Double> getWeekMap() {
        return weekMap;
    }

    Map<String, Double> getMonthMap() {
        return monthMap;
    }

    Map<String, Double> getYearMap() {
        return yearMap;
    }

    boolean shouldShowDayChart() {
        int dayCount = 0;
        for (Map.Entry<String, Double> entry: dayMap.entrySet()) {
            if (entry.getValue() > 0) {
                dayCount++;
            }
        }
        return dayCount > 1;
    }

    boolean shouldShowWeekChart() {
        int weekCount = 0;
        for (Map.Entry<String, Double> entry: weekMap.entrySet()) {
            if (entry.getValue() > 0) {
                weekCount++;
            }
        }
        return weekCount > 0;
    }

    boolean shouldShowMonthChart() {
        int monthCount = 0;
        for (Map.Entry<String, Double> entry: monthMap.entrySet()) {
            if (entry.getValue() > 0) {
                monthCount++;
            }
        }
        return monthCount > 0;
    }

    boolean shouldShowYearChart() {
        int yearCount = 0;
        for (Map.Entry<String, Double> entry: yearMap.entrySet()) {
            if (entry.getValue() > 0) {
                yearCount++;
            }
        }
        return yearCount > 0;
    }

    Double getAverage(ArrayList<Double> arrayList) {
        if (arrayList.size() > 0) {
            double sum = 0;
            for (double num : arrayList) {
                sum += num;
            }
            return (sum / arrayList.size());
        }
        else {
            return 0.0;
        }
    }

    void setUserShowers(ArrayList<Shower> userShowers) {
        this.userShowers = userShowers;
    }
}
