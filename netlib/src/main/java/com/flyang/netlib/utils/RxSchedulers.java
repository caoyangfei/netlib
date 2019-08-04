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

package com.flyang.netlib.utils;


import com.flyang.netlib.func.HandleFuc;
import com.flyang.netlib.func.HttpResponseFunc;
import com.flyang.netlib.model.ApiResult;
import com.flyang.util.log.LogUtils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * @author caoyangfei
 * @ClassName RxSchedulers
 * @date 2019/7/22
 * ------------- Description -------------
 * RXjava线程工具类
 */
public class RxSchedulers {

    /**
     * @param <T>
     * @return
     */
    public static <T> FlowableTransformer<T, T> io_main() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(Flowable<T> flowable) {
                return flowable
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .doOnSubscribe(new Consumer<Subscription>() {
                            @Override
                            public void accept(Subscription subscription) throws Exception {
                                LogUtils.i("+++doOnSubscribe+++");
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtils.i("+++doFinally+++");
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 切换到I/O线程
     *
     * @param <T>
     * @return
     */
    public static <T> FlowableTransformer<ApiResult<T>, T> _io_main() {
        return new FlowableTransformer<ApiResult<T>, T>() {

            @Override
            public Publisher<T> apply(Flowable<ApiResult<T>> flowable) {
                return flowable
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new HandleFuc<T>())
                        .doOnSubscribe(new Consumer<Subscription>() {
                            @Override
                            public void accept(Subscription subscription) throws Exception {

                                LogUtils.i("+++doOnSubscribe+++");
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtils.i("+++doFinally+++");
                            }
                        })
                        .onErrorResumeNext(new HttpResponseFunc<T>());
            }
        };
    }

    /**
     * 切换到主线程
     *
     * @param <T>
     * @return
     */
    public static <T> FlowableTransformer<ApiResult<T>, T> _main() {
        return new FlowableTransformer<ApiResult<T>, T>() {

            @Override
            public Publisher<T> apply(Flowable<ApiResult<T>> flowable) {
                return flowable
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new HandleFuc<T>())
                        .doOnSubscribe(new Consumer<Subscription>() {
                            @Override
                            public void accept(Subscription subscription) throws Exception {
                                LogUtils.i("+++doOnSubscribe+++");
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtils.i("+++doFinally+++");
                            }
                        })
                        .onErrorResumeNext(new HttpResponseFunc<T>());
            }
        };

    }
}
