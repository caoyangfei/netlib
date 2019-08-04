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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.flyang.demo.customapi.test1.ResultBean;
import com.flyang.demo.customapi.test1.TestApiResult1;
import com.flyang.demo.customapi.test10.ArticleBean;
import com.flyang.demo.customapi.test10.TestResultApi10;
import com.flyang.demo.customapi.test2.Result;
import com.flyang.demo.customapi.test2.TestApiResult2;
import com.flyang.demo.customapi.test3.TestApiResult3;
import com.flyang.demo.customapi.test4.GwclBean;
import com.flyang.demo.customapi.test9.FriendsListBean;
import com.flyang.demo.customapi.test9.MyResult;
import com.flyang.demo.customapi.testN.HttpManager;
import com.flyang.netlib.FlyangHttp;
import com.flyang.netlib.cache.model.CacheMode;
import com.flyang.netlib.cache.model.CacheResult;
import com.flyang.netlib.callback.CallBackProxy;
import com.flyang.netlib.callback.ProgressDialogCallBack;
import com.flyang.netlib.callback.SimpleCallBack;
import com.flyang.netlib.exception.ApiException;
import com.flyang.netlib.subsciber.IProgressDialog;

import java.util.List;

/**
 * <p>描述：主要介绍自定义ApiResult使用情景</p>
 * 作者： zhouyou<br>
 * 日期： 2017/7/6 16:23 <br>
 * 版本： v1.0<br>
 */
