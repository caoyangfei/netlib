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

package com.flyang.network.utils;

import com.flyang.util.data.PreconditionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;


/**
 * @author caoyangfei
 * @ClassName RequestBodyUtils
 * @date 2019/7/29
 * ------------- Description -------------
 * 请求RequestBody工具类
 */
public class RequestBodyUtils {

    public final static MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    /**
     * 封装MediaType
     *
     * @param path
     * @return
     */
    public static MediaType guessMediaType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        path = path.replace("#", "");   //解决文件名中含有#号异常的问题
        String contentType = fileNameMap.getContentTypeFor(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }

    /**
     * 提交json
     *
     * @param content
     * @return
     */
    public static RequestBody createByte(final byte[] content) {
        PreconditionUtils.checkNotNull(content, "json not null!");
        return RequestBody.create(MediaType.parse("application/octet-stream; charset=utf-8"), content);
    }

    /**
     * 提交Byte
     *
     * @param content
     * @return
     */
    public static RequestBody createByte(MediaType mediaType, final byte[] content) {
        PreconditionUtils.checkNotNull(content, "json not null!");
        return RequestBody.create(mediaType, content);
    }

    /**
     * 提交json
     *
     * @param jsonString
     * @return
     */
    public static RequestBody createJson(String jsonString) {
        PreconditionUtils.checkNotNull(jsonString, "json not null!");
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
    }

    /**
     * 提交json
     *
     * @param jsonString
     * @return
     */
    public static RequestBody createJson(MediaType mediaType, String jsonString) {
        PreconditionUtils.checkNotNull(jsonString, "json not null!");
        return RequestBody.create(mediaType, jsonString);
    }

    /**
     * 提交string
     *
     * @param name
     * @return
     */
    public static RequestBody createFile(String name) {
        PreconditionUtils.checkNotNull(name, "name not null!");
        return RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), name);
    }

    /**
     * 提交string
     *
     * @param name
     * @return
     */
    public static RequestBody createFile(MediaType mediaType, String name) {
        PreconditionUtils.checkNotNull(name, "name not null!");
        return RequestBody.create(mediaType, name);
    }

    /**
     * 提交文件
     *
     * @param file
     * @return
     */
    public static RequestBody createFile(File file) {
        PreconditionUtils.checkNotNull(file, "file not null!");
        return RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), file);
    }

    /**
     * 提交文件
     *
     * @param file
     * @return
     */
    public static RequestBody createFile(MediaType mediaType, File file) {
        PreconditionUtils.checkNotNull(file, "file not null!");
        return RequestBody.create(mediaType, file);
    }


    /**
     * 提交图片文件
     *
     * @param file
     * @return
     */
    public static RequestBody createImage(File file) {
        PreconditionUtils.checkNotNull(file, "file not null!");
        return RequestBody.create(MediaType.parse("image/jpg; charset=utf-8"), file);
    }

    /**
     * 提交图片文件
     *
     * @param file
     * @return
     */
    public static RequestBody createImage(MediaType mediaType, File file) {
        PreconditionUtils.checkNotNull(file, "file not null!");
        return RequestBody.create(mediaType, file);
    }

    /**
     * 提交压缩文件
     *
     * @param zipFile 压缩文件
     * @return 返回表单
     */
    public static RequestBody createZip(File zipFile) {
        PreconditionUtils.checkNotNull(zipFile, "zipFile not null!");
        return RequestBody.create(MediaType.parse("application/zip"), zipFile);
    }

    /**
     * 提交压缩文件
     *
     * @param zipFile 压缩文件
     * @return 返回表单
     */
    public static RequestBody createZip(MediaType mediaType, File zipFile) {
        PreconditionUtils.checkNotNull(zipFile, "zipFile not null!");
        return RequestBody.create(mediaType, zipFile);
    }

    /**
     * 流
     *
     * @param mediaType
     * @param inputStream
     * @return
     */
    public static RequestBody create(final MediaType mediaType, final InputStream inputStream) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}
