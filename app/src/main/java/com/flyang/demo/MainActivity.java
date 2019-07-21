package com.flyang.demo;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
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

    private ImageView imageView,imageView2;
//    private CircleProgressView progressView;

    String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497688355699&di=ea69a930b82ce88561c635089995e124&imgtype=0&src=http%3A%2F%2Fcms-bucket.nosdn.127.net%2Ff84e566bcf654b3698363409fbd676ef20161119091503.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
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
                .imageCallBackListener(new ImageCallBackListener<Drawable>() {
                    @Override
                    public void onSuccess(Drawable type) {
                        imageView2.setImageDrawable(type);
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
