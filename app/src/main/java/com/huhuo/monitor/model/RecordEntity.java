package com.huhuo.monitor.model;

public class RecordEntity {
    public String name;
    public String number;
    public int type;
    public long lDate;
    public long duration;
    public int _new;

    @Override
    public String toString() {
        return "RecordEntity [toString()=" + name + "," + number + "," + type + "," + lDate + ","
                + duration + "," + name + "," + "]";
    }
}