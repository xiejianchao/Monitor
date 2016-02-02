package com.huhuo.monitor.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huhuo.monitor.MonitorApplication;
import com.huhuo.monitor.R;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.model.MonitorModel;
import com.huhuo.monitor.utils.Base64Utils;
import com.huhuo.monitor.utils.DateUtil;
import com.huhuo.monitor.utils.Logger;
import com.huhuo.monitor.utils.SPUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by xiejc on 16/1/14.
 */
public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.MyViewHolder> {

    private static final String TAG = MonitorAdapter.class.getSimpleName();

    private ArrayList<MonitorModel> models;

    public MonitorAdapter() {

    }

    public MonitorAdapter(ArrayList<MonitorModel> models) {
        this.models = models;
    }

    public void setData(ArrayList<MonitorModel> models) {
        this.models = models;
    }

    public ArrayList<MonitorModel> getData() {
        if (this.models == null) {
            this.models = new ArrayList<MonitorModel>();
        }
        return this.models;
    }

    public void clearAll() {
        if (this.models != null) {
            this.models.clear();
            notifyDataSetChanged();
        }
    }

    public void update(int position) {
        notifyItemChanged(position);
    }

    public void delete(MonitorModel model) {
        if (this.models != null) {
            for (int i = 0; i < this.models.size(); i ++) {
                final MonitorModel m = this.models.get(i);
                if (m.getId().equals(model.getId())) {
                    this.models.remove(m);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    public void addOrRefesh(MonitorModel model) {
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
                this.models.add(0,model);
                notifyItemChanged(0);
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
    public void onBindViewHolder(MonitorAdapter.MyViewHolder holder, final int position) {
        final MonitorModel model = models.get(position);
        Context context = MonitorApplication.getInstance().getApplicationContext();
        String nameOrId = null;
        final String name = SPUtil.getString(model.getId() + "_" + Constants.Key.NAME, "");
        Logger.i(TAG,"从本地取出的名字为：" + name);
        if (!TextUtils.isEmpty(name)) {
            final byte[] decode = Base64Utils.decode(name);
            if (decode != null && decode.length > 0) {
                try {
                    nameOrId = new String(decode,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Logger.e(TAG,"onBindViewHolder",e);
                    nameOrId = model.getId();
                }
            } else {
                nameOrId = model.getId();
            }
        } else {
            nameOrId = model.getId();
        }
        Logger.i(TAG,"上位机显示的名字是：" + nameOrId);
        holder.tvUpperMonitId.setText(nameOrId);
        final String time = DateUtil.getFormatTime(model.getTime(), DateUtil.PATTERN_STANDARD);
        holder.tvTime.setText(time.startsWith("1970") ? "#### ####" : time);
        holder.tvStatus.setText(getStatus(context, model.getStatus()));
        Logger.d(TAG, "加载数据，position:" + position + ",model:" + model + ",接收model时间：" + time);
        if (this.listener != null) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, position);
                }
            });
        }
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

        private CardView cardView;
        private TextView tvUpperMonitId;
        private TextView tvTime;
        private TextView tvStatus;


        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            tvUpperMonitId = (TextView) itemView.findViewById(R.id.tv_uppermonitor_name);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view,int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
