package com.huhuo.monitor.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huhuo.monitor.R;
import com.huhuo.monitor.adapter.MonitorAdapter;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.MonitorModel;
import com.huhuo.monitor.ui.activity.DetailsActivity;
import com.huhuo.monitor.ui.activity.MainActivity;
import com.huhuo.monitor.utils.DateUtil;
import com.huhuo.monitor.utils.Logger;
import com.huhuo.monitor.utils.NetWorkUtil;
import com.huhuo.monitor.utils.SPUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;


@ContentView(R.layout.fragment_monitor)
public abstract class BaseMonitorFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = BaseMonitorFragment.class.getSimpleName();

    @ViewInject(R.id.recycler_view)
    protected RecyclerView recyclerView;

    @ViewInject(R.id.swipe_layout)
    protected SwipeRefreshLayout swipeLayout;

    @ViewInject(R.id.layout_tips)
    protected RelativeLayout tipsLayout;

    @ViewInject(R.id.tv_info)
    protected TextView tvTips;

    @ViewInject(R.id.tv_title)
    protected TextView tvTitle;

    @ViewInject(R.id.tv_net_status)
    protected TextView tvNetStatus;

    @ViewInject(R.id.pb_loading)
    protected ProgressBar progressBar;

    protected LinearLayoutManager mLayoutManager;

    protected MonitorAdapter adapter;

    protected abstract void init();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.colorPrimary);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new MonitorAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(listener);

        tipsLayout.setVisibility(View.VISIBLE);

        init();
    }

    private MonitorAdapter.OnItemClickListener listener = new MonitorAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            final ArrayList<MonitorModel> data = adapter.getData();
            if (data != null) {
                final MonitorModel model = data.get(position);
                final Intent intent = new Intent();
                intent.putExtra(Constants.Key.ID,model.getId());
                intent.putExtra(Constants.Key.NAME,model.getName());
                intent.putExtra(Constants.Key.TIME,model.getTime());
                intent.putExtra(Constants.Key.DESC, model.getDetail());
                intent.putExtra(Constants.Key.CONTENT, model.getContent());
                intent.setClass(getActivity(), DetailsActivity.class);
                getActivity().startActivity(intent);
                Logger.d(TAG,"去往详情的内容：" + model.toString());
                Logger.d(TAG,"从XML中取出的内容：" + SPUtil.getString(Constants.Key.DESC,""));
            }
        }
    };

    protected void updateTips(int title) {
        Logger.d(TAG,"item count:" + adapter.getData().size());
//        if (adapter.getData().size() > 0) {
//            showTipsLayout(false);
//        } else {
//            showTipsLayout(true);
//        }
        if (tvTips != null) {
            tvTips.setText(title);
        }
    }
    protected void updateTips(String title) {
        Logger.d(TAG,"item count:" + adapter.getData().size());
//        if (adapter.getData().size() > 0) {
//            showTipsLayout(false);
//        } else {
//            showTipsLayout(true);
//        }
        if (tvTips != null) {
            tvTips.setText(title);
        }
    }

    protected void updateTitle(int title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    protected void updateTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    protected void showProgressBar(boolean show){
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showTipsLayout(boolean show){
        if (tipsLayout != null) {
            tipsLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isNullData() {
        Logger.d(TAG,"adapter size:" + adapter.getData().size());
        if (adapter.getData().size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    protected void showRecycleView(boolean show){
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void updateNetTips(boolean isConnection){
        if (getActivity() instanceof MainActivity) {
            MainActivity ac = (MainActivity) getActivity();
            final long time = ac.getLastConnTime();
            String formatConnTime = DateUtil.getFormatTime(time, DateUtil.PATTERN_STANDARD);
            if (!TextUtils.isEmpty(formatConnTime)) {
                if (formatConnTime.startsWith("1970")) {
                    formatConnTime = getString(R.string.common_not);
                }
            } else {
                formatConnTime = getString(R.string.common_not);
            }
            if (isConnection) {
                tvNetStatus.setBackgroundColor(getResources().getColor(R.color.color_send_green_bg_normal));
                tvNetStatus.setText(getString(R.string.tips_server_connection_ok, formatConnTime));
            } else {
                tvNetStatus.setBackgroundColor(getResources().getColor(R.color.color_yellow));
                tvNetStatus.setText(getString(R.string.tips_server_connection_error, formatConnTime));
            }
        }
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        swipeLayout.setEnabled(false);

        if (NetWorkUtil.isNetWorkEnable(context)) {
            if (getActivity() instanceof MainActivity) {
                Logger.i(TAG, "重新连接服务器，调用list接口初始化");
                MainActivity ac = (MainActivity) getActivity();
                ac.restartWebSocket();
            }
        } else {
            Logger.d(TAG,"没有网络，不执行onRefresh");
            closeRefresh();
        }

//        if (WebSocketClient.SOCKET_STATUS == Constants.CLOSE
//                || WebSocketClient.SOCKET_STATUS == Constants.ERROR) {
//            if (getActivity() instanceof MainActivity) {
//                MainActivity ac = (MainActivity) getActivity();
//                ac.startWebSocket();
//                Logger.d(TAG,"连接被关闭，或者出现错误，尝试重新连接到服务器...");
//            }
//        } else {
//            closeRefresh();
//            updateNetTips(true);
//            Logger.d(TAG, "与服务器的连接正常，不需要下拉刷新重新连接");
//        }

    }

    public void closeRefresh() {
        swipeLayout.setRefreshing(false);
        swipeLayout.setEnabled(true);
    }


    public void onStatusMonitor(MonitorModel model) {

    }

    public void onDeleteMonitor(MonitorModel model) {

    }

    public void onUpdateMonitro(MonitorModel model){
        adapter.addOrRefesh(model);
    }


    protected void deleteMonitor(MonitorModel model) {
        adapter.delete(model);
        final ArrayList<MonitorModel> datas = adapter.getData();
        if (datas == null || datas.size() == 0) {
            showRecycleView(false);
//            updateTips(R.string.tips_connect_success_online_wait);
        }
    }

    public void onOpen() {
        updateNetTips(true);
        closeRefresh();
        Logger.w(TAG,"server conn onOpen");
    }
    public void onClose(int code, String reason, boolean remote) {
        updateTips(R.string.tips_connect_close_try_again);
        updateNetTips(false);
        closeRefresh();
    }

    public void onError(Exception e) {
        updateTips(R.string.tips_connect_error_try_again);
        updateNetTips(false);
        closeRefresh();
    }

    /**
     * 子类覆写该方法必须调用super.onReceive方法
     * @param model
     */
    public void onReceive(MonitorModel model) {
        final boolean isNull = isNullData();
        showTipsLayout(false);
        Logger.d(TAG,"size:" + adapter.getItemCount());

        closeRefresh();
        showRecycleView(true);
        updateNetTips(true);
    }


    protected void deleteExceptionByXML(MonitorModel model) {
        final boolean b = SPUtil.removeKey(model.getId() + "_" + Constants.Key.CONTENT);
        if (b) {
            Logger.w(TAG,"删除"+ model.getId() +"的本地异常信息成功");
        } else {
            Logger.w(TAG,"删除"+ model.getId() +"的本地异常信息失败");
        }
    }


    public void clearLocalData() {
        if (adapter != null) {
            final ArrayList<MonitorModel> list = adapter.getData();
            if (list != null && list.size() > 0) {
                for (MonitorModel model : list) {
                    SPUtil.clearMonitorInfo(model);
                    Logger.w(TAG,"从本地清除" + model.toString());
                }
            }
            adapter.clearAll();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