public class CustomApiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_api);
    }

    private IProgressDialog mProgressDialog = new IProgressDialog() {
        @Override
        public Dialog getDialog() {
            ProgressDialog dialog = new ProgressDialog(CustomApiActivity.this);
            dialog.setMessage("请稍候...");
            return dialog;
        }
    };
    
    public void onTestOne(View view){
        //方式一
        FlyangHttp.get("/mobile/get")
                .baseUrl("http://apis.juhe.cn")
                .readTimeOut(30 * 1000)//局部定义读超时
                .writeTimeOut(30 * 1000)
                .connectTimeout(30 * 1000)
                .params("phone", "18688994275")
                .params("dtype", "json")
                .params("key", "5682c1f44a7f486e40f9720d6c97ffe4")
                .execute(new CallBackProxy<TestApiResult1<ResultBean>, ResultBean>(new ProgressDialogCallBack<ResultBean>(mProgressDialog) {
                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(ResultBean response) {
                        if (response != null) showToast(response.toString());
                    }
                }) {
                });
        //方式二
        /* Flowable<ResultBean> flowable = EasyHttp.get("http://apis.juhe.cn/mobile/get")
                .readTimeOut(30 * 1000)//局部定义读超时
                .writeTimeOut(30 * 1000)
                .params("phone", "18688994275")
                .params("dtype", "json")
                .params("key", "5682c1f44a7f486e40f9720d6c97ffe4")
                .execute(new CallClazzProxy<TestApiResult1<ResultBean>, ResultBean>(ResultBean.class) {
                });
        flowable.subscribe(new ProgressSubscriber<ResultBean>(this, mProgressDialog) {
            @Override
            public void onError(ApiException e) {
                super.onError(e);
                showToast(e.getMessage());
            }

            @Override
            public void onNext(ResultBean result) {
                showToast(result.toString());
            }
        });*/
    }
    
    public void onTestTwo(View view){
        //http://japi.juhe.cn/joke/content/list.from?key=f5236a9fb8fc75fac0a4d9b8c27a4e90&page=1&pagesize=10&sort=asc&time=1418745237
        FlyangHttp.get("http://japi.juhe.cn/joke/content/list.from")
                .params("key", "f5236a9fb8fc75fac0a4d9b8c27a4e90")
                .params("page", "1")
                .params("pagesize", "10")
                .params("sort", "asc")
                .params("time", "1418745237")
                .execute(new CallBackProxy<TestApiResult2<Result>, Result>(new ProgressDialogCallBack<Result>(mProgressDialog) {
                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(Result result) {
                        if (result != null) showToast(result.toString());
                    }
                }) {
                });

    }
    
    public void onTestThree(View view){
        FlyangHttp.post("http://xxx.xx.xx/dlydbg/api/user/login")
                //{"version":"2.5.0","phone":"13012463189","imei":"6789098763343","imsi":"6675343576887","gwkl":"dl_sj1192"}
                .upJson("{\"\":\"2.5.0\",\"\":\"\",\"\":\"\",\"\":\"\",\"gwkl\":\"\"}")
                .execute(new CallBackProxy<TestApiResult3<List<String>>, List<String>>(new ProgressDialogCallBack<List<String>>(mProgressDialog) {
                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(List<String> result) {
                        if (result != null) showToast(result.toString());
                    }
                }) {
                });
    }
    
    public void onTestFour(View view){
        FlyangHttp.post("api/")
                .baseUrl("http://xxx.xx.xx/dlydbg/")
                .upJson("{\"\":\"\",\"\":\"\",\"\":\"\",\"\":\"\",\"\":\"\",\"version\":\"1.0.0\"}")
                .cacheMode(CacheMode.CACHEANDREMOTE)
                .cacheKey(this.getClass().getSimpleName()+"test")
                 //方式一
                .execute(new CallBackProxy<TestApiResult3<CacheResult<List<GwclBean>>>, CacheResult<List<GwclBean>>>(new SimpleCallBack<CacheResult<List<GwclBean>>>() {
                    @Override
                    public void onError(ApiException e) {
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(CacheResult<List<GwclBean>> cacheResult) {
                        String from;
                        if (cacheResult.isFromCache) {
                            from = "我来自缓存:";
                        } else {
                            from = "我来自远程网络:";
                        }

                        showToast(from+cacheResult.data.toString());
                    }
                }) {
                });
                //方式二
                //如果你想获取当前的返回是否来自缓存需要在解析的数据外面包裹CacheResult<>  cacheResult.isFromCache可以获取是来自缓存还是网络
                //如果你不想关注是否来自缓存，可以不加CacheResult<>，就是上层没有isFromCache这个标记了
               /* .execute(new CallBackProxy<TestApiResult3<List<GwclBean>>, List<GwclBean>>(new SimpleCallBack<CacheResult<List<GwclBean>>>() {
                    @Override
                    public void onError(ApiException e) {
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(CacheResult<List<GwclBean>> result) {
                        LogUtils.i(">>>>>>>>>>>>>"+result.toString());
                        if (result != null) showToast(result.toString());
                    }
                }) {
                });*/
    }

    /**
     * 自定义请求返回集合
     */
    public void onTestFive(View view){
        FlyangHttp.get("https://api.91kaiteng.com/v1/user/Frineds")
                .params("token", "5b305fbeaa331")
                .params("keyword", "")
                .execute(new CallBackProxy<MyResult<List<FriendsListBean>>, List<FriendsListBean>>(new SimpleCallBack<List<FriendsListBean>>() {
                    @Override
                    public void onError(ApiException e) {
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(List<FriendsListBean> result) {
                        if (result != null) showToast(result.toString());
                    }
                }) {
                });
    }
    
    public void onTestSix(View view){
        FlyangHttp.get("http://www.wanandroid.com/article/list/0/json")
                .execute(new CallBackProxy<TestResultApi10<ArticleBean>, ArticleBean>(new SimpleCallBack<ArticleBean>() {
                    @Override
                    public void onError(ApiException e) {
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(ArticleBean result) {
                        if (result != null) showToast(result.toString());
                    }
                }) {
                });
    }

    /**
     * 主要解决重复写代理的问题,用法不变，只是将EasyHttp换成你自己定义的HttpManager去访问。HttpManager只是个类名可以使用其他名称。
     * @param view
     */
    public void onTestN(View view){
        HttpManager.get("http://japi.juhe.cn/joke/content/list.from")
                .params("key", "f5236a9fb8fc75fac0a4d9b8c27a4e90")
                .params("page", "1")
                .params("pagesize", "10")
                .params("sort", "asc")
                .params("time", "1418745237")
                .execute(new ProgressDialogCallBack<Result>(mProgressDialog) {//这么实现是不是没有代理了
                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(Result result) {
                        if (result != null) showToast(result.toString());
                    }
                });
    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
