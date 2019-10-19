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

import com.flyang.netlib.cache.converter.CacheFactory;
import com.flyang.netlib.cache.converter.CacheType;
import com.flyang.netlib.cache.model.CacheMode;
import com.flyang.netlib.cache.model.CacheResult;
import com.flyang.netlib.cache.stategy.IStrategy;
import com.flyang.netlib.utils.HttpUtils;
import com.flyang.util.cache.CacheDiskUtils;
import com.flyang.util.cache.CacheDoubleStaticUtils;
import com.flyang.util.cache.CacheDoubleUtils;
import com.flyang.util.cache.CacheMemoryUtils;
import com.flyang.util.data.PreconditionUtils;
import com.flyang.util.log.LogUtils;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.Exceptions;

/**
 * @author caoyangfei
 * @ClassName RxCache
 * @date 2019/7/24
 * ------------- Description -------------
 * 缓存
 * <p>
 * 1.可以独立使用，单独用RxCache来存储数据
 * 2.采用transformer与网络请求结合，可以实现网络缓存功能,本地硬缓存
 * 3.可以保存缓存 （异步）
 * 4.可以读取缓存（异步）
 * 6.根据key删除缓存
 * 7.清空缓存（异步）
 * 8.缓存Key会自动进行MD5加密
 * 9.其它参数设置：缓存磁盘大小、缓存key、缓存时间、缓存类型、缓存目录
 * </p>
 * <p>
 * RxCache rxCache = new RxCache.Builder(this)
 * .diskDir(new File(getCacheDir().getPath() + File.separator + "data-cache"))//不设置，默认使用缓存路径
 * .diskConverter(new SerializableDiskConverter())//目前只支持Serializable缓存
 * .diskMax(20*1024*1024)//不设置， 默为认50MB
 * .build();
 */
public final class RxCache {
    private final Context context;
    private final String cacheKey;                                      //缓存的key
    private final int cacheTime;                                        //缓存的时间 单位:秒
    private final File diskDir;                                         //缓存的磁盘目录，默认是缓存目录
    private final long diskMaxSize;                                     //缓存的磁盘大小
    private final CacheType cacheType;                                  //缓存类型

    public RxCache() {
        this(new Builder());
    }

    private RxCache(Builder builder) {
        this.context = builder.context;
        this.cacheKey = builder.cachekey;
        this.cacheTime = builder.cacheTime;
        this.diskDir = builder.diskDir;
        this.diskMaxSize = builder.diskMaxSize;
        this.cacheType = builder.cacheType;

        CacheMemoryUtils cacheMemoryUtils = CacheMemoryUtils.getInstance(cacheKey, 256);
        CacheDiskUtils cacheDiskUtils = CacheDiskUtils.getInstance(diskDir, diskMaxSize, Integer.MAX_VALUE);
        CacheDoubleStaticUtils.setDefaultCacheDoubleUtils(CacheDoubleUtils.getInstance(cacheMemoryUtils, cacheDiskUtils));
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
    public <T> ObservableTransformer<T, CacheResult<T>> transformer(CacheMode cacheMode, final Type type) {
        final IStrategy strategy = loadStrategy(cacheMode);//获取缓存策略
        return new ObservableTransformer<T, CacheResult<T>>() {
            @Override
            public ObservableSource<CacheResult<T>> apply(Observable<T> upstream) {
                LogUtils.tag("FlyangHttp").i("cackeKey=" + RxCache.this.cacheKey);
                Type tempType = type;
                if (type instanceof ParameterizedType) {//自定义ApiResult
                    Class<T> cls = (Class) ((ParameterizedType) type).getRawType();
                    if (CacheResult.class.isAssignableFrom(cls)) {
                        tempType = HttpUtils.getParameterizedType(type, 0);
                    }
                }
                return strategy.execute(RxCache.this, RxCache.this.cacheKey, RxCache.this.cacheTime, upstream, tempType);
            }
        };
    }

    private static abstract class SimpleSubscribe<T> implements ObservableOnSubscribe<T> {
        @Override
        public void subscribe(@NonNull ObservableEmitter<T> subscriber) throws Exception {
            try {
                T data = execute();
                if (!subscriber.isDisposed()) {
                    subscriber.onNext(data);
                }
            } catch (Throwable e) {
                LogUtils.tag("FlyangHttp").e(e.getMessage());
                if (!subscriber.isDisposed()) {
                    subscriber.onError(e);
                }
                Exceptions.throwIfFatal(e);
                //RxJavaPlugins.onError(e);
                return;
            }

            if (!subscriber.isDisposed()) {
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
    public <T> Observable<T> load(final Type type, final String key) {
        return load(type, key, -1);
    }

    /**
     * 根据时间读取缓存
     *
     * @param type 保存的类型
     * @param key  缓存key
     * @param time 保存时间
     */
    public <T> Observable<T> load(final Type type, final String key, final long time) {
        return Observable.create(new SimpleSubscribe<T>() {
            @Override
            T execute() throws Throwable {
                return CacheFactory.load(key, cacheType, type);
            }
        });
    }

    /**
     * 保存
     *
     * @param key   缓存key
     * @param value 缓存Value
     */
    public <T> Observable<Boolean> save(final String key, final T value) {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                CacheFactory.write(key, value, cacheType, cacheTime);
                return true;
            }
        });
    }

    /**
     * 删除缓存
     */
    public Observable<Boolean> remove(final String key) {
        return Observable.create(new SimpleSubscribe<Boolean>() {

            @Override
            Boolean execute() throws Throwable {
                CacheFactory.remove(key);
                return true;
            }
        });
    }

    /**
     * 清空缓存
     */
    public Observable<Boolean> clear() {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                CacheFactory.clear();
                return true;
            }
        });
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

    public File getDiskDir() {
        return diskDir;
    }


    public long getDiskMaxSize() {
        return diskMaxSize;
    }

    public static final class Builder {
        private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
        private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
        public static final int CACHE_NEVER_EXPIRE = -1;//永久不过期
        private long diskMaxSize;
        private File diskDir;
        private Context context;
        private String cachekey;
        private int cacheTime;
        private CacheType cacheType = CacheType.Serializable;                         //缓存的转换器

        public Builder() {
            cacheTime = CACHE_NEVER_EXPIRE;
        }

        public Builder(RxCache rxCache) {
            this.context = rxCache.context;
            this.diskMaxSize = rxCache.diskMaxSize;
            this.diskDir = rxCache.diskDir;
            this.context = rxCache.context;
            this.cachekey = rxCache.cacheKey;
            this.cacheTime = rxCache.cacheTime;
            this.cacheType = rxCache.cacheType;
        }

        public Builder init(Context context) {
            this.context = context;
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

        public Builder cacheTime(int cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        public Builder cacheType(CacheType cacheType) {
            this.cacheType = cacheType;
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
            if (diskMaxSize <= 0) {
                diskMaxSize = calculateDiskCacheSize(diskDir);
            }
            cacheTime = Math.max(CACHE_NEVER_EXPIRE, this.cacheTime);

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
