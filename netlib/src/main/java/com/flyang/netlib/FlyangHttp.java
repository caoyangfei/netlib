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

package com.flyang.netlib;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.flyang.netlib.cache.RxCache;
import com.flyang.netlib.cache.converter.CacheType;
import com.flyang.netlib.cache.model.CacheMode;
import com.flyang.netlib.cookie.CookieManger;
import com.flyang.netlib.https.HttpsSslManager;
import com.flyang.netlib.interceptor.HttpLoggingInterceptor;
import com.flyang.netlib.model.HttpHeaders;
import com.flyang.netlib.model.HttpParams;
import com.flyang.netlib.request.CustomRequest;
import com.flyang.netlib.request.DeleteRequest;
import com.flyang.netlib.request.DownloadRequest;
import com.flyang.netlib.request.GetRequest;
import com.flyang.netlib.request.PostRequest;
import com.flyang.netlib.request.PutRequest;
import com.flyang.netlib.utils.RxSchedulers;
import com.flyang.util.app.ApplicationUtils;
import com.flyang.util.data.PreconditionUtils;
import com.flyang.util.log.LogUtils;

import java.io.File;
import java.io.InputStream;
import java.net.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * @author caoyangfei
 * @ClassName FlyangHttp
 * @date 2019/7/23
 * ------------- Description -------------
 * 网络请求入口类
 * <br>
 * 功能：全局设置
 * 1.全局设置超时时间
 * 2.支持请求错误重试相关参数，包括重试次数、重试延时时间
 * 3.支持缓存支持6种缓存模式、时间、大小、缓存目录
 * 4.支持支持GET、post、delete、put请求
 * 5.支持支持自定义请求
 * 6.支持文件上传、下载
 * 7.支持全局公共请求头
 * 8.支持全局公共参数
 * 9.支持okhttp相关参数，包括拦截器
 * 10.支持Retrofit相关参数
 * 11.支持Cookie管理
 * </br>
 */
public final class FlyangHttp {
    private static Application sContext = ApplicationUtils.getApp();
    public static final int DEFAULT_MILLISECONDS = 60000;             //默认的超时时间
    private static final int DEFAULT_RETRY_COUNT = 3;                 //默认重试次数
    private static final int DEFAULT_RETRY_INCREASEDELAY = 0;         //默认重试叠加时间
    private static final int DEFAULT_RETRY_DELAY = 500;               //默认重试延时
    public static final int DEFAULT_CACHE_NEVER_EXPIRE = -1;          //缓存过期时间，默认永久缓存
    private Cache mCache = null;                                      //Okhttp缓存对象
    private CacheMode mCacheMode = CacheMode.NO_CACHE;                //缓存类型
    private int mCacheTime = -1;                                      //缓存时间
    private File mCacheDirectory;                                     //缓存目录
    private long mCacheMaxSize;                                       //缓存大小
    private String mBaseUrl;                                          //全局BaseUrl
    private int mRetryCount = DEFAULT_RETRY_COUNT;                    //重试次数默认3次
    private int mRetryDelay = DEFAULT_RETRY_DELAY;                    //延迟xxms重试
    private int mRetryIncreaseDelay = DEFAULT_RETRY_INCREASEDELAY;    //叠加延迟
    private HttpHeaders mCommonHeaders;                               //全局公共请求头
    private HttpParams mCommonParams;                                 //全局公共请求参数
    private OkHttpClient.Builder okHttpClientBuilder;                 //okhttp请求的客户端
    private Retrofit.Builder retrofitBuilder;                         //Retrofit请求Builder
    private RxCache.Builder rxCacheBuilder;                           //RxCache请求的Builder
    private CookieManger cookieJar;                                   //Cookie管理
    private volatile static FlyangHttp singleton = null;

