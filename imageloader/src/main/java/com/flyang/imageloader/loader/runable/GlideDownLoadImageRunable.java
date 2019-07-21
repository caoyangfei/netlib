package com.flyang.imageloader.loader.runable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
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
 * 单下载图片
 */
public class GlideDownLoadImageRunable<T> extends ThreadUtils.SimpleTask<T> {
    private ImageConfigSpread imageConfig;
    private RequestBuilder request;
    private T t;

    public GlideDownLoadImageRunable(RequestBuilder request, ImageConfigSpread imageConfig) {
        this.imageConfig = imageConfig;
        this.request = request;
    }

    @Nullable
    @Override
    public T doInBackground() throws Throwable {
        FutureTarget<T> submit = request.submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        t = submit.get();

        boolean storage = PermissionUtils.hasSelfPermissions(PermissionConstant.getPermissions(PermissionConstant.STORAGE));
        if (imageConfig.isSaveGallery()) {
            if (!storage) {
                ToastUtils.showShort("没有存储权限，不能保存到图库！");
            } else {
                if (imageConfig.isAsBitmap()) {
                    //保存到图库
                    ImageUtils.saveImageToGallery((Bitmap) t, Bitmap.CompressFormat.JPEG);
                } else if (imageConfig.isAsDrawable()) {
                    Bitmap bitmap = ConvertUtils.drawable2Bitmap((Drawable) t);
                    //保存到图库
                    ImageUtils.saveImageToGallery(bitmap, Bitmap.CompressFormat.JPEG);
                }
            }
        }
        return t;
    }

    @Override
    public void onSuccess(@Nullable T t) {
        if (imageConfig.getImageCallBackListener() != null) {
            imageConfig.getImageCallBackListener().onSuccess(t);
        }
    }

    @Override
    public void onFail(Throwable t) {
        super.onFail(t);
        if (imageConfig.getImageCallBackListener() != null) {
            imageConfig.getImageCallBackListener().onFail();
        }
    }
}
