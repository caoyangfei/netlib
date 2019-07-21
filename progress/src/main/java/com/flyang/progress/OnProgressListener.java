/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flyang.progress;


import com.flyang.progress.model.ProgressInfo;

/**
 * @author caoyangfei
 * @ClassName OnProgressListener
 * @date 2019/7/20
 * ------------- Description -------------
 * 进度监听器
 */
public interface OnProgressListener {
    /**
     * 进度监听
     *
     * @param progressInfo 关于进度的所有信息
     */
    void onProgress(ProgressInfo progressInfo);

    /**
     * 错误监听
     *
     * @param id 进度信息的 id
     * @param e  错误
     */
    void onError(long id, Exception e);
}
