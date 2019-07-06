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
package com.flyang.imageloader.config;

import android.content.Context;
import android.widget.ImageView;

import com.flyang.util.app.ApplicationUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author caoyangfei
 * @ClassName ImageConfig
 * @date 2019/7/3
 * ------------- Description -------------
 * <P>策略者模式</P>
 * 图片配置信息基类，每个图片加载框架对应自己的实现配置信息类
 */
public class ImageConfig {

    protected Context context;

    /**
     * 默认最大缓存
     */
    public static int CACHE_IMAGE_SIZE = 250;

    protected WeakReference<ImageView> targetView;

    protected String url;             //远程图片	http://, https://	HttpURLConnection 或者参考 使用其他网络加载方案
    protected String filePath;        //本地文件	file://	FileInputStream 加载SD卡资源
    protected File file;              //加载SD卡资源
    protected int resId;              //加载drawable资源
    protected String rawPath;         //加载raw资源
    protected String assertspath;     //asset目录下的资源	asset://	AssetManager  加载asserts资源
    protected String contentProvider; //Content provider	content://	ContentResolver 加载ContentProvider资源

    protected int placeholder;      //占位图片
    protected int errorPic;         //失败占位图片
    protected float thumbnail; //缩略图缩放倍数
    protected boolean saveGallery; //保存图片到图库

    public Context getContext() {
        if (context == null) {
            context = ApplicationUtils.getApp();
        }
        return context;
    }

    public ImageView getTargetView() {
        return targetView.get();
    }

    public String getUrl() {
        return url;
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFile() {
        return file;
    }

    public int getResId() {
        return resId;
    }

    public String getRawPath() {
        return rawPath;
    }

    public String getAssertspath() {
        return assertspath;
    }

    public String getContentProvider() {
        return contentProvider;
    }

    public int getPlaceholder() {
        return placeholder;
    }

    public int getErrorPic() {
        return errorPic;
    }

    public static int getCacheImageSize() {
        return CACHE_IMAGE_SIZE;
    }

    public float getThumbnail() {
        return thumbnail;
    }

    public boolean isSaveGallery() {
        return saveGallery;
    }
}
