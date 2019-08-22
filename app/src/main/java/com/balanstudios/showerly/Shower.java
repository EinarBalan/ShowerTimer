package com.balanstudios.showerly;

import java.util.Date;

public class Shower {

    private long showerLengthMillis;
    private double showerLengthMinutes;
    private static double gallonsPerMinute = 2.1;
    private static double dollarsPerGallon = .0015;
    private double volume;
    private double cost;
    private DateHandler dateHandler = new DateHandler();
//    private String date;
//    private int dayOfMonth = 0;
//    private int monthNum = 0;
//    private String time;
    private boolean goalMet;

    public Shower(){
        showerLengthMillis = 0;
        showerLengthMinutes = 0;
        volume = 0;
        cost = 0;
//        date = "";
//        dayOfMonth = dateHandler.getDayOfMonth();
//        monthNum = dateHandler.getMonthCode();
//        time = "";
        goalMet = false;
    }

    public Shower(long showerLengthMillis, boolean goalMet){
        this.showerLengthMillis = showerLengthMillis;
        showerLengthMinutes = (double)showerLengthMillis / 1000 / 60;
        volume = calculateVolume(showerLengthMinutes);
        cost = calculateCost(volume);
//        dayOfMonth = dateHandler.getDayOfMonth();
//        monthNum = dateHandler.getMonthCode();
        this.goalMet = goalMet;
    }

    public Shower(long showerLengthMillis, String date, String time, boolean goalMet){
        this.showerLengthMillis = showerLengthMillis;
        showerLengthMinutes = (double)showerLengthMillis / 1000 / 60;
        volume = calculateVolume(showerLengthMinutes);
        cost = calculateCost(volume);
//        this.date = date;
//        dayOfMonth = dateHandler.getDayOfMonth();
//        monthNum = dateHandler.getMonthCode();
//        this.time = time;
        this.goalMet = goalMet;
    }

    public String toString(){
        String s = "Showered for " + showerLengthMinutes + " minutes on " + getDate() + " at " + getTime() + ". Volume: " + volume + " Cost: " + cost + " Goal Met: " + goalMet;
        return s;
    }



    public long getShowerLengthMillis() {
        return showerLengthMillis;
    }

    public void setShowerLengthMillis(long showerLengthMillis) {
        this.showerLengthMillis = showerLengthMillis;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public static double calculateVolume(double showerLengthMinutes){
        return showerLengthMinutes * gallonsPerMinute;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public static double calculateCost(double volume){
        return volume * dollarsPerGallon;
    }

    public String getDate() {
        return dateHandler.getDateString();
    }

    public String getTime() {
        return dateHandler.getTimeString();
    }


    public boolean isGoalMet() {
        return goalMet;
    }

    public void setGoalMet(boolean goalMet) {
        this.goalMet = goalMet;
    }

    public double getShowerLengthMinutes() {
        return showerLengthMinutes;
    }

    public void setShowerLengthMinutes(double showerLengthMinutes) {
        this.showerLengthMinutes = showerLengthMinutes;
    }

    public static double getGallonsPerMinute() {
        return gallonsPerMinute;
    }

    public static void setGallonsPerMinute(double gpm) {
        gallonsPerMinute = gpm;
    }

    public static double getDollarsPerGallon() {
        return dollarsPerGallon;
    }

    public static void setDollarsPerGallon(double dpg) {
        dollarsPerGallon = dpg;
    }

//    public int getDayOfMonth() {
//        return dayOfMonth;
//    }

//    public void setDayOfMonth(int dayOfMonth) {
//        this.dayOfMonth = dayOfMonth;
//    }

//    public int getMonthNum() {
//        return monthNum;
//    }

//    public void setMonthNum(int monthNum) {
//        this.monthNum = monthNum;
//    }

    public DateHandler getDateHandler() {
        return dateHandler;
    }
}
