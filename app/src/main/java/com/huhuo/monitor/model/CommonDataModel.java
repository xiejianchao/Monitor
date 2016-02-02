package com.huhuo.monitor.model;

import java.util.List;

/**
 * Created by xiejc on 16/1/24.
 */
public class CommonDataModel extends BaseJsonModel {

    private MonitorModel data;

    public MonitorModel getData() {
        return data;
    }

    public void setData(MonitorModel data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommonDataModel{" +
                "data=" + data +
                '}';
    }
}
