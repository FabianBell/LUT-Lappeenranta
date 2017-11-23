package com.fabianbell.janinakeller.lut_lappeenranta.listener;

/**
 * Created by Fabian on 22.11.2017.
 */

public class BetterDay {

    private int year;
    private int month;
    private int day;

    public BetterDay(int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public static BetterDay parse(String source){
        String[] splited = source.split("\\.");
        int[] parsed = {Integer.parseInt(splited[0]), Integer.parseInt(splited[1]), Integer.parseInt(splited[2])};
        return new BetterDay(parsed[0], parsed[1], parsed[2]);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BetterDay){
            BetterDay day = (BetterDay) obj;
            return ((day.getYear() == this.year) && (day.getMonth() == this.month) && (day.getDay() == this.day));
        }else{
            return false;
        }
    }

    public boolean before(BetterDay dayAfter){
        if(this.year < dayAfter.getYear()){
            return true;
        }else{
            if(this.year > dayAfter.getYear()){
                return false;
            }else {
                if (this.month < dayAfter.getMonth()) {
                    return true;
                } else {
                    if (this.month > dayAfter.getMonth()) {
                        return false;
                    } else {
                        if (this.day < dayAfter.getDay()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
    }

    public BetterDay diff(BetterDay oldDay){
        if (this.equals(oldDay)){
            return new BetterDay(0,0,0);
        }
        if (this.before(oldDay)){
            return null;
        }
        int diffYear = this.year - oldDay.year;
        int diffMonth = this.month - oldDay.month;
        int diffDay = this.day - oldDay.day;

        if (diffDay < 0){
            diffDay = 30 - diffDay;
            diffMonth -= 1;
        }
        if (diffMonth < 0){
            diffMonth = 12 - diffMonth;
            diffYear -= 1;
        }
        return new BetterDay(diffDay, diffMonth, diffYear);
    }

    public String print(){
        return this.year + "." + this.month + "." + this.day;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getTotalDays(){
        long days = ((long) this.year) * 365 + this.month * 30 + this.day;
        return days;
    }
}
