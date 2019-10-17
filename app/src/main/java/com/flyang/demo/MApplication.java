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

package com.flyang.demo;

import android.app.Application;
import android.content.Context;

import com.flyang.demo.constant.AppConstant;
import com.flyang.demo.interceptor.CustomSignInterceptor;
import com.flyang.demo.utils.SystemInfoUtils;
import com.flyang.netlib.FlyangHttp;
import com.flyang.netlib.cache.converter.CacheType;
import com.flyang.netlib.model.HttpHeaders;
import com.flyang.netlib.model.HttpParams;
import com.flyang.progress.ProgressManager;
import com.flyang.util.log.LogUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


public class MApplication extends Application {
    private static Application app = null;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        FlyangHttp.init(this);

        //这里涉及到安全我把url去掉了，demo都是调试通的
        String Url = "http://www.xxx.com";
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.put("User-Agent", SystemInfoUtils.getUserAgent(this, AppConstant.APPID));
        //设置请求参数
        HttpParams params = new HttpParams();
        params.put("appId", AppConstant.APPID);
        FlyangHttp.getInstance()
                .debug("RxEasyHttp", BuildConfig.DEBUG)
                .setReadTimeOut(60 * 1000)
                .setWriteTimeOut(60 * 1000)
                .setConnectTimeout(60 * 1000)
                .setRetryCount(3)//默认网络不好自动重试3次
                .setRetryDelay(500)//每次延时500ms重试
                .setRetryIncreaseDelay(500)//每次延时叠加500ms
                .setBaseUrl(Url)
                .setCacheCacheType(CacheType.Serializable)//默认缓存使用序列化转化
                .setCacheMaxSize(50 * 1024 * 1024)//设置缓存大小为50M
                .setHostnameVerifier(new UnSafeHostnameVerifier(Url))//全局访问规则
                .setCertificates()//信任所有证书
                //.addConverterFactory(GsonConverterFactory.create(gson))//本框架没有采用Retrofit的Gson转化，所以不用配置
                .addCommonHeaders(headers)//设置全局公共头
                .addCommonParams(params)//设置全局公共参数
                .addInterceptor(new CustomSignInterceptor());//添加参数签名拦截器
//        .addInterceptor(new HeTInterceptor());//处理自己业务的拦截器


        ProgressManager.getInstance().with(FlyangHttp.getOkHttpClientBuilder());
    }

    public class UnSafeHostnameVerifier implements HostnameVerifier {
        private String host;

        public UnSafeHostnameVerifier(String host) {
            this.host = host;
            LogUtils.i("###############　UnSafeHostnameVerifier " + host);
        }

        @Override
        public boolean verify(String hostname, SSLSession session) {
            LogUtils.i("############### verify " + hostname + " " + this.host);
            if (this.host == null || "".equals(this.host) || !this.host.contains(hostname))
                return false;
            return true;
        }
    }

    /**
     * 获取Application的Context
     **/
    public static Context getAppContext() {
        if (app == null)
            return null;
        return app.getApplicationContext();
    }
}
