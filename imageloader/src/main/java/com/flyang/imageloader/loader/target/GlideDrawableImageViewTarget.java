package com.flyang.imageloader.loader.target;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.flyang.imageloader.config.ImageConfigSpread;
import com.flyang.util.constant.PermissionConstant;
import com.flyang.util.data.ConvertUtils;
import com.flyang.util.data.ThreadUtils;
import com.flyang.util.system.PermissionUtils;
import com.flyang.util.view.ToastUtils;
import com.flyang.util.view.img.ImageUtils;

/**
 * @author yangfei.cao
 * @ClassName netlib_demo
 * @date 2019/7/6
 * ------------- Description -------------
 * 加载监听进度 Drawable
 */
public class GlideDrawableImageViewTarget extends DrawableImageViewTarget {
    private ImageConfigSpread imageConfig;
    private String key;

    public GlideDrawableImageViewTarget(ImageView view, final ImageConfigSpread imageConfig, String key) {
        super(view);
        this.imageConfig = imageConfig;
        this.key = key;
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        super.onLoadStarted(placeholder);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        if (imageConfig.getImageCallBackListener() != null) {
            imageConfig.getImageCallBackListener().onFail();
        }
        super.onLoadFailed(errorDrawable);
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        if (imageConfig.getImageCallBackListener() != null) {
            imageConfig.getImageCallBackListener().onSuccess(resource);
        }

        boolean storage = PermissionUtils.hasSelfPermissions(PermissionConstant.getPermissions(PermissionConstant.STORAGE));

        if (imageConfig.isSaveGallery()) {
            if (!storage) {
                ToastUtils.showShort("没有存储权限，不能保存到图库！");
            } else {
                ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
                    @Nullable
                    @Override
                    public Void doInBackground() throws Throwable {
                        Bitmap bitmap = ConvertUtils.drawable2Bitmap(resource);
                        //保存到图库
                        ImageUtils.saveImageToGallery(bitmap, Bitmap.CompressFormat.JPEG);
                        return null;
                    }

                    @Override
                    public void onSuccess(@Nullable Void aVoid) {

                    }
                });
            }
        }

        super.onResourceReady(resource, transition);
    }
}
