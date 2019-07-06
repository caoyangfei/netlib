package com.flyang.imageloader;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.flyang.imageloader.manager.ProgressManager;

import java.io.InputStream;

/**
 * @author caoyangfei
 * @ClassName ImageGlideModule
 * @date 2019/7/6
 * ------------- Description -------------
 * glide注解
 */
@GlideModule
public class ImageAppGlideModule extends AppGlideModule {

    //注册自定义组件
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(ProgressManager.getOkHttpClient()));
    }

    //全局配置Glide
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
    }

    // 避免二次加载
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}