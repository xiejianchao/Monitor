package com.huhuo.monitor.ui.fragment;

import android.view.View;

import com.huhuo.monitor.R;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.MonitorModel;
import com.huhuo.monitor.net.WebSocketClient;
import com.huhuo.monitor.ui.activity.MainActivity;
import com.huhuo.monitor.utils.DateUtil;
import com.huhuo.monitor.utils.Logger;


public class AlarmFragment extends BaseMonitorFragment {

    private static final String TAG = AlarmFragment.class.getSimpleName();

    @Override
    protected void init() {
        updateTitle(R.string.monitor_alarm);

        updateTips("暂未收到警报上位机");

        String lastTime = showMainActivityLastConntime();
        Logger.d(TAG, "init lastConnTime:" + lastTime);
        if (WebSocketClient.SOCKET_STATUS == Constants.OPEN
                || WebSocketClient.SOCKET_STATUS == Constants.RECEIVE_MESSAGE) {
            progressBar.setVisibility(View.GONE);
            updateTips(R.string.tips_connect_success_alarm_wait);
            updateNetTips(true);
        } else {
            updateNetTips(false);
        }
    }

    private String showMainActivityLastConntime(){
        if (getActivity() instanceof  MainActivity) {
            MainActivity ac = (MainActivity) getActivity();
            if (ac != null) {
                long lastConnTime = ac.getLastConnTime();
                String formatConnTime = DateUtil.getFormatTime(lastConnTime, DateUtil.PATTERN_STANDARD);
                return formatConnTime;
            }
        }

        return null;
    }

    @Override
    public void onStatusMonitor(MonitorModel model) {
        super.onStatusMonitor(model);
        /**
         * 检查服务器传递过来的上位机是否在当前页面列表中，如果在，直接从当前列表中删除
         * 所以，就相当于执行了删除操作
         */
        deleteMonitor(model);
        updateAlarmTabCount();
    }

    @Override
    public void onDeleteMonitor(MonitorModel model) {
        super.onDeleteMonitor(model);
        deleteMonitor(model);
        updateAlarmTabCount();
    }

    @Override
    public void onOpen() {
        showProgressBar(false);
        updateTips(R.string.tips_connect_success_alarm_wait);
        closeRefresh();
        updateAlarmTabCount();
    }

    @Override
    public void onReceive(MonitorModel model) {
        super.onReceive(model);
        String lastTime = showMainActivityLastConntime();
        Logger.d(TAG, "onReceive lastConnTime:" + lastTime);
        Logger.d(TAG, "接收到警报数据：" + model.toString());
        adapter.addOrRefesh(model);
        if (adapter.getItemCount() > 0) {
            showTipsLayout(false);
        } else {
            showTipsLayout(true);
        }
        updateAlarmTabCount();
    }

    protected void updateAlarmTabCount() {
        if (getActivity() instanceof MainActivity) {
            MainActivity ac = (MainActivity) getActivity();
            ac.updateAlarmTabCount(adapter.getItemCount());
        }
    }

    private void updateAlarmTabCountX(){
        try {
            if (this.isAdded()) {
                Logger.d(TAG, "fragment attached to activity，立即再设置标题");
                if (adapter.getItemCount() > 0) {
                    updateTitle(getString(R.string.monitor_alarm_args,adapter.getItemCount()));
                } else {
                    updateTitle(getString(R.string.monitor_alarm));
                }
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            if (adapter.getItemCount() > 0) {
                                updateTitle(getString(R.string.monitor_alarm_args,adapter.getItemCount()));
                            } else {
                                updateTitle(getString(R.string.monitor_alarm));
                            }
                        }
                    }
                }
                        ,2000);
                Logger.d(TAG, "fragment not attached to activity，稍后再设置标题");
            }
        } catch (Exception e) {
            Logger.e(TAG,"onReceive",e);
        }
    }
}
