/*
 * Copyright (C) 2017 zhouyou(478319399@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flyang.network.subsciber;

import android.content.Context;

import com.flyang.network.exception.ApiException;
import com.flyang.network.utils.HttpUtils;
import com.flyang.util.log.LogUtils;

import java.lang.ref.WeakReference;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

/**
 * @author caoyangfei
 * @ClassName BaseSubscriber
 * @date 2019/10/17
 * ------------- Description -------------
 * 订阅基类
 * <p>
 * 1.防止内存泄露。
 * 2.在onStart()没有网络时直接onCompleted();
 * 3.统一处理异常
 */
public abstract class BaseSubscriber<T> extends DisposableObserver<T> {
    public WeakReference<Context> contextWeakReference;

    public BaseSubscriber() {
    }

    @Override
    protected void onStart() {
        LogUtils.tag("FlyangHttp").i("-->http is onStart");
        if (contextWeakReference != null && contextWeakReference.get() != null && !HttpUtils.isNetworkAvailable(contextWeakReference.get())) {
            //Toast.makeText(context, "无网络，读取缓存数据", Toast.LENGTH_SHORT).show();
            onComplete();
        }
    }


    public BaseSubscriber(Context context) {
        if (context != null) {
            contextWeakReference = new WeakReference<>(context);
        }
    }

    @Override
    public void onNext(@NonNull T t) {
        LogUtils.tag("FlyangHttp").i("-->http is onNext");
    }

    @Override
    public final void onError(Throwable e) {
        LogUtils.tag("FlyangHttp").e("-->http is onError");
        if (e instanceof ApiException) {
            LogUtils.tag("FlyangHttp").e("--> e instanceof ApiException err:" + e);
            onError((ApiException) e);
        } else {
            LogUtils.tag("FlyangHttp").e("--> e !instanceof ApiException err:" + e);
            onError(ApiException.handleException(e));
        }
    }

    @Override
    public void onComplete() {
        LogUtils.tag("FlyangHttp").i("-->http is onComplete");
    }


    public abstract void onError(ApiException e);

}
