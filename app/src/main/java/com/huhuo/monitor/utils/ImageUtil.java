package com.huhuo.monitor.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.huhuo.monitor.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xiejc on 15/12/14.
 */
public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();
    private static DisplayImageOptions circleOptions;
    private static DisplayImageOptions defaultOptions;

    private static ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    static {

        circleOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示,图片会缩放到目标大小完全
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.RED, 1))//圆形ImageView的边框大小
                .build();

        defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示,图片会缩放到目标大小完全
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    /**
     * 加载网络图片
     *
     * @param imageView
     * @param url
     */
    public static void displayImage(ImageView imageView, String url) {
        ImageLoader.getInstance().displayImage(url, imageView,defaultOptions,animateFirstListener);
    }

    public static void displayImageLocal(ImageView imageView, String url) {
        String path = Uri.fromFile(new File(url)).toString();
        String path2 = "file://" + url;
        Logger.d(TAG, "path:" + path);
        Logger.d(TAG, "path2:" + path2);

        ImageLoader.getInstance().displayImage(path2, imageView,defaultOptions,animateFirstListener);
    }

    /**
     * 加载网络图片使其变成圆形显示
     *
     * @param imageView
     *             ImageView宽高属性必须有具体的大小，不能是wrap_content
     * @param url
     */
    public static void displayImage2Circle(ImageView imageView, String url) {
        ImageLoader.getInstance().displayImage(url, imageView, circleOptions, animateFirstListener);
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    public static void clearMemoryCache(){
        ImageLoader.getInstance().clearMemoryCache();
    }

    /**
     * 默认不清楚，只有在主动清理缓存是才清楚磁盘缓存
     */
    public static void clearDiskCache(){
        ImageLoader.getInstance().clearDiskCache();
    }

    public static void clearAllCache(){
        clearMemoryCache();
        clearDiskCache();
    }

}
