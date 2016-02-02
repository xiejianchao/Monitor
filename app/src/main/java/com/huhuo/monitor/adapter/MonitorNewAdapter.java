package com.huhuo.monitor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huhuo.monitor.MonitorApplication;
import com.huhuo.monitor.R;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.MonitorModel;
import com.huhuo.monitor.utils.DateUtil;
import com.huhuo.monitor.utils.Logger;

import java.util.ArrayList;

/**
 * Created by xiejc on 16/1/14.
 */
public class MonitorNewAdapter extends RecyclerView.Adapter<MonitorNewAdapter.MyViewHolder> {

    private static final String TAG = MonitorNewAdapter.class.getSimpleName();

    private ArrayList<MonitorModel> models;

    public MonitorNewAdapter() {

    }

    public MonitorNewAdapter(ArrayList<MonitorModel> models) {
        this.models = models;
    }

    public void setData(ArrayList<MonitorModel> models) {
        this.models = models;
    }

    public void update(int position) {
        notifyItemChanged(position);
    }

    public void refesh(MonitorModel model) {
        if (this.models == null) {
            this.models = new ArrayList<MonitorModel>();
        }
        if (this.models.size() > 0) {
            boolean haveModel = false;
            for (int i = 0; i < this.models.size(); i ++) {
                final MonitorModel m = this.models.get(i);
                if (m.getId().equals(model.getId())) {
                    this.models.remove(m);
                    this.models.add(i,model);
                    notifyItemChanged(i);
                    haveModel = true;
                    break;
                } else {
                    haveModel = false;
                }
            }
            if (!haveModel) {
                this.models.add(model);
                notifyItemChanged(this.models.size() - 1);
            }
        } else {
            this.models.add(model);
            this.notifyDataSetChanged();
            notifyItemChanged(0);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //如果如果需要显示不同类型的网站，需要重写该方法，通知onCreateViewHolder 显示不同的布局

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_item_ping_test, parent, false);
        return new MyViewHolder(view);
    }

    /**
     * 如果需要显示不同类型的网站，需要重写该方法，通知onCreateViewHolder 显示不同的布局
     *
     * @param position
     * @return
     */
//    @Override
//    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//    }

    @Override
    public void onBindViewHolder(MonitorNewAdapter.MyViewHolder holder, int position) {
        final MonitorModel model = models.get(position);;
        Context context = MonitorApplication.getInstance().getApplicationContext();
        holder.tvUpperMonitId.setText(model.getId());
        final String time = DateUtil.getFormatTime(model.getTime(), DateUtil.PATTERN_STANDARD);
        holder.tvTime.setText(time);
        holder.tvStatus.setText(getStatus(context, model.getStatus()));
        Logger.d(TAG, "加载数据，position:" + position + ",model:" + model + ",接收model时间：" + time);
    }

    private String getStatus(Context context, final int status) {
        String retVal = null;
        switch (status) {
            case Constants.ONLINE:
                retVal = context.getString(R.string.monitor_online);
                break;
            case Constants.OFFLINE:
                retVal = context.getString(R.string.monitor_offline);
                break;
            case Constants.FAULT:
                retVal = context.getString(R.string.monitor_fault);
                break;
            case Constants.ALARM:
                retVal = context.getString(R.string.monitor_alarm);
                break;
            default:
                retVal = context.getString(R.string.monitor_unknow);
                break;
        }

        return retVal;
    }

    @Override
    public int getItemCount() {
        return this.models == null ? 0 : this.models.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUpperMonitId;
        private TextView tvTime;
        private TextView tvStatus;


        public MyViewHolder(View itemView) {
            super(itemView);

            tvUpperMonitId = (TextView) itemView.findViewById(R.id.tv_uppermonitor_name);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);

        }
    }
}
