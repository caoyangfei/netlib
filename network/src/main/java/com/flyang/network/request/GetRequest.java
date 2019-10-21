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

package com.flyang.network.request;


import com.flyang.network.cache.model.CacheResult;
import com.flyang.network.callback.CallBack;
import com.flyang.network.callback.CallBackProxy;
import com.flyang.network.callback.CallClazzProxy;
import com.flyang.network.func.ApiResultFunc;
import com.flyang.network.func.CacheResultFunc;
import com.flyang.network.func.RetryExceptionFunc;
import com.flyang.network.model.ApiResult;
import com.flyang.network.subsciber.CallBackSubsciber;
import com.flyang.network.utils.RxSchedulers;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * @author caoyangfei
 * @ClassName GetRequest
 * @date 2019/7/23
 * ------------- Description -------------
 * get请求
 */
@SuppressWarnings(value = {"unchecked", "deprecation"})
public class GetRequest extends BaseRequest<GetRequest> {
    public GetRequest(String url) {
        super(url);
    }

    /**
     * 默认
     * {@link ApiResult}
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Observable<T> execute(Class<T> clazz) {
        return execute(new CallClazzProxy<ApiResult<T>, T>(clazz) {
        });
    }

    /**
     * 默认
     * {@link ApiResult}
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> Observable<T> execute(Type type) {
        return execute(new CallClazzProxy<ApiResult<T>, T>(type) {
        });
    }

    /**
     * 拓展使用
     * {@link CallClazzProxy}代理
     * <p>
     * 继承{@link ApiResult}
     *
     * @param proxy
     * @param <T>
     * @return
     */
    public <T> Observable<T> execute(CallClazzProxy<? extends ApiResult<T>, T> proxy) {
        return build().generateRequest()
                .map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxSchedulers._main() : RxSchedulers._io_main())
                .compose(rxCache.transformer(cacheMode, proxy.getCallType()))
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay))
                .compose(new ObservableTransformer() {
                    @Override
                    public ObservableSource apply(@NonNull Observable observable) {
                        //无论是否启用缓存都转换为泛型对象返回
                        return observable.map(new CacheResultFunc<T>());
                    }
                });
    }

    /**
     * 默认ApiResult
     * {@link ApiResult}
     *
     * @param callBack
     * @param <T>
     * @return
     */
    public <T> Disposable execute(CallBack<T> callBack) {
        return execute(new CallBackProxy<ApiResult<T>, T>(callBack) {
        });
    }

    /**
     * 拓展使用继承ApiResult
     * {@link CallBackProxy}代理
     * <p>
     * 继承{@link ApiResult}
     *
     * @param proxy
     * @param <T>
     * @return
     */
    public <T> Disposable execute(CallBackProxy<? extends ApiResult<T>, T> proxy) {
        Observable<CacheResult<T>> observable = build().toFlowable(apiManager.get(url, params.urlParamsMap), proxy);
        if (CacheResult.class != proxy.getCallBack().getRawType()) {
            return observable.compose(new ObservableTransformer<CacheResult<T>, T>() {
                @Override
                public ObservableSource<T> apply(@NonNull Observable<CacheResult<T>> observable) {
                    //没有启用缓存时,转换成泛型对象返回
                    return observable.map(new CacheResultFunc<T>());
                }
            }).subscribeWith(new CallBackSubsciber<T>(context, proxy.getCallBack()));
        } else {
            //启用缓存时,返回CacheResult对象
            return observable.subscribeWith(new CallBackSubsciber<CacheResult<T>>(context, proxy.getCallBack()));
        }
    }

    private <T> Observable<CacheResult<T>> toFlowable(Observable observable, CallBackProxy<? extends ApiResult<T>, T> proxy) {
        return observable.map(new ApiResultFunc(proxy != null ? proxy.getType() : new TypeToken<ResponseBody>() {
        }.getType()))
                .compose(isSyncRequest ? RxSchedulers._main() : RxSchedulers._io_main())
                .compose(rxCache.transformer(cacheMode, proxy.getCallBack().getType()))
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    @Override
    protected Observable<ResponseBody> generateRequest() {
        return apiManager.get(url, params.urlParamsMap);
    }
}
