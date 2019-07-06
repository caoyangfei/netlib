package com.flyang.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.flyang.imageloader.ImageLoader;
import com.flyang.imageloader.config.inter.ShapeMode;
import com.flyang.imageloader.lisenter.OnProgressListener;

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


    public void button(View view) {

        ImageLoader.with(this)
                .url(url)
                .isNeedVignette(true)
                .shapeMode(ShapeMode.OVAL)
                .rectRoundRadius(50)
                .asDrawable(true)
                .saveGallery(true)
                .onProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(boolean isComplete, int percentage, long bytesRead, long totalBytes) {
//                        if (isComplete) {
//                            progressView.setVisibility(View.GONE);
//                        } else {
//                            progressView.setVisibility(View.VISIBLE);
//                            progressView.setProgress(percentage);
//                        }
                    }
                })
                .into(imageView);

    }
}
