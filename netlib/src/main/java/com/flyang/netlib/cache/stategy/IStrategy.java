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

/**
 * @author caoyangfei
 * @ClassName IStrategy
 * @date 2019/7/22
 * ------------- Description -------------
 * 实现缓存策略的接口
 * {@link CacheAndRemoteDistinctStrategy 先显示缓存，再请求网络;
 * @link CacheAndRemoteStrategy  先显示缓存，再请求网络;
 * @link FirstCacheStategy  先显示缓存，缓存不存在，再请求网络;
 * @link FirstRemoteStrategy  先请求网络，网络请求失败，再加载缓存;
 * @link NoStrategy  网络加载，不缓存;
 * @link OnlyCacheStrategy  只读缓存;
 * @link OnlyRemoteStrategy  只请求网络;}
 * <p>
 * 自定义缓存实现方式，实现该接口
 */
public interface IStrategy {

    /**
     * 执行缓存
     *
     * @param rxCache   缓存管理对象
     * @param cacheKey  缓存key
     * @param cacheTime 缓存时间
     * @param source    网络请求对象
     * @param type      要转换的目标对象
     * @return 返回带缓存的Observable流对象
     */
    <T> Flowable<CacheResult<T>> execute(RxCache rxCache, String cacheKey, long cacheTime, Flowable<T> source, Type type);

}
