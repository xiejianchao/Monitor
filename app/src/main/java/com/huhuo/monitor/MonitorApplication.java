package com.huhuo.monitor;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.huhuo.monitor.constants.AppConfig;
import com.huhuo.monitor.constants.Constants;
import com.huhuo.monitor.db.DatabaseHelper;
import com.huhuo.monitor.utils.Logger;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiejc on 15/12/11.
 */
public class MonitorApplication extends Application{

    private static final String TAG = MonitorApplication.class.getSimpleName();
    private Context context;


    private static MonitorApplication application;
    private String imUserId = null;

    @Override
    public void onCreate() {
        super.onCreate();

        this.application = this;
        x.Ext.init(this);
        if (AppConfig.IS_DEBUG) {
            x.Ext.setDebug(true); // 是否输出debug日志
        } else {
            x.Ext.setDebug(false);
        }

//        initDatabaseHelper();

//        initImageLoader(getApplicationContext());
    }

    //测试代码，初始化数据库，根据当前登录用户的id创建db，随时删除
    private void initDatabaseHelper() {
        if (DatabaseHelper.getInstance() == null) {
            DatabaseHelper.init(this, Constants.DEFAULT_USER_ID);
            Logger.d(TAG, "初始化数据库");

        }
    }

    private List<Activity> list = new ArrayList<Activity>();

    public void addTempActivity(Activity ac){
        list.add(ac);
    }

    public void removeTempActivity(){
        if (list.size() > 0) {
            for (Activity ac : list) {
                if (ac != null) {
                    ac.finish();
                }
            }
        }
    }

    public static MonitorApplication getInstance() {
        return application;
    }

    public Context getContext() {
        return getInstance();
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        //缓存文件的目录
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, "Monitor/Cache");
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        if (AppConfig.IS_DEBUG) {
            config.writeDebugLogs(); // Remove for release app
        }

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    public void setImUserId(String userId){
        this.imUserId = userId;
    }

    public String getImUserId() {
        return this.imUserId;
    }

}
