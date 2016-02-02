package com.huhuo.monitor.model;

/**
 * Created by xiejc on 15/12/4.
 */
public class MonitorModel {

    private String id;
    private long time;
    private int status = -1;
    private String content;
    private String name;
    private String detail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "MonitorModel{" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", status=" + status +
                ", content='" + content + '\'' +
                ", name='" + name + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
