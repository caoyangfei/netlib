package com.flyang.netlib.cache.converter;

import com.flyang.util.cache.CacheDoubleStaticUtils;
import com.flyang.util.data.GsonUtils;

import java.io.Serializable;
import java.lang.reflect.Type;

import okio.ByteString;

/**
 * @author yangfei.cao
 * @ClassName netlib_demo
 * @date 2019/7/24
 * ------------- Description -------------
 * 缓存工厂类
 */
public class CacheFactory<T> {

    public static <T> T load(String key, CacheType cacheType, Type type) {
        String cacheKey = ByteString.of(key.getBytes()).md5().hex();
        switch (cacheType) {
            case Serializable:
                Object serializable = CacheDoubleStaticUtils.getSerializable(cacheKey);
                return (T) serializable;
            case Json:
                String str = CacheDoubleStaticUtils.getString(cacheKey);
                return GsonUtils.fromJson(str, type);
            default:
                return null;
        }
    }

    public static <T> void write(String key, T data, CacheType cacheType, int saveTime) {
        String cacheKey = ByteString.of(key.getBytes()).md5().hex();
        switch (cacheType) {
            case Serializable:
                CacheDoubleStaticUtils.put(cacheKey, ((Serializable) data), saveTime);
                break;
            case Json:
                CacheDoubleStaticUtils.put(cacheKey, GsonUtils.toJson(data));
                break;
        }
    }

    public static <T> void remove(String key) {
        CacheDoubleStaticUtils.remove(key);
    }

    public static <T> void clear() {
        CacheDoubleStaticUtils.clear();
    }
}
