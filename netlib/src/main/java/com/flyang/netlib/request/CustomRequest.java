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

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
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
     * 创建api服务  可以支持自定义的api，默认使用BaseApiService,上层不用关心
     *
     * @param service 自定义的apiservice class
     */
    public <T> T create(final Class<T> service) {
        PreconditionUtils.checkNotNull(retrofit, "请先在调用build()才能使用");
        return retrofit.create(service);
    }

    /**
     * 调用call返回一个Observable<T>
     * 举例：如果你给的是一个Observable<ApiResult<AuthModel>> 那么返回的<T>是一个ApiResult<AuthModel>
     */
    public <T> Flowable<T> call(Flowable<T> flowable) {
        PreconditionUtils.checkNotNull(retrofit, "请先在调用build()才能使用");
        return flowable.compose(RxSchedulers.io_main())
                .compose(new HandleErrTransformer())
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    public <T> void call(Flowable<T> flowable, CallBack<T> callBack) {
        call(flowable, new CallBackSubsciber(context, callBack));
    }

    public <R> void call(Flowable flowable, Subscriber<R> subscriber) {
        flowable.compose(RxSchedulers.io_main())
                .subscribe(subscriber);
    }


    /**
     * Flowable,针对ApiResult的业务<T>
     * 举例：如果你给的是一个Observable<ApiResult<AuthModel>> 那么返回的<T>是AuthModel
     */
    public <T> Flowable<T> apiCall(Flowable<ApiResult<T>> flowable) {
        PreconditionUtils.checkNotNull(retrofit, "请先在调用build()才能使用");
        return flowable
                .map(new HandleFuc<T>())
                .compose(RxSchedulers.<T>io_main())
                .compose(new HandleErrTransformer<T>())
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    public <T> Disposable apiCall(Flowable<T> flowable, CallBack<T> callBack) {
        return call(flowable, new CallBackProxy<ApiResult<T>, T>(callBack) {
        });
    }

    public <T> Disposable call(Flowable<T> flowable, CallBackProxy<? extends ApiResult<T>, T> proxy) {
        Flowable<CacheResult<T>> cacheobservable = build().toObservable(flowable, proxy);
        if (CacheResult.class != proxy.getCallBack().getRawType()) {
            return cacheobservable.compose(new FlowableTransformer<CacheResult<T>, T>() {
                @Override
                public Publisher<T> apply(Flowable<CacheResult<T>> upstream) {
                    return upstream.map(new CacheResultFunc<T>());
                }
            }).subscribeWith(new CallBackSubsciber<T>(context, proxy.getCallBack()));
        } else {
            return cacheobservable.subscribeWith(new CallBackSubsciber<CacheResult<T>>(context, proxy.getCallBack()));
        }
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T> Flowable<CacheResult<T>> toObservable(Flowable flowable, CallBackProxy<? extends ApiResult<T>, T> proxy) {
        return flowable.map(new ApiResultFunc(proxy != null ? proxy.getType() : new TypeToken<ResponseBody>() {
        }.getType()))
                .compose(isSyncRequest ? RxSchedulers._main() : RxSchedulers._io_main())
                .compose(rxCache.transformer(cacheMode, proxy.getCallBack().getType()))
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    @Override
    protected Flowable<ResponseBody> generateRequest() {
        return null;
    }
}
