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
import com.flyang.netlib.func.RetryExceptionFunc;
import com.flyang.netlib.model.ApiResult;
import com.flyang.netlib.subsciber.CallBackSubsciber;
import com.flyang.netlib.utils.RxSchedulers;
import com.google.gson.reflect.TypeToken;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.disposables.Disposable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author caoyangfei
 * @ClassName DeleteRequest
 * @date 2019/7/23
 * ------------- Description -------------
 * 删除请求
 */
@SuppressWarnings(value = {"unchecked", "deprecation"})
public class DeleteRequest extends BaseBodyRequest<DeleteRequest> {
    public DeleteRequest(String url) {
        super(url);
    }

    public <T> Disposable execute(CallBack<T> callBack) {
        return execute(new CallBackProxy<ApiResult<T>, T>(callBack) {
        });
    }

    public <T> Disposable execute(CallBackProxy<? extends ApiResult<T>, T> proxy) {
        Flowable<CacheResult<T>> flowable = build().toObservable(generateRequest(), proxy);
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
                .compose(isSyncRequest ? RxSchedulers._main() : RxSchedulers._io_main())
                .compose(rxCache.transformer(cacheMode, proxy.getCallBack().getType()))
                .retryWhen(new RetryExceptionFunc(retryCount, retryDelay, retryIncreaseDelay));
    }

    @Override
    protected Flowable<ResponseBody> generateRequest() {
        if (this.requestBody != null) { //自定义的请求体
            return apiManager.deleteBody(url, this.requestBody);
        } else if (this.json != null) {//Json
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), this.json);
            return apiManager.deleteJson(url, body);
        } else if (this.object != null) {//自定义的请求object
            return apiManager.deleteBody(url, object);
        } else if (this.string != null) {//文本内容
            RequestBody body = RequestBody.create(mediaType, this.string);
            return apiManager.deleteBody(url, body);
        } else {
            return apiManager.delete(url, params.urlParamsMap);
        }
    }
}
