package com.huhuo.monitor.ui.fragment;

import android.view.View;

import com.huhuo.monitor.R;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.MonitorModel;
import com.huhuo.monitor.net.WebSocketClient;
import com.huhuo.monitor.ui.activity.MainActivity;
import com.huhuo.monitor.utils.Logger;

import org.xutils.view.annotation.ContentView;


@ContentView(R.layout.fragment_monitor)
public class OfflineFragment extends BaseMonitorFragment {

    private static final String TAG = OfflineFragment.class.getSimpleName();

    @Override
    protected void init() {
        updateTitle(R.string.monitor_offline);

        updateTips("暂未收到离线上位机");

        if (WebSocketClient.SOCKET_STATUS == Constants.OPEN
                || WebSocketClient.SOCKET_STATUS == Constants.RECEIVE_MESSAGE) {
            progressBar.setVisibility(View.GONE);
            updateTips(R.string.tips_connect_success_offline_wait);
            updateNetTips(true);
        } else {
            updateNetTips(false);
        }
    }

    @Override
    public void onStatusMonitor(MonitorModel model) {
        super.onStatusMonitor(model);
        /**
         * 检查服务器传递过来的上位机是否在当前页面列表中，如果在，直接从当前列表中删除
         * 所以，就相当于执行了删除操作
         */
        deleteMonitor(model);
        updateOfflineTabCount();

        autoShowTipsLayout();
    }

    @Override
    public void onDeleteMonitor(MonitorModel model) {
        super.onDeleteMonitor(model);
        deleteMonitor(model);
        updateOfflineTabCount();

        Logger.d(TAG,"adapter.getItemCount()" + adapter.getItemCount());
//        if (adapter.getItemCount() > 0) {
//            showTipsLayout(false);
//        } else {
//            showTipsLayout(true);
//        }
        autoShowTipsLayout();
    }



    @Override
    public void onOpen() {
        showProgressBar(false);
        updateTips(R.string.tips_connect_success_offline_wait);
        closeRefresh();
        updateOfflineTabCount();

        autoShowTipsLayout();
    }

    @Override
    public void onReceive(MonitorModel model) {
        super.onReceive(model);
        Logger.d(TAG, "接收到离线数据：" + model.toString());

        deleteExceptionByXML(model);

        adapter.addOrRefesh(model);

//        if (adapter.getItemCount() > 0) {
//            showTipsLayout(false);
//        } else {
//            showTipsLayout(true);
//        }

        autoShowTipsLayout();
        Logger.d(TAG, "adapter item size：" + adapter.getItemCount());
        updateOfflineTabCount();
    }

    private void autoShowTipsLayout() {
        if (adapter.getItemCount() > 0) {
            showTipsLayout(false);
        } else {
            showTipsLayout(true);
        }
    }

    protected void updateOfflineTabCount() {
        if (getActivity() instanceof MainActivity) {
            MainActivity ac = (MainActivity) getActivity();
            ac.updateOfflineTabCount(adapter.getItemCount());
        }
    }

    private void updateOfflineTabCountX(){
        try {
            if (this.isAdded()) {
                Logger.d(TAG, "fragment attached to activity，立即再设置标题");
                if (adapter.getItemCount() > 0) {
                    updateTitle(getString(R.string.monitor_offline_args,adapter.getItemCount()));
                } else {
                    updateTitle(getString(R.string.monitor_offline));
                }
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            if (adapter.getItemCount() > 0) {
                                updateTitle(getString(R.string.monitor_offline_args,adapter.getItemCount()));
                            } else {
                                updateTitle(getString(R.string.monitor_offline));
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
