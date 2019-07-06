package com.flyang.imageloader.lisenter;

/**
 * @author caoyangfei
 * @ClassName BitmapListener
 * @date 2019/7/3
 * ------------- Description -------------
 * 获取图片的回调
 */
public interface ImageCallBackListener<T> {
    void onSuccess(T type);

    void onFail();
}
