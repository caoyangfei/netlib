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

package com.flyang.netlib.request;


import com.flyang.netlib.cache.model.CacheResult;
import com.flyang.netlib.callback.CallBack;
import com.flyang.netlib.callback.CallBackProxy;
import com.flyang.netlib.func.ApiResultFunc;
import com.flyang.netlib.func.CacheResultFunc;
import com.flyang.netlib.func.HandleFuc;
import com.flyang.netlib.func.RetryExceptionFunc;
import com.flyang.netlib.model.ApiResult;
import com.flyang.netlib.subsciber.CallBackSubsciber;
import com.flyang.netlib.transformer.HandleErrTransformer;
import com.flyang.netlib.utils.RxSchedulers;
import com.flyang.util.data.PreconditionUtils;
import com.google.gson.reflect.TypeToken;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * @author caoyangfei
 * @ClassName CustomRequest
 * @date 2019/7/23
 * ------------- Description -------------
 * 自定义请求，例如你有自己的ApiService
 */
@SuppressWarnings(value = {"unchecked", "deprecation"})
public class CustomRequest extends BaseRequest<CustomRequest> {
    public CustomRequest() {
        super("");
    }

    @Override
    public CustomRequest build() {
        return super.build();
    }

    /**
     * 创建api服务  可以支持自定义的api，默认使用ApiService,上层不用关心
     *
     * @param service 自定义的apiservice class
     */
    public <T> T create(final Class<T> service) {
        PreconditionUtils.checkNotNull(retrofit, "请先在调用build()才能使用");
        return retrofit.create(service);
    }


    /**
     * 返回Observable
     * <p>
     * 可以自定义返回实体继承ApiResult
     */
    public <T> Observable<T> call(Observable<T> observable) {
        PreconditionUtils.checkNotNull(retrofit, "请先在调用build()才能使用");
        return observable.compose(RxSchedulers.io_main())
                .compose(new HandleErrTransformer())
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    /**
     * CallBack回调处理结果
     */
    public <T> void call(Observable<T> observable, CallBack<T> callBack) {
        call(observable, new CallBackSubsciber(context, callBack));
    }

    /**
     * CallBack回调处理结果
     */
    public <R> void call(Observable observable, Observer<R> subscriber) {
        observable.compose(RxSchedulers.io_main())
                .subscribe(subscriber);
    }


    /**
     * 返回Observable<AuthModel>
     *
     * @param observable
     * @param <T>
     * @return
     */
    public <T> Observable<T> apiCall(Observable<ApiResult<T>> observable) {
        PreconditionUtils.checkNotNull(retrofit, "请先在调用build()才能使用");
        return observable
                .map(new HandleFuc<T>())
                .compose(RxSchedulers.<T>io_main())
                .compose(new HandleErrTransformer<T>())
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    /**
     * CallBack回调处理结果
     * <p>
     * 默认使用ApiResult
     */
    public <T> Disposable apiCall(Observable<T> observable, CallBack<T> callBack) {
        return call(observable, new CallBackProxy<ApiResult<T>, T>(callBack) {
        });
    }

    /**
     * CallBack回调处理结果
     * <p>
     * 自定义返回实体继承ApiResult
     */
    public <T> Disposable call(Observable<T> observable, CallBackProxy<? extends ApiResult<T>, T> proxy) {
        Observable<CacheResult<T>> cacheobservable = build().toObservable(observable, proxy);
        if (CacheResult.class != proxy.getCallBack().getRawType()) {
            return cacheobservable.compose(new ObservableTransformer<CacheResult<T>, T>() {
                @Override
                public ObservableSource<T> apply(@NonNull Observable<CacheResult<T>> observable) {
                    return observable.map(new CacheResultFunc<T>());
                }
            }).subscribeWith(new CallBackSubsciber<T>(context, proxy.getCallBack()));
        } else {
            return cacheobservable.subscribeWith(new CallBackSubsciber<CacheResult<T>>(context, proxy.getCallBack()));
        }
    }

    @SuppressWarnings(value = {"unchecked"})
    private <T> Observable<CacheResult<T>> toObservable(Observable observable, CallBackProxy<? extends ApiResult<T>, T> proxy) {
        return observable.map(new ApiResultFunc(proxy != null ? proxy.getType() : new TypeToken<ResponseBody>() {
        }.getType()))
                .compose(isSyncRequest ? RxSchedulers._main() : RxSchedulers._io_main())
                .compose(rxCache.transformer(cacheMode, proxy.getCallBack().getType()))
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    @Override
    protected Observable<ResponseBody> generateRequest() {
        return null;
    }
}
