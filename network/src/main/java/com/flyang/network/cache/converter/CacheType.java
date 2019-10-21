package com.flyang.network.cache.converter;

/**
 * @author yangfei.cao
 * @ClassName netlib_demo
 * @date 2019/7/24
 * ------------- Description -------------
 * 缓存数据类型
 */
public enum CacheType {
    //序列化
    Serializable(1),

    //json缓存
    Json(2);


    private int type;

    CacheType(int type) {
        this.type = type;
    }

}
