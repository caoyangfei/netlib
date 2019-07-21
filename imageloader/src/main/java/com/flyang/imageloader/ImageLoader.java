package com.flyang.imageloader;

import android.content.Context;

import com.bumptech.glide.MemoryCategory;
import com.flyang.imageloader.config.ImageConfigSpread;
import com.flyang.imageloader.loader.ImageLoaderStrategy;
import com.flyang.util.data.PreconditionUtils;

/**
 * @author yangfei.cao
 * @ClassName ImageLoader
 * @Description: 加载接口
 * @date 2017/7/18/018
 */
public class ImageLoader<T extends ImageLoaderStrategy, R extends Runnable> {


    private static ImageLoaderStrategy imageLoaderStrategy;

    public static <T extends ImageLoaderStrategy> void init(T ImageLoaderStrategy) {
        init(ImageLoaderStrategy, ImageConfigSpread.CACHE_IMAGE_SIZE);
    }

    public static <T extends ImageLoaderStrategy> void init(T ImageLoaderStrategy, int cacheSizeInM) {
        init(ImageLoaderStrategy, cacheSizeInM, MemoryCategory.NORMAL);
    }

    public static <T extends ImageLoaderStrategy> void init(T ImageLoaderStrategy, int cacheSizeInM, MemoryCategory memoryCategory) {
        init(ImageLoaderStrategy, cacheSizeInM, memoryCategory, true);
    }

    public static <T extends ImageLoaderStrategy> void init(T ImageLoaderStrategy, int cacheSizeInM, MemoryCategory memoryCategory, boolean isInternalCD) {
        imageLoaderStrategy = ImageLoaderStrategy;
        imageLoaderStrategy.init(cacheSizeInM, memoryCategory, isInternalCD);
    }

    /**
     * 获取当前的Loader
     *
     * @return
     */
    public static ImageLoaderStrategy getActualLoader() {
        PreconditionUtils.checkNotNull(imageLoaderStrategy, "Not initialized ImageLoader");
        return imageLoaderStrategy;
    }

    /**
     * 链式结构加载图片
     *
     * @return
     */
    public static ImageConfigSpread.Builder with() {
        ImageConfigSpread.Builder with = with(null);
        return with;
    }

    /**
     * 链式结构加载图片
     *
     * @return
     */
    public static ImageConfigSpread.Builder with(Context context) {
        ImageConfigSpread.Builder builder = new ImageConfigSpread.Builder(context);
        return builder;
    }

    /**
     * 清除所有磁盘缓存
     */
    public static void clearDiskCache() {
        getActualLoader().clearDiskCache();
    }

    /**
     * 清除内存缓存
     */
    public static void clearMomory() {
        getActualLoader().clearMomory();
    }

    public static void resumeRequests() {
        getActualLoader().resumeRequests();
    }

    public static void pauseRequests() {
        getActualLoader().pauseRequests();
    }

    public static void trimMemory(int level) {
        getActualLoader().trimMemory(level);
    }

    public static void clearAllMemoryCaches() {
        getActualLoader().clearAllMemoryCaches();
    }
}
