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

package com.flyang.network.interceptor;

import android.content.Context;

import com.flyang.network.utils.HttpUtils;
import com.flyang.util.log.LogUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author caoyangfei
 * @ClassName CacheInterceptorOffline
 * @date 2019/10/16
 * ------------- Description -------------
 * 支持离线缓存,使用OKhttp自带缓存
 * <p>
 * 配置Okhttp的Cache
 * 配置请求头中的cache-control或者统一处理所有请求的请求头
 * 云端配合设置响应头或者自己写拦截器修改响应头中cache-control
 *
 * <p>
 * 在Retrofit中，我们可以通过@Headers来配置，单独对api缓存如：
 * @Headers("Cache-Control: public, max-age=3600)
 * @GET("merchants/{shopId}/icon") </p>
 * Observable<ShopIconEntity> getShopIcon(@Path("shopId") long shopId);
 * </P>
 */
public class CacheInterceptorOffline extends CacheInterceptor {
    public CacheInterceptorOffline(Context context) {
        super(context);
    }

    public CacheInterceptorOffline(Context context, String cacheControlValue) {
        super(context, cacheControlValue);
    }

    public CacheInterceptorOffline(Context context, String cacheControlValue, String cacheOnlineControlValue) {
        super(context, cacheControlValue, cacheOnlineControlValue);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!HttpUtils.isNetworkAvailable(context)) {
            LogUtils.tag("FlyangHttp").i(" no network load cache:" + request.cacheControl().toString());
           /* request = request.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "only-if-cached, " + cacheControlValue_Offline)
                    .build();*/

            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    //.cacheControl(CacheControl.FORCE_NETWORK)
                    .build();
            Response response = chain.proceed(request);
            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, " + cacheControlValue_Offline)
                    .build();
        }
        return chain.proceed(request);
    }
}
