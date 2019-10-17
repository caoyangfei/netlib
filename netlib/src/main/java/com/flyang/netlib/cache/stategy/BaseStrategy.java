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

package com.flyang.netlib.cache.stategy;


import android.annotation.SuppressLint;

import com.flyang.netlib.cache.RxCache;
import com.flyang.netlib.cache.model.CacheResult;
import com.flyang.util.log.LogUtils;

import java.lang.reflect.Type;
import java.util.ConcurrentModificationException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * @author caoyangfei
 * @ClassName BaseStrategy
 * @date 2019/7/22
 * ------------- Description -------------
 * 实现缓存策略的基类
 */
public abstract class BaseStrategy implements IStrategy {

    /**
     * 加载缓存
     *
     * @param rxCache
     * @param type
     * @param key
     * @param time
     * @param needEmpty
     * @param <T>
     * @return
     */
    protected <T> Observable<CacheResult<T>> loadCache(final RxCache rxCache, Type type, final String key, final long time, final boolean needEmpty) {
        Observable<CacheResult<T>> observable = rxCache.<T>load(type, key, time).flatMap(new Function<T, ObservableSource<CacheResult<T>>>() {
            @Override
            public ObservableSource<CacheResult<T>> apply(T t) throws Exception {
                if (t == null) {
                    return Observable.error(new NullPointerException("Not find the cache!"));
                }
                return Observable.just(new CacheResult<T>(true, t));
            }
        });
        if (needEmpty) {
            observable = observable
                    .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends CacheResult<T>>>() {
                        @Override
                        public ObservableSource<? extends CacheResult<T>> apply(Throwable throwable) throws Exception {
                            return Observable.empty();
                        }
                    });
        }
        return observable;
    }

    /**
     * 请求成功后：异步保存
     *
     * @param rxCache
     * @param key
     * @param source
     * @param needEmpty
     * @param <T>
     * @return
     */
    protected <T> Observable<CacheResult<T>> loadRemote2(final RxCache rxCache, final String key, Observable<T> source, final boolean needEmpty) {
        Observable<CacheResult<T>> observable = source
                .map(new Function<T, CacheResult<T>>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public CacheResult<T> apply(T t) throws Exception {
                        LogUtils.i("loadRemote result=" + t);
                        rxCache.save(key, t).subscribeOn(Schedulers.io())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(@NonNull Boolean status) throws Exception {
                                        LogUtils.i("save status => " + status);
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(@NonNull Throwable throwable) throws Exception {
                                        if (throwable instanceof ConcurrentModificationException) {
                                            LogUtils.i("Save failed, please use a synchronized cache strategy :", throwable);
                                        } else {
                                            LogUtils.i(throwable.getMessage());
                                        }
                                    }
                                });
                        return new CacheResult<T>(false, t);
                    }
                });
        if (needEmpty) {
            observable = observable
                    .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends CacheResult<T>>>() {
                        @Override
                        public ObservableSource<? extends CacheResult<T>> apply(Throwable throwable) throws Exception {
                            return Observable.empty();
                        }
                    });
        }
        return observable;
    }

    /**
     * 请求成功后：同步保存
     *
     * @param rxCache
     * @param key
     * @param source
     * @param needEmpty
     * @param <T>
     * @return
     */
    protected <T> Observable<CacheResult<T>> loadRemote(final RxCache rxCache, final String key, Observable<T> source, final boolean needEmpty) {
        Observable<CacheResult<T>> observable = source
                .map(new Function<T, CacheResult<T>>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public CacheResult<T> apply(@NonNull T t) throws Exception {
                        rxCache.save(key, t).subscribeOn(Schedulers.io())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(@NonNull Boolean status) throws Exception {
                                        LogUtils.i("save status => " + status);
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(@NonNull Throwable throwable) throws Exception {
                                        if (throwable instanceof ConcurrentModificationException) {
                                            LogUtils.i("Save failed, please use a synchronized cache strategy :", throwable);
                                        } else {
                                            LogUtils.i(throwable.getMessage());
                                        }
                                    }
                                });

                        return new CacheResult<T>(false, t);
                    }
                });
        if (needEmpty) {
            observable = observable
                    .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends CacheResult<T>>>() {
                        @Override
                        public ObservableSource<? extends CacheResult<T>> apply(Throwable throwable) throws Exception {
                            return Observable.empty();
                        }
                    });
        }
        return observable;
    }
}
