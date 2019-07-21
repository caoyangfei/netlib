package com.flyang.imageloader.loader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.flyang.imageloader.GlideApp;
import com.flyang.imageloader.config.ImageConfigSpread;
import com.flyang.imageloader.config.ImageLoaderOptionsSwitch;
import com.flyang.imageloader.config.inter.ScaleMode;
import com.flyang.imageloader.loader.runable.GlideDownLoadImageRunable;
import com.flyang.imageloader.loader.target.GlideBitmapImageViewTarget;
import com.flyang.imageloader.loader.target.GlideDrawableImageViewTarget;
import com.flyang.progress.ProgressManager;
import com.flyang.util.app.ApplicationUtils;
import com.flyang.util.data.ThreadUtils;

import java.io.File;

/**
 * @author yangfei.cao
 * @ClassName netlib_demo
 * @date 2019/7/4
 * ------------- Description -------------
 * <p>
 * glide加载
 */
public class GlideImageLoaderStrategy implements ImageLoaderStrategy<ImageConfigSpread> {

    public static Context context = ApplicationUtils.getApp();

    String key = "";

    public void init(int cacheSizeInM, MemoryCategory memoryCategory, boolean isInternalCD) {
        GlideBuilder builder = new GlideBuilder();
        if (isInternalCD) {
            //内部磁盘缓存
            builder.setDiskCache(new InternalCacheDiskCacheFactory(context, cacheSizeInM * 1024 * 1024));
        } else {
            //外部磁盘缓存
            builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, cacheSizeInM * 1024 * 1024));
        }
        GlideApp.init(context, builder);
        GlideApp.get(context).setMemoryCategory(memoryCategory); //如果在应用当中想要调整内存缓存的大小，开发者可以通过如下方式：
    }

    @Override
    public void showImage(ImageConfigSpread imageConfig) {
        if (imageConfig == null) {
            throw new RuntimeException("Not initialized ImageConfigSpread");
        }
        context = imageConfig.getContext();
        RequestManager requestManager = GlideApp.with(context);
        RequestBuilder request = getDrawableTypeRequest(imageConfig, requestManager);
        loadOptions(request, imageConfig);
    }

    /**
     * 清除磁盘缓存（需要在子线程里调用）
     */
    @Override
    public void clearDiskCache() {
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @Nullable
            @Override
            public Boolean doInBackground() throws Throwable {
                GlideApp.get(context).clearDiskCache();
                return true;
            }

            @Override
            public void onSuccess(@Nullable Boolean result) {

            }
        });
    }

    /**
     * 清除内存缓存（需要在UI线程里调用）
     */
    @Override
    public void clearMomory() {
        GlideApp.get(context).clearMemory();
    }

    @Override
    public void resumeRequests() {
        GlideApp.with(context).resumeRequestsRecursive();
    }

    @Override
    public void pauseRequests() {
        key = "";
        GlideApp.with(context).pauseRequestsRecursive();
    }


    @Override
    public void trimMemory(int level) {
        GlideApp.get(context).onTrimMemory(level);
    }

    @Override
    public void clearAllMemoryCaches() {
        GlideApp.get(context).onLowMemory();
    }

    /**
     * 获取加载路径
     *
     * @param imageConfig
     * @param requestManager
     * @return
     */
    @Nullable
    private RequestBuilder getDrawableTypeRequest(ImageConfigSpread imageConfig, RequestManager requestManager) {
        RequestBuilder requestBuilder = null;
        if (imageConfig.isAsBitmap()) {
            requestBuilder = requestManager.asBitmap();
        }
        if (requestBuilder == null && imageConfig.isAsGif()) {
            requestBuilder = requestManager.asGif();
        }
        if (requestBuilder == null && imageConfig.isAsFile()) {
            requestBuilder = requestManager.asFile();
        }
        if (requestBuilder == null && imageConfig.isAsDrawable()) {
            requestBuilder = requestManager.asDrawable();
        }

        if (!TextUtils.isEmpty(imageConfig.getUrl())) {
            key = imageConfig.getUrl();
            requestBuilder = requestBuilder.load(imageConfig.getUrl());
        } else if (!TextUtils.isEmpty(imageConfig.getFilePath())) {
            key = imageConfig.getFilePath();
            requestBuilder = requestBuilder.load(imageConfig.getFilePath());
        } else if (!TextUtils.isEmpty(imageConfig.getContentProvider())) {
            key = imageConfig.getContentProvider();
            requestBuilder = requestBuilder.load(Uri.parse(imageConfig.getContentProvider()));
        } else if (imageConfig.getResId() > 0) {
            key = String.valueOf(imageConfig.getResId());
            requestBuilder = requestBuilder.load(imageConfig.getResId());
        } else if (imageConfig.getFile() != null) {
            key = imageConfig.getFile().toString();
            requestBuilder = requestBuilder.load(imageConfig.getFile());
        } else if (!TextUtils.isEmpty(imageConfig.getAssertspath())) {
            key = imageConfig.getAssertspath();
            requestBuilder = requestBuilder.load(imageConfig.getAssertspath());
        } else if (!TextUtils.isEmpty(imageConfig.getRawPath())) {
            key = imageConfig.getRawPath();
            requestBuilder = requestBuilder.load(imageConfig.getRawPath());
        } else if (imageConfig.getGlideUrl() != null) {
            key = imageConfig.getGlideUrl().getCacheKey();
            requestBuilder = requestBuilder.load(imageConfig.getGlideUrl());
        } else {
            throw new RuntimeException("No images to load");
        }

        if (imageConfig.getOnProgressListener() != null && !TextUtils.isEmpty(key)) {
            ProgressManager.getInstance().addResponseListener(key, imageConfig.getOnProgressListener());
        }
        return requestBuilder;
    }


    /**
     * 加载参数并设置图片
     *
     * @param request
     * @param imageConfig
     */
    @SuppressLint("CheckResult")
    private void loadOptions(RequestBuilder request, final ImageConfigSpread imageConfig) {
        //设置缩略图
        if (imageConfig.getThumbnail() != 0) {
            request.thumbnail(imageConfig.getThumbnail());
        }
        if (imageConfig.getPlaceholder() > 0) {
            request.placeholder(imageConfig.getPlaceholder());
        }
        if (imageConfig.getErrorPic() > 0) {
            request.error(imageConfig.getErrorPic());
        }

        int scaleMode = imageConfig.getScaleMode();
        switch (scaleMode) {
            case ScaleMode.CENTER_CROP:
                request.centerCrop();
                break;
            case ScaleMode.FIT_CENTER:
                request.fitCenter();
                break;
            default:
                request.centerInside();
                break;
        }
        //设置图片加载的分辨 sp
        if (imageConfig.getoWidth() != 0 && imageConfig.getoHeight() != 0) {
            request.override(imageConfig.getoWidth(), imageConfig.getoHeight());
        }
        //是否跳过内存缓存
        request.skipMemoryCache(imageConfig.isSkipMemoryCache());

        //磁盘缓存策略
        request.diskCacheStrategy(imageConfig.getDiskCacheStrategy());

        ImageLoaderOptionsSwitch.setShapeModeAndBlur(imageConfig, request);
        //设置图片加载优先级
        ImageLoaderOptionsSwitch.setPriority(imageConfig, request);

        if (imageConfig.getTransitionOptions() != null) {
            request.transition(imageConfig.getTransitionOptions());
        }

        if (imageConfig.getTargetView() != null && !imageConfig.isAsFile()) {
            if (imageConfig.isAsBitmap()) {
                request.into(new GlideBitmapImageViewTarget(imageConfig.getTargetView(), imageConfig, key));
            } else if (imageConfig.isAsDrawable()) {
                request.into(new GlideDrawableImageViewTarget(imageConfig.getTargetView(), imageConfig, key));
            } else {
                request.into(imageConfig.getTargetView());
            }
        } else {
            if (imageConfig.isAsBitmap()) {
                ThreadUtils.executeByCached(new GlideDownLoadImageRunable<Bitmap>(request, imageConfig));
            } else if (imageConfig.isAsDrawable()) {
                ThreadUtils.executeByCached(new GlideDownLoadImageRunable<Drawable>(request, imageConfig));
            } else if (imageConfig.isAsFile()) {
                ThreadUtils.executeByCached(new GlideDownLoadImageRunable<File>(request, imageConfig));
            }
        }

    }
}
