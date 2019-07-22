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


import com.flyang.netlib.cache.RxCache;
import com.flyang.netlib.cache.model.CacheResult;

import java.lang.reflect.Type;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okio.ByteString;

/**
 * @author caoyangfei
 * @ClassName CacheAndRemoteDistinctStrategy
 * @date 2019/7/22
 * ------------- Description -------------
 * 先显示缓存，再请求网络(反射使用)
 */
public final class CacheAndRemoteDistinctStrategy extends BaseStrategy {
    @Override
    public <T> Flowable<CacheResult<T>> execute(RxCache rxCache, String key, long time, Flowable<T> source, Type type) {
        Flowable<CacheResult<T>> cache = loadCache(rxCache, type, key, time, true);
        Flowable<CacheResult<T>> remote = loadRemote(rxCache, key, source, false);
        return Flowable.concat(cache, remote)
                .filter(new Predicate<CacheResult<T>>() {
                    @Override
                    public boolean test(@NonNull CacheResult<T> tCacheResult) throws Exception {
                        return tCacheResult != null && tCacheResult.data != null;
                    }
                }).distinctUntilChanged(new Function<CacheResult<T>, String>() {
                    @Override
                    public String apply(@NonNull CacheResult<T> tCacheResult) throws Exception {
                        return ByteString.of(tCacheResult.data.toString().getBytes()).md5().hex();
                    }
                });
    }

}
