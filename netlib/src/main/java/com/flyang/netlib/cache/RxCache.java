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

package com.flyang.netlib.cache;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.flyang.netlib.cache.converter.IDiskConverter;
import com.flyang.netlib.cache.converter.SerializableDiskConverter;
import com.flyang.netlib.cache.core.CacheCore;
import com.flyang.netlib.cache.core.LruDiskCache;
import com.flyang.netlib.cache.model.CacheMode;
import com.flyang.netlib.cache.model.CacheResult;
import com.flyang.netlib.cache.stategy.IStrategy;
import com.flyang.netlib.utils.Utils;
import com.flyang.util.data.PreconditionUtils;
import com.flyang.util.log.LogUtils;

import org.reactivestreams.Publisher;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.Exceptions;


/**
 * <p>描述：缓存统一入口类</p>
 * <p>
 * <p>主要实现技术：Rxjava+DiskLruCache(jakewharton大神开源的LRU库)</p>
 * <p>
 * <p>
 * 主要功能：<br>
 * 1.可以独立使用，单独用RxCache来存储数据<br>
 * 2.采用transformer与网络请求结合，可以实现网络缓存功能,本地硬缓存<br>
 * 3.可以保存缓存 （异步）<br>
 * 4.可以读取缓存（异步）<br>
 * 5.可以判断缓存是否存在<br>
 * 6.根据key删除缓存<br>
 * 7.清空缓存（异步）<br>
 * 8.缓存Key会自动进行MD5加密<br>
 * 9.其它参数设置：缓存磁盘大小、缓存key、缓存时间、缓存存储的转换器、缓存目录、缓存Version<br>
 * <p>
 * <p>
 * 使用说明：<br>
 * RxCache rxCache = new RxCache.Builder(this)<br>
 * .appVersion(1)//不设置，默认为1</br>
 * .diskDir(new File(getCacheDir().getPath() + File.separator + "data-cache"))//不设置，默认使用缓存路径<br>
 * .diskConverter(new SerializableDiskConverter())//目前只支持Serializable缓存<br>
 * .diskMax(20*1024*1024)//不设置， 默为认50MB<br>
 * .build();</br>
 * </P>
 * 作者： zhouyou<br>
 * 日期： 2016/12/24 10:35<br>
 * 版本： v2.0<br>
 */
public final class RxCache {
    private final Context context;
    private final CacheCore cacheCore;                                  //缓存的核心管理类
    private final String cacheKey;                                      //缓存的key
    private final long cacheTime;                                       //缓存的时间 单位:秒
    private final IDiskConverter diskConverter;                         //缓存的转换器
    private final File diskDir;                                         //缓存的磁盘目录，默认是缓存目录
    private final int appVersion;                                       //缓存的版本
    private final long diskMaxSize;                                     //缓存的磁盘大小

    public RxCache() {
        this(new Builder());
    }

