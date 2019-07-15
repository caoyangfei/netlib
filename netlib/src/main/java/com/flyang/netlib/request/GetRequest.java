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
import com.flyang.netlib.callback.CallClazzProxy;
import com.flyang.netlib.func.ApiResultFunc;
import com.flyang.netlib.func.CacheResultFunc;
import com.flyang.netlib.func.RetryExceptionFunc;
import com.flyang.netlib.model.ApiResult;
import com.flyang.netlib.subsciber.CallBackSubsciber;
import com.flyang.netlib.utils.RxUtil;
import com.google.gson.reflect.TypeToken;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.lang.reflect.Type;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import okhttp3.ResponseBody;

/**
 * <p>描述：get请求</p>
 * 作者： zhouyou<br>
 * 日期： 2017/4/28 14:28 <br>
 * 版本： v1.0<br>
 */
@SuppressWarnings(value = {"unchecked", "deprecation"})
public class GetRequest extends BaseRequest<GetRequest> {
    public GetRequest(String url) {
        super(url);
    }

    public <T> Flowable<T> execute(Class<T> clazz) {
        return execute(new CallClazzProxy<ApiResult<T>, T>(clazz) {
        });
    }

    public <T> Flowable<T> execute(Type type) {
        return execute(new CallClazzProxy<ApiResult<T>, T>(type) {
        });
    }

    public <T> Flowable<T> execute(CallClazzProxy<? extends ApiResult<T>, T> proxy) {
        return build().generateRequest()
                .map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._main() : RxUtil._io_main())
                .compose(rxCache.transformer(cacheMode, proxy.getCallType()))
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay))
                .compose(new FlowableTransformer() {
                    @Override
                    public Publisher apply(Flowable upstream) {
                        return upstream.map(new CacheResultFunc<T>());
                    }
                });
    }

    public <T> Subscriber execute(CallBack<T> callBack) {
        return execute(new CallBackProxy<ApiResult<T>, T>(callBack) {
        });
    }

    public <T> Subscriber execute(CallBackProxy<? extends ApiResult<T>, T> proxy) {
        Flowable<CacheResult<T>> flowable = build().toObservable(apiManager.get(url, params.urlParamsMap), proxy);
        if (CacheResult.class != proxy.getCallBack().getRawType()) {
            return flowable.compose(new FlowableTransformer<CacheResult<T>, T>() {
                @Override
                public Publisher<T> apply(Flowable<CacheResult<T>> upstream) {
                    return upstream.map(new CacheResultFunc<T>());
                }
            }).subscribeWith(new CallBackSubsciber<T>(context, proxy.getCallBack()));
        } else {
            return flowable.subscribeWith(new CallBackSubsciber<CacheResult<T>>(context, proxy.getCallBack()));
        }
    }

    private <T> Flowable<CacheResult<T>> toObservable(Flowable flowable, CallBackProxy<? extends ApiResult<T>, T> proxy) {
        return flowable.map(new ApiResultFunc(proxy != null ? proxy.getType() : new TypeToken<ResponseBody>() {
        }.getType()))
                .compose(isSyncRequest ? RxUtil._main() : RxUtil._io_main())
                .compose(rxCache.transformer(cacheMode, proxy.getCallBack().getType()))
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    @Override
    protected Flowable<ResponseBody> generateRequest() {
        return apiManager.get(url, params.urlParamsMap);
    }
}