    private FlyangHttp() {
        okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.hostnameVerifier(new DefaultHostnameVerifier());
        okHttpClientBuilder.connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());//增加RxJava2CallAdapterFactory
        rxCacheBuilder = new RxCache.Builder().init(sContext)
                .cacheType(CacheType.Serializable);      //目前只支持Serializable和Gson缓存其它可以自己扩展
    }

    public static FlyangHttp getInstance() {
        testInitialize();
        if (singleton == null) {
            synchronized (FlyangHttp.class) {
                if (singleton == null) {
                    singleton = new FlyangHttp();
                }
            }
        }
        return singleton;
    }

    /**
     * 必须在全局Application先调用，获取context上下文，否则缓存无法使用
     */
    public static void init(Application app) {
        sContext = app;
    }

    /**
     * 获取全局上下文
     */
    public static Context getContext() {
        testInitialize();
        return sContext;
    }

    private static void testInitialize() {
        if (sContext == null)
            throw new ExceptionInInitializerError("请先在全局Application中调用 FlyangHttp.init() 初始化！");
    }

    public static OkHttpClient getOkHttpClient() {
        return getInstance().okHttpClientBuilder.build();
    }

    public static Retrofit getRetrofit() {
        return getInstance().retrofitBuilder.build();
    }

    public static RxCache getRxCache() {
        return getInstance().rxCacheBuilder.build();
    }

    /**
     * 对外暴露 OkHttpClient,方便自定义
     */
    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        return getInstance().okHttpClientBuilder;
    }

    /**
     * 对外暴露 Retrofit,方便自定义
     */
    public static Retrofit.Builder getRetrofitBuilder() {
        return getInstance().retrofitBuilder;
    }

    /**
     * 对外暴露 RxCache,方便自定义
     */
    public static RxCache.Builder getRxCacheBuilder() {
        return getInstance().rxCacheBuilder;
    }

    /**
     * 调试模式,默认打开所有的异常日志
     *
     * @param tag tag
     * @return
     */
    public FlyangHttp debug(String tag) {
        debug(tag, true);
        return this;
    }

    /**
     * @param tag              tag
     * @param isPrintException log是否显示
     * @return
     */
    public FlyangHttp debug(String tag, boolean isPrintException) {
        String tempTag = TextUtils.isEmpty(tag) ? "RxEasyHttp_" : tag;
        if (isPrintException) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(tempTag, isPrintException);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }
        LogUtils.getLogConfig().configAllowLog(isPrintException);
        return this;
    }

    /**
     * 此类是用于主机名验证的基接口。 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，
     * 则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。策略可以是基于证书的或依赖于其他验证方案。
     * 当验证 URL 主机名使用的默认规则失败时使用这些回调。如果主机名是可接受的，则返回 true
     */
    public class DefaultHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * https的全局访问规则
     *
     * @param hostnameVerifier
     * @return
     */
    public FlyangHttp setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * https的全局自签名证书
     *
     * @param certificates
     * @return
     */
    public FlyangHttp setCertificates(InputStream... certificates) {
        HttpsSslManager.SSLParams sslParams = HttpsSslManager.getSslSocketFactory(null, null, certificates);
        okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return this;
    }

    /**
     * https双向认证证书
     *
     * @param bksFile
     * @param password
     * @param certificates
     * @return
     */
    public FlyangHttp setCertificates(InputStream bksFile, String password, InputStream... certificates) {
        HttpsSslManager.SSLParams sslParams = HttpsSslManager.getSslSocketFactory(bksFile, password, certificates);
        okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return this;
    }

    /**
     * 全局cookie存取规则
     *
     * @param cookieManager
     * @return
     */
    public FlyangHttp setCookieStore(CookieManger cookieManager) {
        cookieJar = cookieManager;
        okHttpClientBuilder.cookieJar(cookieJar);
        return this;
    }

    /**
     * 获取全局的cookie实例
     *
     * @return
     */
    public static CookieManger getCookieJar() {
        return getInstance().cookieJar;
    }

    /**
     * 全局读取超时时间
     *
     * @param readTimeOut
     * @return
     */
    public FlyangHttp setReadTimeOut(long readTimeOut) {
        okHttpClientBuilder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 全局写入超时时间
     *
     * @param writeTimeout
     * @return
     */
    public FlyangHttp setWriteTimeOut(long writeTimeout) {
        okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 全局连接超时时间
     *
     * @param connectTimeout
     * @return
     */
    public FlyangHttp setConnectTimeout(long connectTimeout) {
        okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 超时重试次数
     *
     * @param retryCount
     * @return
     */
    public FlyangHttp setRetryCount(int retryCount) {
        if (retryCount < 0) throw new IllegalArgumentException("retryCount must > 0");
        mRetryCount = retryCount;
        return this;
    }

    /**
     * 超时重试次数
     *
     * @return
     */
    public static int getRetryCount() {
        return getInstance().mRetryCount;
    }

    /**
     * 超时重试延迟时间
     *
     * @param retryDelay
     * @return
     */
    public FlyangHttp setRetryDelay(int retryDelay) {
        if (retryDelay < 0) throw new IllegalArgumentException("retryDelay must > 0");
        mRetryDelay = retryDelay;
        return this;
    }

    /**
     * 超时重试延迟时间
     *
     * @return
     */
    public static int getRetryDelay() {
        return getInstance().mRetryDelay;
    }

    /**
     * 超时重试延迟叠加时间
     *
     * @param retryIncreaseDelay
     * @return
     */
    public FlyangHttp setRetryIncreaseDelay(int retryIncreaseDelay) {
        if (retryIncreaseDelay < 0)
            throw new IllegalArgumentException("retryIncreaseDelay must > 0");
        mRetryIncreaseDelay = retryIncreaseDelay;
        return this;
    }

    /**
     * 超时重试延迟叠加时间
     *
     * @return
     */
    public static int getRetryIncreaseDelay() {
        return getInstance().mRetryIncreaseDelay;
    }

    /**
     * 全局的缓存模式
     *
     * @param cacheMode
     * @return
     */
    public FlyangHttp setCacheMode(CacheMode cacheMode) {
        mCacheMode = cacheMode;
        return this;
    }

    /**
     * 获取全局的缓存模式
     *
     * @return
     */
    public static CacheMode getCacheMode() {
        return getInstance().mCacheMode;
    }

    /**
     * 全局的缓存过期时间
     *
     * @param cacheTime
     * @return
     */
    public FlyangHttp setCacheTime(int cacheTime) {
        if (cacheTime <= -1) cacheTime = DEFAULT_CACHE_NEVER_EXPIRE;
        mCacheTime = cacheTime;
        return this;
    }

    /**
     * 获取全局的缓存过期时间
     */
    public static int getCacheTime() {
        return getInstance().mCacheTime;
    }

    /**
     * 全局的缓存大小,默认50M
     */
    public FlyangHttp setCacheMaxSize(long maxSize) {
        mCacheMaxSize = maxSize;
        return this;
    }

    /**
     * 获取全局的缓存大小
     */
    public static long getCacheMaxSize() {
        return getInstance().mCacheMaxSize;
    }


    /**
     * 全局设置缓存的路径，默认是应用包下面的缓存
     */
    public FlyangHttp setCacheDirectory(File directory) {
        mCacheDirectory = PreconditionUtils.checkNotNull(directory, "directory == null");
        rxCacheBuilder.diskDir(directory);
        return this;
    }

    /**
     * 获取缓存的路劲
     */
    public static File getCacheDirectory() {
        return getInstance().mCacheDirectory;
    }

    /**
     * 全局设置缓存的转换器
     */
    public FlyangHttp setCacheCacheType(CacheType cacheType) {
        rxCacheBuilder.cacheType(PreconditionUtils.checkNotNull(cacheType, "converter == null"));
        return this;
    }

    /**
     * 全局设置OkHttp的缓存,默认是3天
     */
    public FlyangHttp setHttpCache(Cache cache) {
        this.mCache = cache;
        return this;
    }

    /**
     * 获取OkHttp的缓存<br>
     */
    public static Cache getHttpCache() {
        return getInstance().mCache;
    }

    /**
     * 添加全局公共请求参数
     */
    public FlyangHttp addCommonParams(HttpParams commonParams) {
        if (mCommonParams == null) mCommonParams = new HttpParams();
        mCommonParams.put(commonParams);
        return this;
    }

    /**
     * 获取全局公共请求参数
     */
    public HttpParams getCommonParams() {
        return mCommonParams;
    }

    /**
     * 获取全局公共请求头
     */
    public HttpHeaders getCommonHeaders() {
        return mCommonHeaders;
    }

    /**
     * 添加全局公共请求参数
     */
    public FlyangHttp addCommonHeaders(HttpHeaders commonHeaders) {
        if (mCommonHeaders == null) mCommonHeaders = new HttpHeaders();
        mCommonHeaders.put(commonHeaders);
        return this;
    }

    /**
     * 添加全局拦截器
     */
    public FlyangHttp addInterceptor(Interceptor interceptor) {
        okHttpClientBuilder.addInterceptor(PreconditionUtils.checkNotNull(interceptor, "interceptor == null"));
        return this;
    }

    /**
     * 添加全局网络拦截器
     */
    public FlyangHttp addNetworkInterceptor(Interceptor interceptor) {
        okHttpClientBuilder.addNetworkInterceptor(PreconditionUtils.checkNotNull(interceptor, "interceptor == null"));
        return this;
    }

    /**
     * 全局设置代理
     */
    public FlyangHttp setOkproxy(Proxy proxy) {
        okHttpClientBuilder.proxy(PreconditionUtils.checkNotNull(proxy, "proxy == null"));
        return this;
    }

    /**
     * 全局设置请求的连接池
     */
    public FlyangHttp setOkconnectionPool(ConnectionPool connectionPool) {
        okHttpClientBuilder.connectionPool(PreconditionUtils.checkNotNull(connectionPool, "connectionPool == null"));
        return this;
    }

    /**
     * 全局为Retrofit设置自定义的OkHttpClient
     */
    public FlyangHttp setOkclient(OkHttpClient client) {
        retrofitBuilder.client(PreconditionUtils.checkNotNull(client, "client == null"));
        return this;
    }

    /**
     * 全局设置Converter.Factory,默认GsonConverterFactory.create()
     */
    public FlyangHttp addConverterFactory(Converter.Factory factory) {
        retrofitBuilder.addConverterFactory(PreconditionUtils.checkNotNull(factory, "factory == null"));
        return this;
    }

    /**
     * 全局设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
     */
    public FlyangHttp addCallAdapterFactory(CallAdapter.Factory factory) {
        retrofitBuilder.addCallAdapterFactory(PreconditionUtils.checkNotNull(factory, "factory == null"));
        return this;
    }

    /**
     * 全局设置Retrofit callbackExecutor
     */
    public FlyangHttp setCallbackExecutor(Executor executor) {
        retrofitBuilder.callbackExecutor(PreconditionUtils.checkNotNull(executor, "executor == null"));
        return this;
    }

    /**
     * 全局设置Retrofit对象Factory
     */
    public FlyangHttp setCallFactory(okhttp3.Call.Factory factory) {
        retrofitBuilder.callFactory(PreconditionUtils.checkNotNull(factory, "factory == null"));
        return this;
    }

    /**
     * 设置全局baseUrl
     *
     * @param baseUrl
     * @return
     */
    public FlyangHttp setBaseUrl(String baseUrl) {
        mBaseUrl = PreconditionUtils.checkNotNull(baseUrl, "baseUrl == null");
        return this;
    }

    /**
     * 获取全局baseurl
     */
    public static String getBaseUrl() {
        return getInstance().mBaseUrl;
    }

    /**
     * get请求
     */
    public static GetRequest get(String url) {
        return new GetRequest(url);
    }

    /**
     * post请求
     */
    public static PostRequest post(String url) {
        return new PostRequest(url);
    }


    /**
     * delete请求
     */
    public static DeleteRequest delete(String url) {

        return new DeleteRequest(url);
    }

    /**
     * 自定义请求
     */
    public static CustomRequest custom() {
        return new CustomRequest();
    }

    public static DownloadRequest downLoad(String url) {
        return new DownloadRequest(url);
    }

    public static PutRequest put(String url) {
        return new PutRequest(url);
    }

    /**
     * 取消订阅
     */
    public static void cancelSubscription(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    /**
     * 清空缓存
     */
    @SuppressLint("CheckResult")
    public static void clearCache() {
        getRxCache().clear().compose(RxSchedulers.<Boolean>io_main())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        LogUtils.i("clearCache success!!!");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        LogUtils.i("clearCache err!!!");
                    }
                });
    }

    /**
     * 移除缓存（key）
     */
    @SuppressLint("CheckResult")
    public static void removeCache(String key) {
        getRxCache().remove(key).compose(RxSchedulers.<Boolean>io_main()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                LogUtils.i("removeCache success!!!");
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                LogUtils.i("removeCache err!!!");
            }
        });
    }
}
