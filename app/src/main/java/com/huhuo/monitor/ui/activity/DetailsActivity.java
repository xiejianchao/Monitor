package com.huhuo.monitor.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.huhuo.monitor.R;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.utils.Base64Utils;
import com.huhuo.monitor.utils.DateUtil;
import com.huhuo.monitor.utils.Logger;
import com.huhuo.monitor.utils.SPUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.UnsupportedEncodingException;

@ContentView(R.layout.activity_details)
public class DetailsActivity extends BaseActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    @ViewInject(R.id.tv_details_title)
    private TextView tvTitle;

    @ViewInject(R.id.tv_details)
    private TextView tvDetails;

    @ViewInject(R.id.tv_content)
    private TextView tvContent;

    @ViewInject(R.id.tv_date)
    private TextView tvDate;

    @Override
    public void init(Bundle savedInstanceStat) {

        final Intent intent = getIntent();
        if (intent != null) {
            final String id = intent.getStringExtra(Constants.Key.ID);
            long time = intent.getLongExtra(Constants.Key.TIME, 0);
            final String formatTime = DateUtil.getFormatTime(time, DateUtil.PATTERN_STANDARD);

            final String name = SPUtil.getString(id + "_" + Constants.Key.NAME,"");
            final String desc = SPUtil.getString(id + "_" + Constants.Key.DESC, "");
            final String content = SPUtil.getString(id + "_" + Constants.Key.CONTENT, "");

            Logger.d(TAG, "desc:" + desc + ",content:" + content);
            tvDate.setText(formatTime);

            //显示编号
            tvDetails.append("编号：" + id + "\n");

            //显示别名
            if (!TextUtils.isEmpty(name) && Base64Utils.decode(name)
                    != null && Base64Utils.decode(name).length > 0) {
                try {
                    tvDetails.append("别名：" + new String(Base64Utils.decode(name),"UTF-8") + "\n");
                } catch (UnsupportedEncodingException e) {
                    Logger.e(TAG,"init",e);
                    tvDetails.append("别名：" + getString(R.string.common_name_null) + "\n");
                }
            } else {
                tvDetails.append("别名：" + getString(R.string.common_name_null) + "\n");
            }

            //显示详情
            if (!TextUtils.isEmpty(desc) && Base64Utils.decode(desc)
                    != null && Base64Utils.decode(desc).length > 0) {
                try {
                    tvDetails.append("详情：" + new String(Base64Utils.decode(desc),"UTF-8") + "\n");
                } catch (UnsupportedEncodingException e) {
                    Logger.e(TAG,"init",e);
                    tvDetails.append("详情：" + R.string.common_deatils_null + "\n");
                }
            } else {
                tvDetails.append("详情：" + R.string.common_deatils_null + "\n");
            }

            //显示异常信息
            if (!TextUtils.isEmpty(content) && !content.equalsIgnoreCase("null")) {
                tvContent.append("异常信息：\n");
                tvContent.append(content);
                tvContent.setTextColor(getResources().getColor(R.color.color_red));
            }
        }

    }

}