    private RxCache(Builder builder) {
        this.context = builder.context;
        this.cacheKey = builder.cachekey;
        this.cacheTime = builder.cacheTime;
        this.diskDir = builder.diskDir;
        this.appVersion = builder.appVersion;
        this.diskMaxSize = builder.diskMaxSize;
        this.diskConverter = builder.diskConverter;
        cacheCore = new CacheCore(new LruDiskCache(diskConverter, diskDir, appVersion, diskMaxSize));
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * 缓存transformer
     *
     * @param cacheMode 缓存类型
     * @param type      缓存clazz
     */
    @SuppressWarnings(value = {"unchecked", "deprecation"})
    public <T> FlowableTransformer<T, CacheResult<T>> transformer(CacheMode cacheMode, final Type type) {
        final IStrategy strategy = loadStrategy(cacheMode);//获取缓存策略
        return new FlowableTransformer<T, CacheResult<T>>() {
            @Override
            public Publisher<CacheResult<T>> apply(Flowable<T> upstream) {
                LogUtils.i("cackeKey=" + RxCache.this.cacheKey);
                Type tempType = type;
                if (type instanceof ParameterizedType) {//自定义ApiResult
                    Class<T> cls = (Class) ((ParameterizedType) type).getRawType();
                    if (CacheResult.class.isAssignableFrom(cls)) {
                        tempType = Utils.getParameterizedType(type, 0);
                    }
                }
                return strategy.execute(RxCache.this, RxCache.this.cacheKey, RxCache.this.cacheTime, upstream, tempType);
            }
        };
    }

    private static abstract class SimpleSubscribe<T> implements FlowableOnSubscribe<T> {
        @Override
        public void subscribe(@NonNull FlowableEmitter<T> subscriber) throws Exception {
            try {
                T data = execute();
                if (!subscriber.isCancelled()) {
                    subscriber.onNext(data);
                }
            } catch (Throwable e) {
                LogUtils.e(e.getMessage());
                if (!subscriber.isCancelled()) {
                    subscriber.onError(e);
                }
                Exceptions.throwIfFatal(e);
                //RxJavaPlugins.onError(e);
                return;
            }

            if (!subscriber.isCancelled()) {
                subscriber.onComplete();
            }
        }

        abstract T execute() throws Throwable;
    }

    /**
     * 获取缓存
     *
     * @param type 保存的类型
     * @param key  缓存key
     */
    public <T> Flowable<T> load(final Type type, final String key) {
        return load(type, key, -1);
    }

    /**
     * 根据时间读取缓存
     *
     * @param type 保存的类型
     * @param key  缓存key
     * @param time 保存时间
     */
    public <T> Flowable<T> load(final Type type, final String key, final long time) {
        return Flowable.create(new SimpleSubscribe<T>() {
            @Override
            T execute() throws Throwable {
                return cacheCore.load(type, key, time);
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * 保存
     *
     * @param key   缓存key
     * @param value 缓存Value
     */
    public <T> Flowable<Boolean> save(final String key, final T value) {
        return Flowable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                cacheCore.save(key, value);
                return true;
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * 是否包含
     */
    public Flowable<Boolean> containsKey(final String key) {
        return Flowable.create(new SimpleSubscribe<Boolean>() {

            @Override
            Boolean execute() throws Throwable {
                return cacheCore.containsKey(key);
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * 删除缓存
     */
    public Flowable<Boolean> remove(final String key) {
        return Flowable.create(new SimpleSubscribe<Boolean>() {

            @Override
            Boolean execute() throws Throwable {
                return cacheCore.remove(key);
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * 清空缓存
     */
    public Flowable<Boolean> clear() {
        return Flowable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return cacheCore.clear();
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * 利用反射，加载缓存策略模型
     */
    private IStrategy loadStrategy(CacheMode cacheMode) {
        try {
            String pkName = IStrategy.class.getPackage().getName();
            return (IStrategy) Class.forName(pkName + "." + cacheMode.getClassName()).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("loadStrategy(" + cacheMode + ") err!!" + e.getMessage());
        }
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public Context getContext() {
        return context;
    }

    public CacheCore getCacheCore() {
        return cacheCore;
    }

    public IDiskConverter getDiskConverter() {
        return diskConverter;
    }

    public File getDiskDir() {
        return diskDir;
    }

    public int getAppVersion() {
        return appVersion;
    }

    public long getDiskMaxSize() {
        return diskMaxSize;
    }

    public static final class Builder {
        private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
        private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
        public static final long CACHE_NEVER_EXPIRE = -1;//永久不过期
        private int appVersion;
        private long diskMaxSize;
        private File diskDir;
        private IDiskConverter diskConverter;
        private Context context;
        private String cachekey;
        private long cacheTime;

        public Builder() {
            diskConverter = new SerializableDiskConverter();
            cacheTime = CACHE_NEVER_EXPIRE;
            appVersion = 1;
        }

        public Builder(RxCache rxCache) {
            this.context = rxCache.context;
            this.appVersion = rxCache.appVersion;
            this.diskMaxSize = rxCache.diskMaxSize;
            this.diskDir = rxCache.diskDir;
            this.diskConverter = rxCache.diskConverter;
            this.context = rxCache.context;
            this.cachekey = rxCache.cacheKey;
            this.cacheTime = rxCache.cacheTime;
        }

        public Builder init(Context context) {
            this.context = context;
            return this;
        }

        /**
         * 不设置，默认为1
         */
        public Builder appVersion(int appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        /**
         * 默认为缓存路径
         *
         * @param directory
         * @return
         */
        public Builder diskDir(File directory) {
            this.diskDir = directory;
            return this;
        }


        public Builder diskConverter(IDiskConverter converter) {
            this.diskConverter = converter;
            return this;
        }

        /**
         * 不设置， 默为认50MB
         */
        public Builder diskMax(long maxSize) {
            this.diskMaxSize = maxSize;
            return this;
        }

        public Builder cachekey(String cachekey) {
            this.cachekey = cachekey;
            return this;
        }

        public Builder cacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        public RxCache build() {
            if (this.diskDir == null && this.context != null) {
                this.diskDir = getDiskCacheDir(this.context, "data-cache");
            }
            PreconditionUtils.checkNotNull(this.diskDir, "diskDir==null");
            if (!this.diskDir.exists()) {
                this.diskDir.mkdirs();
            }
            if (this.diskConverter == null) {
                this.diskConverter = new SerializableDiskConverter();
            }
            if (diskMaxSize <= 0) {
                diskMaxSize = calculateDiskCacheSize(diskDir);
            }
            cacheTime = Math.max(CACHE_NEVER_EXPIRE, this.cacheTime);

            appVersion = Math.max(1, this.appVersion);

            return new RxCache(this);
        }

        @SuppressWarnings("deprecation")
        private static long calculateDiskCacheSize(File dir) {
            long size = 0;
            try {
                StatFs statFs = new StatFs(dir.getAbsolutePath());
                long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
                size = available / 50;
            } catch (IllegalArgumentException ignored) {
            }
            return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
        }

        /**
         * 应用程序缓存原理：
         * 1.当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，否则就调用getCacheDir()方法来获取缓存路径<br>
         * 2.前者是/sdcard/Android/data/<application package>/cache 这个路径<br>
         * 3.后者获取到的是 /data/data/<application package>/cache 这个路径<br>
         *
         * @param uniqueName 缓存目录
         */
        public File getDiskCacheDir(Context context, String uniqueName) {
            File cacheDir;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
                cacheDir = context.getExternalCacheDir();
            } else {
                cacheDir = context.getCacheDir();
            }
            if (cacheDir == null) {// if cacheDir is null throws NullPointerException
                cacheDir = context.getCacheDir();
            }
            return new File(cacheDir.getPath() + File.separator + uniqueName);
        }

    }
}
