package com.flyang.imageloader.lisenter;

/**
 * @author caoyangfei
 * @ClassName OnProgressListener
 * @date 2019/7/6
 * ------------- Description -------------
 * 加载进度监听
 */
public interface OnProgressListener {
    void onProgress(boolean isComplete, int percentage, long bytesRead, long totalBytes);
}
