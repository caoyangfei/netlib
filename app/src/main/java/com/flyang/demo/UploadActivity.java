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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.flyang.network.FlyangHttp;
import com.flyang.network.body.UIProgressResponseCallBack;
import com.flyang.network.callback.ProgressDialogCallBack;
import com.flyang.network.exception.ApiException;
import com.flyang.network.subsciber.IProgressDialog;
import com.flyang.util.log.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import okhttp3.MediaType;

/**
 * <p>描述：文件上传</p>
 * 作者： zhouyou<br>
 * 日期： 2017/7/6 16:26 <br>
 * 版本： v1.0<br>
 */
public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
    }

    private ProgressDialog dialog;
    private IProgressDialog mProgressDialog = new IProgressDialog() {
        @Override
        public Dialog getDialog() {
            if(dialog==null) {
                dialog = new ProgressDialog(UploadActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置进度条的形式为圆形转动的进度条
                dialog.setMessage("正在上传...");
                // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
                dialog.setTitle("文件上传");
                dialog.setMax(100);
            }
            return dialog;
        }
    };

    public void onUploadFile(View view) throws Exception {
        final UIProgressResponseCallBack listener = new UIProgressResponseCallBack() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int progress = (int) (bytesRead * 100 / contentLength);
                LogUtils.e(progress + "% ");
                dialog.setProgress(progress);
                dialog.setMessage(progress + "%");
                if (done) {//完成
                    dialog.setMessage("上传完整");
                }
            }
        };
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/1.jpg");
        FlyangHttp.post("/v1/user/uploadAvatar")
                //如果有文件名字可以不用再传Type,会自动解析到是image/*
                .params("avatar", file, file.getName(), listener)
                //.params("avatar", file, file.getName(),MediaType.parse("image/*"), listener)
                .accessToken(true)
                .timeStamp(true)
                .execute(new ProgressDialogCallBack<String>(mProgressDialog, true, true) {
                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(String response) {
                        showToast(response);
                    }
                });
    }

    public void onUploadInputStream(View view) throws Exception {
        final UIProgressResponseCallBack listener = new UIProgressResponseCallBack() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int progress = (int) (bytesRead * 100 / contentLength);
                LogUtils.e(progress + "% ");
                ((ProgressDialog) mProgressDialog.getDialog()).setProgress(progress);
                if (done) {//完成
                    ((ProgressDialog) mProgressDialog.getDialog()).setMessage("上传完整");
                }
            }
        };

        final InputStream inputStream = getResources().getAssets().open("1.jpg");
        FlyangHttp.post("/v1/user/uploadAvatar")
                .params("avatar", inputStream, "clife.png", listener)
                .accessToken(true)
                .timeStamp(true)
                .execute(new ProgressDialogCallBack<String>(mProgressDialog, true, true) {
                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(String response) {
                        showToast(response);
                    }
                });
    }

    public void onUploadBytes(View view) throws Exception {
        final UIProgressResponseCallBack listener = new UIProgressResponseCallBack() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int progress = (int) (bytesRead * 100 / contentLength);
                LogUtils.e(progress + "% ");
                ((ProgressDialog) mProgressDialog.getDialog()).setProgress(progress);
                if (done) {//完成
                    ((ProgressDialog) mProgressDialog.getDialog()).setMessage("上传完整");
                }
            }
        };

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        final byte[] bytes = baos.toByteArray();

        //avatar:表示key
        //bytes:表示上传的字节内容
        //"streamfile.png" 表示图片名称
        //MediaType.parse("image/*") 类型 表示上传的是图片
        //listener 上传进度回调监听
        FlyangHttp.post("/v1/user/uploadAvatar")
                //.params("avatar",bytes,"streamfile.png",MediaType.parse("image/*"),listener)
                //如果有文件名字可以不用再传Type,会自动解析到是image/*
                .params("avatar", bytes, "streamfile.png", listener)
                .accessToken(true)
                .timeStamp(true)
                .execute(new ProgressDialogCallBack<String>(mProgressDialog, true, true) {
                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(String response) {
                        showToast(response);
                    }
                });
    }

    public void onUploadFileMaps(View view) {
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/1.jpg");
        UIProgressResponseCallBack mUIProgressResponseCallBack = new UIProgressResponseCallBack() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int progress = (int) (bytesRead * 100 / contentLength);
                LogUtils.i("progress=" + progress);
                ((ProgressDialog) mProgressDialog.getDialog()).setProgress(progress);
                if (done) {//完成
                    ((ProgressDialog) mProgressDialog.getDialog()).setMessage("上传完整");
                }
            }
        };
        FlyangHttp.post("AppYuFaKu/uploadHeadImg")
                .baseUrl("http://www.izaodao.com/Api/")
                .params("uid", "4811420")
                .params("auth_key", "21f8d9bcc50c6ac1ae1020ce12f5f5a7")
                .params("avatar", file, file.getName(), mUIProgressResponseCallBack)
                .execute(new ProgressDialogCallBack<String>(mProgressDialog, true, true) {
                    @Override
                    public void onSuccess(String response) {
                        showToast(response);
                    }

                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }
                });

    }

    public void onUploadFileMaps2(View view) {
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/1.jpg");
        UIProgressResponseCallBack mUIProgressResponseCallBack = new UIProgressResponseCallBack() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int progress = (int) (bytesRead * 100 / contentLength);
                LogUtils.i("progress=" + progress);
                ((ProgressDialog) mProgressDialog.getDialog()).setProgress(progress);
                if (done) {//完成
                    ((ProgressDialog) mProgressDialog.getDialog()).setMessage("上传完整");
                }
            }
        };
        FlyangHttp.post("http://106.14.83.28:89/index.php?s=/Home/user/dj_add_care")
                .params("uid", "1")
                .params("title", "冷小菜")
                .params("content", "我发个哈哈哈哈哈哈哈哈")
                .params("key", "dangjian")
                .params("imgs[]", file, file.getName(), MediaType.parse("image/*"), mUIProgressResponseCallBack)
                .params("imgs[]", file, file.getName(), MediaType.parse("image/*"), mUIProgressResponseCallBack)
                .params("imgs[]", file, file.getName(), MediaType.parse("image/*"), mUIProgressResponseCallBack)
                .execute(new ProgressDialogCallBack<String>(mProgressDialog, true, true) {
                    @Override
                    public void onSuccess(String response) {
                        showToast(response);
                    }

                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }
                });

    }

    public void onUploadOne(View view) {
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/1.jpg");
        UIProgressResponseCallBack mUIProgressResponseCallBack = new UIProgressResponseCallBack() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int progress = (int) (bytesRead * 100 / contentLength);
                LogUtils.i("progress=" + progress);
                ((ProgressDialog) mProgressDialog.getDialog()).setProgress(progress);
                if (done) {//完成
                    ((ProgressDialog) mProgressDialog.getDialog()).setMessage("上传完整");
                }
            }
        };
        FlyangHttp.post("index.php/Home/index/uploadImg")
                .baseUrl("http://106.15.42.39")
                .params("img", file, file.getName(), mUIProgressResponseCallBack)
                .execute(new ProgressDialogCallBack<String>(mProgressDialog, true, true) {
                    @Override
                    public void onSuccess(String s) {
                        showToast(s);
                    }

                    @Override
                    public void onError(ApiException e) {
                        super.onError(e);
                        showToast(e.getMessage());
                    }
                });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
