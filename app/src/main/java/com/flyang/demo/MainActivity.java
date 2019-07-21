package com.flyang.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.flyang.imageloader.ImageLoader;
import com.flyang.imageloader.config.inter.ShapeMode;
import com.flyang.imageloader.lisenter.ImageCallBackListener;
import com.flyang.progress.OnProgressListener;
import com.flyang.progress.model.ProgressInfo;
import com.flyang.util.log.LogUtils;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
//    private CircleProgressView progressView;

    String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497688355699&di=ea69a930b82ce88561c635089995e124&imgtype=0&src=http%3A%2F%2Fcms-bucket.nosdn.127.net%2Ff84e566bcf654b3698363409fbd676ef20161119091503.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
//        progressView = findViewById(R.id.progressView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ImageLoader.resumeRequests();
    }

    public void button(View view) {

        ImageLoader.with(this)
                .url(url)
                .isNeedVignette(true)
                .shapeMode(ShapeMode.OVAL)
                .skipMemoryCache(true)
                .rectRoundRadius(50)
                .asDrawable(true)
                .saveGallery(true)
                .onProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(ProgressInfo progressInfo) {
                        LogUtils.e(progressInfo.toString());
                    }

                    @Override
                    public void onError(long id, Exception e) {
                        LogUtils.e(e.toString());
                    }
                })
                .imageCallBackListener(new ImageCallBackListener() {
                    @Override
                    public void onSuccess(Object type) {
                        LogUtils.e(type);
                    }

                    @Override
                    public void onFail() {

                    }
                })
                .into(imageView);

    }

    @SuppressLint("CheckResult")
    public void onClick(View view) {
        ImageLoader.clearMomory();
        ImageLoader.clearDiskCache();
//        EasyHttp.getInstance().setWriteTimeOut(10L);
//        EasyHttp.get("http://apis.juhe.cn/mobile/get")
//                .params("phone", "18688994275")
//                .params("dtype", "json")
//                .params("key", "5682c1f44a7f486e40f9720d6c97ffe4")
//                .cacheKey("test")
//                .cacheMode(CacheStrategy.CACHEANDREMOTEDISTINCT)
//                .execute(new CallClazzProxy<TestApiResult1<ResultBean>, ResultBean>(ResultBean.class) {
//
//                })
//                .onErrorReturn(new Function<Throwable, ResultBean>() {
//                    @Override
//                    public ResultBean apply(Throwable throwable) throws Exception {
//                        ResultBean resultBean = new ResultBean();
//                        LogUtils.e(throwable.toString());
//                        return resultBean;
//                    }
//                })
//                .subscribe(new Consumer<ResultBean>() {
//                    @Override
//                    public void accept(ResultBean resultBean) throws Exception {
//                        LogUtils.e(resultBean.toString());
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        LogUtils.e(throwable.toString());
//                    }
//                }, new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        LogUtils.e("----");
//                    }
//                });
//        EasyHttp.get("http://apis.juhe.cn/mobile/get")
//                .cacheMode(CacheStrategy.CACHEANDREMOTEDISTINCT)
////                .readTimeOut(30 * 1000)//测试局部读超时30s
////                .cacheKey(this.getClass().getSimpleName())//缓存key
////                .retryCount(5)//重试次数
////                .cacheTime(5 * 60)//缓存时间300s，默认-1永久缓存
////                //.cacheDiskConverter(new GsonDiskConverter())//默认使用的是 new SerializableDiskConverter();
////                .cacheDiskConverter(new SerializableDiskConverter())//默认使用的是 new SerializableDiskConverter();
////                .timeStamp(true)
//                .execute(new SimpleCallBack<ResultBean>() {
//                    @Override
//                    public void onError(ApiException e) {
//                        LogUtils.e(e.toString());
//                    }
//
//                    @Override
//                    public void onSuccess(ResultBean resultBean) {
//                        LogUtils.e(resultBean.toString());
//                    }
//                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImageLoader.pauseRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
