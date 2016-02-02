package com.huhuo.monitor.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.huhuo.monitor.R;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.CommonDataModel;
import com.huhuo.monitor.model.BaseJsonModel;
import com.huhuo.monitor.model.ListDataModel;
import com.huhuo.monitor.model.MonitorModel;
import com.huhuo.monitor.net.WebSocketClient;
import com.huhuo.monitor.ui.fragment.OfflineFragment;
import com.huhuo.monitor.ui.fragment.FaultFragment;
import com.huhuo.monitor.ui.fragment.OnlineFragment;
import com.huhuo.monitor.ui.fragment.AlarmFragment;
import com.huhuo.monitor.utils.Base64Utils;
import com.huhuo.monitor.utils.DateUtil;
import com.huhuo.monitor.utils.DialogBuilder;
import com.huhuo.monitor.utils.GsonUtil;
import com.huhuo.monitor.utils.Logger;
import com.huhuo.monitor.utils.NetWorkUtil;
import com.huhuo.monitor.utils.SPUtil;

import org.java_websocket.handshake.ServerHandshake;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements WebSocketClient.OnReceiveMessageListener
        ,WebSocketClient.OnRestartListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private FragmentManager fragmentManager;
    // 在线
    private OnlineFragment onlineFragment;

    // 故障
    private FaultFragment faultFragment;

    // 报警
    private AlarmFragment alarmFragment;

    // 离线
    private OfflineFragment offlineFragment;

    //在线tab的TextView
    @ViewInject(R.id.tv_online)
    private TextView tvOnline;

    //报警tab的TextView
    @ViewInject(R.id.tv_alarm)
    private TextView tvAlram;

    //故障tab的TextView
    @ViewInject(R.id.tv_fault)
    private TextView tvFault;

    //离线tab的TextView
    @ViewInject(R.id.tv_offline)
    private TextView tvOffline;

    private WebSocketClient webSocketClient;

    @Override
    protected void init(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
//        updateTitleText(R.string.app_name);
        // 默认选中首屏
        setTabSelection(0);
        enableSwipeBack(false);//首页禁止开启右滑返回

        initFragment();

        initWebSocket();
        startWebSocket();
    }

    private void initWebSocket(){
        try {
            URI uri = new URI("ws://xiaofang.northchinatech.com:28007/websocket");
            webSocketClient = new WebSocketClient(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void initFragment() {
        //确保执行fragment的各声明周期，否则无法执行fragment内相应逻辑
        setTabSelection(0);
        setTabSelection(1);
        setTabSelection(2);
        setTabSelection(3);
        setTabSelection(0);
    }

    private void startSocket() {
        try {
            if (NetWorkUtil.isNetWorkEnable(this)) {
                Logger.w(TAG,"网络正常，开始重启");
                if (webSocketClient == null) {
                    initWebSocket();
                }
//          webSocketClient.connectBlocking();
                webSocketClient.connect();
                webSocketClient.setReceiveMessageListener(MainActivity.this);
            } else {
                Logger.w(TAG,"网络不正常，取消重启圆圈");
                closeRefresh();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG,"startWebSocket",e);
        }
    }


    private void startWebSocket() {
        startSocket();
    }

    /**
     * 重启websocket，先关闭，然后再开启
     */
    public void restartWebSocket() {
        closeWebSocket(true);
    }

    private void closeWebSocket(boolean restart) {
        if (webSocketClient != null) {
            if (webSocketClient.isClosed()) {
                Logger.d(TAG,"已经close");
                webSocketClient = null;
                closeRefresh();
                startSocket();
            } else {
                Logger.d(TAG,"没有close");
                webSocketClient.setOnRestartListener(restart ? this : null);
                webSocketClient.close();
                webSocketClient = null;
            }

        }
    }

    @Override
    public void onAllowRestartWebSocket() {
        Logger.w(TAG,"允许重新打开websocket，可以重新打开了");
        startSocket();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (onlineFragment != null) {
            onlineFragment.onOpen();
        }

        if (faultFragment != null) {
            faultFragment.onOpen();
        }

        if (alarmFragment != null) {
            alarmFragment.onOpen();
        }

        if (offlineFragment != null) {
            offlineFragment.onOpen();
        }

        Logger.w(TAG, "onOpen");
    }


    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (onlineFragment != null) {
            onlineFragment.onClose(code, reason, remote);
        }

        if (faultFragment != null) {
            faultFragment.onClose(code, reason, remote);
        }

        if (alarmFragment != null) {
            alarmFragment.onClose(code, reason, remote);
        }

        if (offlineFragment != null) {
            offlineFragment.onClose(code, reason, remote);
        }

        closeRefresh();
        Logger.w(TAG, "onClose");
    }

    @Override
    public void onError(Exception e) {
        if (onlineFragment != null) {
            onlineFragment.onError(e);
        }

        if (faultFragment != null) {
            faultFragment.onError(e);
        }

        if (alarmFragment != null) {
            alarmFragment.onError(e);
        }

        if (offlineFragment != null) {
            offlineFragment.onError(e);
        }

        Logger.w(TAG, "onError");
    }

    private long lastConnectionTime = 0;
    public long getLastConnTime() {
        return lastConnectionTime;
    }

    private void updateLastConnTime(long mil) {
        Logger.v(TAG, "现在业务时间戳：" + mil);
        final String formatTime = DateUtil.getFormatTime(mil, DateUtil.PATTERN_STANDARD);
        Logger.v(TAG, "现在业务时间戳：" + formatTime);

        final boolean isConn = isConnServer();
        if (mil > lastConnectionTime) {
            this.lastConnectionTime = mil;
            updateNetTips(isConn);
            Logger.v(TAG, "现在更新业务时间戳：" + formatTime);
        } else {
            Logger.d(TAG, "当前业务时间小于之前保存的最后一次与服务器连接时间， 不予记录");
        }
    }

    //0表示正常，1表示故障，2表示报警,3表示报警
    @Override
    public void onReceiveMessage(String message) {
        Logger.i(TAG, "onReceiveMessage" + message);
        final BaseJsonModel baseModel = GsonUtil.parseMonitorNew(message);
        if (baseModel != null) {
            final String type = baseModel.getType();
            switch (type) {
                case Constants.LIST://初始化上位机列表
                    ListDataModel listModel = (ListDataModel) baseModel;
                    initMonitorList(listModel);
                    Logger.w(TAG, "初始化上位机列表...");
                    break;
                case Constants.ADD:
                    CommonDataModel addModel = (CommonDataModel) baseModel;
                    addMonitor(addModel);
                    Logger.w(TAG, "接收到新增上位机消息...");
                    break;
                case Constants.DEL:
                    CommonDataModel delModel = (CommonDataModel) baseModel;
                    delMonitor(delModel);
                    Logger.w(TAG, "接收到删除上位机消息...");
                    break;
                case Constants.UPDATE:
                    CommonDataModel updateModel = (CommonDataModel) baseModel;
                    updateMonitor(updateModel);
                    Logger.w(TAG, "接收到更新上位机消息...");
                    break;
                case Constants.STATUS:
                    CommonDataModel statusModel = (CommonDataModel) baseModel;
                    statusMonitor(statusModel);
                    Logger.w(TAG, "接收到状态更新上位机消息...");
                    break;
                case Constants.CLEAR:
                    CommonDataModel clarModel = (CommonDataModel) baseModel;
                    clearMonitor(clarModel);
                    Logger.w(TAG, "接收到clear上位机消息...");
                    break;
            }
        } else {
            Logger.d(TAG, "上位机发送了一条看不懂的消息：" + message);
        }
    }

    /**
     * 根据服务器指令，从本地列表中修改指定的上位机状态
     *
     * @param statusModel
     */
    private void statusMonitor(CommonDataModel statusModel) {
        if (statusModel != null) {
            final MonitorModel data = statusModel.getData();
            long time = data.getTime();
            updateLastConnTime(time);
            if (data != null) {
                //1.先将要修改的上位机从所有页面全部删除
                if (onlineFragment != null) {
                    onlineFragment.onStatusMonitor(data);
                }

                if (faultFragment != null) {
                    faultFragment.onStatusMonitor(data);
                }

                if (alarmFragment != null) {
                    alarmFragment.onStatusMonitor(data);
                }

                if (offlineFragment != null) {
                    offlineFragment.onStatusMonitor(data);
                }
                //2.然后再根据要修改状态的上位机属于哪个状态，添加到所属页面
                showMonitor(data);
            }
        }
    }

    /**
     * 根据服务器指令，从本地列表中清除指定的上位机的异常信息
     *
     * @param clearModel
     */
    private void clearMonitor(CommonDataModel clearModel) {
        if (clearModel != null) {
            final MonitorModel data = clearModel.getData();
            long time = data.getTime();
            updateLastConnTime(time);

            //1.先将其从原来的异常页面删除
            deleteMonitor(data);
            //2.再将该上位机更新到正确的页面
            showMonitor(data);
        }
    }



    /**
     * 根据服务器指令，从本地列表中更新指定的上位机的信息
     *
     * @param updateModel
     */
    private void updateMonitor(CommonDataModel updateModel) {
        if (updateModel != null) {
            final MonitorModel data = updateModel.getData();
            long time = data.getTime();
            updateLastConnTime(time);

            saveLocalMonitorData(data);
            showMonitor(data);
        }
    }

    /**
     * 根据服务器指令，从本地列表中删除指定的上位机
     *
     * @param delModel
     */
    private void delMonitor(CommonDataModel delModel) {
        if (delModel != null) {
            final MonitorModel data = delModel.getData();
            long time = data.getTime();
            updateLastConnTime(time);
            deleteMonitor(data);
        }
    }

    private void addMonitor(CommonDataModel model) {
        if (model != null) {
            final MonitorModel data = model.getData();
            long time = data.getTime();
            updateLastConnTime(time);
            saveLocalMonitorData(data);
            if (data != null) {
                showMonitor(data);
            }
        }
    }



    /**
     * 初始化上位机列表
     *
     * @param listModel
     */
    private void initMonitorList(ListDataModel listModel) {
        final List<MonitorModel> data = listModel.getData();
        if (data != null && data.size() > 0) {
            clearLocalMonitorData();
            for (MonitorModel model : data) {
                //fuck 叶总，更新的时候不给我返回name，还得让本王初始化时保存一份
                Logger.v(TAG, "id：" + model.getId());
                if (!TextUtils.isEmpty(model.getName())) {
                    final byte[] decode = Base64Utils.decode(model.getName());
                    Logger.v(TAG,"name：" + new String(decode));
                } else {
                    Logger.v(TAG,"name：null" );
                }
                Logger.v(TAG, "status：" + model.getStatus());

                saveLocalMonitorData(model);
                showMonitor(model);
            }
        }
    }

    /**
     * 初始化接口时，删除所有在本地保存的信息
     */
    private void clearLocalMonitorData() {

        if (onlineFragment != null) {
            onlineFragment.clearLocalData();
        }

        if (faultFragment != null) {
            faultFragment.clearLocalData();
        }

        if (alarmFragment != null) {
            alarmFragment.clearLocalData();
        }

        if (offlineFragment != null) {
            offlineFragment.clearLocalData();
        }
    }

    private void saveLocalMonitorData(MonitorModel model) {
        SPUtil.saveMonitorInfo(model);
    }

    private void deleteMonitor(MonitorModel data) {
        if (onlineFragment != null) {
            onlineFragment.onDeleteMonitor(data);
        }

        if (faultFragment != null) {
            faultFragment.onDeleteMonitor(data);
        }

        if (alarmFragment != null) {
            alarmFragment.onDeleteMonitor(data);
        }

        if (offlineFragment != null) {
            offlineFragment.onDeleteMonitor(data);
        }
    }

    private void closeRefresh() {
        if (onlineFragment != null) {
            onlineFragment.closeRefresh();
        }

        if (faultFragment != null) {
            faultFragment.closeRefresh();
        }

        if (alarmFragment != null) {
            alarmFragment.closeRefresh();
        }

        if (offlineFragment != null) {
            offlineFragment.closeRefresh();
        }
    }

    /**
     * 根据不同状态，显示上位机到不同的页面
     *
     * @param model
     */
    private void showMonitor(MonitorModel model) {
        long time = model.getTime();
        updateLastConnTime(time);
        if (model != null) {
            final int status = model.getStatus();
            switch (status) {
                case Constants.ONLINE:
                    if (onlineFragment != null && model != null) {
                        onlineFragment.onReceive(model);
                    }
                    break;
                case Constants.FAULT:
                    if (faultFragment != null && model != null) {
                        faultFragment.onReceive(model);
                    }
                    break;
                case Constants.ALARM:
                    if (alarmFragment != null && model != null) {
                        alarmFragment.onReceive(model);
                    }
                    break;
                case Constants.OFFLINE:
                    if (offlineFragment != null && model != null) {
                        offlineFragment.onReceive(model);
                    }
                    break;
            }
        }
    }

    @Event(value = {
            R.id.layout_online,
            R.id.layout_offline,
            R.id.layout_fault,
            R.id.layout_alarm,})
    private void tabLayoutClick(View view) {
        switch (view.getId()) {
            case R.id.layout_online:
                setTabSelection(0);
                updateTitleText(R.string.monitor_online);
                break;
            case R.id.layout_fault:
                setTabSelection(1);
                updateTitleText(R.string.monitor_fault);
                break;
            case R.id.layout_alarm:
                setTabSelection(2);
                updateTitleText(R.string.monitor_alarm);
                break;
            case R.id.layout_offline:
                setTabSelection(3);
                updateTitleText(R.string.monitor_offline);
                break;
        }

    }

    private boolean isConnServer() {
        boolean isConn = false;
        if (WebSocketClient.SOCKET_STATUS == Constants.OPEN
                || WebSocketClient.SOCKET_STATUS == Constants.RECEIVE_MESSAGE) {
            isConn = true;
        } else {
            isConn = false;
        }
        return isConn;
    }

    private void updateNetTips(boolean isConn){
        if (onlineFragment != null) {
            onlineFragment.updateNetTips(isConn);
        }

        if (faultFragment != null) {
            faultFragment.updateNetTips(isConn);
        }

        if (alarmFragment != null) {
            alarmFragment.updateNetTips(isConn);
        }

        if (offlineFragment != null) {
            offlineFragment.updateNetTips(isConn);
        }
    }

    /**
     * 根据传入的index参数来设置选中的tab页。
     *
     * @param index 每个tab页对应的下标。0表示合租吧，1表示消息，2表示我的
     */
    private void setTabSelection(int index) {
        boolean isConn = isConnServer();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 先清除掉上次的选中状态
        clearSelection();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        switchSelectedTabImg(index);
        switch (index) {
            case 0:
                if (onlineFragment == null) {
                    onlineFragment = new OnlineFragment();
                    transaction.add(R.id.content, onlineFragment);
                } else {
                    transaction.show(onlineFragment);
                }
                onlineFragment.updateNetTips(isConn);
                break;
            case 1:
                if (faultFragment == null) {
                    faultFragment = new FaultFragment();
                    transaction.add(R.id.content, faultFragment);
                } else {
                    transaction.show(faultFragment);
                }
                faultFragment.updateNetTips(isConn);
                break;

            case 2:
                if (alarmFragment == null) {
                    alarmFragment = new AlarmFragment();
                    transaction.add(R.id.content, alarmFragment);
                } else {
                    transaction.show(alarmFragment);
                }
                alarmFragment.updateNetTips(isConn);
                break;

            case 3:
                if (offlineFragment == null) {
                    offlineFragment = new OfflineFragment();
                    transaction.add(R.id.content, offlineFragment);
                } else {
                    transaction.show(offlineFragment);
                }
                offlineFragment.updateNetTips(isConn);
                break;

        }
        transaction.commit();
    }

    private void switchSelectedTabImg(int index) {
        switch (index) {
            case 0:
                tvOnline.setTextColor(getResources().getColor(R.color.color_blue_default));
                tvOnline.setSelected(true);
                tvFault.setSelected(false);
                tvAlram.setSelected(false);
                tvOffline.setSelected(false);
                break;
            case 1:
                tvFault.setTextColor(getResources().getColor(R.color.color_blue_default));
                tvFault.setSelected(true);
                tvOnline.setSelected(false);
                tvOffline.setSelected(false);
                tvAlram.setSelected(false);
                break;

            case 2:
                tvAlram.setTextColor(getResources().getColor(R.color.color_blue_default));
                tvAlram.setSelected(true);
                tvOnline.setSelected(false);
                tvOffline.setSelected(false);
                tvFault.setSelected(false);
                break;

            case 3:
                tvOffline.setTextColor(getResources().getColor(R.color.color_blue_default));
                tvOffline.setSelected(true);
                tvOnline.setSelected(false);
                tvFault.setSelected(false);
                tvAlram.setSelected(false);
                break;

        }
    }

    /**
     * 清除掉所有的选中状态。
     */
    private void clearSelection() {
        tvOnline.setTextColor(getResources().getColor(
                R.color.color_guide_background_press));
        tvFault.setTextColor(getResources().getColor(
                R.color.color_guide_background_press));
        tvAlram.setTextColor(getResources().getColor(
                R.color.color_guide_background_press));
        tvOffline.setTextColor(getResources().getColor(
                R.color.color_guide_background_press));
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (onlineFragment != null) {
            transaction.hide(onlineFragment);
        }
        if (faultFragment != null) {
            transaction.hide(faultFragment);
        }
        if (alarmFragment != null) {
            transaction.hide(alarmFragment);
        }
        if (offlineFragment != null) {
            transaction.hide(offlineFragment);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            DialogBuilder.showDialog(
                    this,
                    R.string.common_prompt,
                    R.string.tips_exit,
                    R.string.common_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    },
                    R.string.common_cancel,
                    null,
                    true);

            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void updateOnlineTabCount(int count){
        if (count > 0) {
            tvOnline.setText(getString(R.string.monitor_online_args, count));
        } else {
            tvOnline.setText(R.string.monitor_online);
        }
    }

    public void updateFaultTabCount(int count){
        if (count > 0) {
            tvFault.setText(getString(R.string.monitor_fault_args,count));
        } else {
            tvFault.setText(R.string.monitor_fault);
        }
    }

    public void updateAlarmTabCount(int count){
        if (count > 0) {
            tvAlram.setText(getString(R.string.monitor_alarm_args,count));
        } else {
            tvAlram.setText(R.string.monitor_alarm);
        }
    }

    public void updateOfflineTabCount(int count){
        if (count > 0) {
            tvOffline.setText(getString(R.string.monitor_offline_args, count));
        } else {
            tvOffline.setText(R.string.monitor_offline);
        }
    }

}
