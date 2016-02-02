package com.huhuo.monitor.model;

import java.util.List;

/**
 * Created by xiejc on 16/1/24.
 */
public class ListDataModel extends BaseJsonModel {

    private List<MonitorModel> data;

    public void setData(List<MonitorModel> data) {
        this.data = data;
    }

    public List<MonitorModel> getData() {
        return data;
    }


    @Override
    public String toString() {
        return "ListDataModel{" +
                "data=" + data +
                '}';
    }
}
