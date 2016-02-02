package com.huhuo.monitor.model;

/**
 * Created by xiejc on 16/1/24.
 */
public class BaseJsonModel {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BaseJsonModel{" +
                "type='" + type + '\'' +
                '}';
    }
}
