package com.flyang.imageloader.loader;

import com.bumptech.glide.MemoryCategory;
import com.flyang.imageloader.config.ImageConfig;

/**
 * @author yangfei.cao
 * @ClassName netlib_demo
 * @date 2019/7/4
 * ------------- Description -------------
 * 策略模式加载接口
 */
public interface ImageLoaderStrategy<T extends ImageConfig> {

    void init(int cacheSizeInM, MemoryCategory memoryCategory, boolean isInternalCD);

    void showImage(T imageConfig);

    void clearDiskCache();

    void clearMomory();

    void pauseRequests();

    void resumeRequests();

    void trimMemory(int level);

    void clearAllMemoryCaches();

}
