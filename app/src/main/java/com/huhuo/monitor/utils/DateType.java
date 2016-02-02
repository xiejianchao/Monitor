package com.huhuo.monitor.utils;


import com.huhuo.monitor.MonitorApplication;
import com.huhuo.monitor.R;

public enum DateType {


	BEFORE(0, MonitorApplication.getInstance().getApplicationContext().getString(R.string.common_before_yesterday_2)),
	YESTERDAY(1, MonitorApplication.getInstance().getApplicationContext().getString(R.string.common_yesterday)),
	TODAY(2, MonitorApplication.getInstance().getApplicationContext().getString(R.string.common_today)),
	TOMORROW(3, MonitorApplication.getInstance().getApplicationContext().getString(R.string.common_tomorrow));
		
	private int value;
	private String text;
	
	private DateType(int value, String label) {
		this.value = value;
		this.text = label;
	}
	
	public Integer getValue() {
		return value;
	}

	public String getLabel() {
		return text;
	}
		
}