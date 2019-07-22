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

package com.flyang.demo.customapi.testN;


import com.flyang.demo.customapi.test2.TestApiResult2;
import com.flyang.netlib.callback.CallBack;
import com.flyang.netlib.callback.CallBackProxy;
import com.flyang.netlib.callback.CallClazzProxy;
import com.flyang.netlib.request.GetRequest;

import java.lang.reflect.Type;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;


/**
 * <p>描述：扩展GetResult请求，解决自定义ApiResult重复写代理的问题</p>
 * 1.用test2包中实例举例,假如你的自定义ApiResult是TestApiResult2<br>
 * 作者： zhouyou<br>
 * 日期： 2017/7/7 10:23 <br>
 * 版本： v1.0<br>
 */
public class CustomGetRequest extends GetRequest {
    public CustomGetRequest(String url) {
        super(url);
    }

    /**
     * 覆写execute方法指定，代理用TestApiResult2
     *
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> Flowable<T> execute(Type type) {
        return super.execute(new CallClazzProxy<TestApiResult2<T>, T>(type) {
        });
    }

    @Override
    public <T> Flowable<T> execute(Class<T> clazz) {
        return super.execute(new CallClazzProxy<TestApiResult2<T>, T>(clazz) {
        });
    }

    @Override
    public <T> Disposable execute(CallBack<T> callBack) {
        return super.execute(new CallBackProxy<TestApiResult2<T>, T>(callBack) {
        });
    }
}