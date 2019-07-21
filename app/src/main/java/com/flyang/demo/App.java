package com.flyang.demo;

import android.app.Application;

import com.flyang.imageloader.ImageLoader;
import com.flyang.imageloader.loader.GlideImageLoaderStrategy;
import com.flyang.util.app.ApplicationUtils;

/**
 * @author yangfei.cao
 * @ClassName netlib_demo
 * @date 2019/7/6
 * ------------- Description -------------
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationUtils.init(this);
        ImageLoader.init(new GlideImageLoaderStrategy());
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ImageLoader.trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        ImageLoader.clearAllMemoryCaches();
    }
}
