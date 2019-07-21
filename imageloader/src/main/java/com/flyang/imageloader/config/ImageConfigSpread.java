package com.flyang.imageloader.config;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.flyang.imageloader.ImageLoader;
import com.flyang.imageloader.R;
import com.flyang.imageloader.lisenter.ImageCallBackListener;
import com.flyang.progress.OnProgressListener;

import java.io.File;
import java.lang.ref.WeakReference;


/**
 * @author yangfei.cao
 * @ClassName netlib_demo
 * @date 2019/7/3
 * ------------- Description -------------
 * <p>生成器模式</p>
 * 扩展的配置信息
 */
public class ImageConfigSpread extends ImageConfig {


    private boolean asGif;
    private boolean asBitmap;//只获取bitmap
    private boolean asDrawable;//只获取Drawable
    private boolean asFile;//只获取File
    private ImageCallBackListener imageCallBackListener;
    private OnProgressListener onProgressListener;

    // TODO: 2017/4/24 宽高的获取
    private int width;
    private int height;

    private int oWidth; //选择加载分辨率的宽
    private int oHeight; //选择加载分辨率的高

    //滤镜
    private boolean isNeedVignette; //是否需要晕映
    private boolean isNeedSketch; //是否需要素描
    private float pixelationLevel; //是否需要马赛克
    private boolean isNeedPixelation; //是否需要马赛克
    private boolean isNeedInvert; //是否需要胶片
    private float contrastLevel; //是否需要墨画
    private boolean isNeedContrast; //是否需要墨画
    private boolean isNeedSepia; //是否需要墨画
    private boolean isNeedToon; //是否需要油画
    private boolean isNeedSwirl; //是否需要漩涡
    private boolean isNeedGrayscale; //是否需要亮度
    private boolean isNeedBrightness; //是否需要亮度
    private float brightnessLeve; //亮度等级
    private boolean needBlur;//是否需要模糊
    private boolean needFilteColor;//是否需要滤镜颜色
    private int blurRadius;

    private int shapeMode;//默认矩形,可选直角矩形,圆形/椭圆
    private int rectRoundRadius;//圆角矩形时圆角的半径

    private DiskCacheStrategy diskCacheStrategy;//磁盘缓存策略

    private boolean skipMemoryCache;//是否跳过内存缓存

    private int scaleMode;//填充模式,默认centercrop,可选fitXY,centerInside...

    private int priority; //请求优先级

    private int filteColor; //滤镜颜色

    private TransitionOptions transitionOptions;

    private ImageConfigSpread(Builder builder) {
        CACHE_IMAGE_SIZE = builder.CACHE_IMAGE_SIZE;
        this.context = builder.context;
        this.targetView = builder.targetView;
        this.url = builder.url;
        this.filePath = builder.filePath;
        this.file = builder.file;
        this.resId = builder.resId;
        this.rawPath = builder.rawPath;
        this.assertspath = builder.assertspath;
        this.contentProvider = builder.contentProvider;
        this.saveGallery = builder.saveGallery;

        this.placeholder = builder.placeholder;
        this.errorPic = builder.errorPic;
        this.thumbnail = builder.thumbnail;
        this.asGif = builder.asGif;
        this.asBitmap = builder.asBitmap;
        this.asDrawable = builder.asDrawable;
        this.asFile = builder.asFile;
        this.imageCallBackListener = builder.imageCallBackListener;
        this.onProgressListener = builder.onProgressListener;
        this.width = builder.width;
        this.height = builder.height;
        this.oWidth = builder.oWidth; //选择加载分辨率的宽
        this.oHeight = builder.oHeight; //选择加载分辨率的高
        //滤镜
        this.isNeedVignette = builder.isNeedVignette; //是否需要晕映
        this.isNeedSketch = builder.isNeedSketch; //是否需要素描

        this.pixelationLevel = builder.pixelationLevel; //是否需要马赛克
        this.isNeedPixelation = builder.isNeedPixelation; //是否需要马赛克
        this.isNeedInvert = builder.isNeedInvert; //是否需要胶片
        this.contrastLevel = builder.contrastLevel; //是否需要墨画
        this.isNeedContrast = builder.isNeedContrast; //是否需要墨画
        this.isNeedSepia = builder.isNeedSepia; //是否需要墨画
        this.isNeedToon = builder.isNeedToon; //是否需要油画
        this.isNeedSwirl = builder.isNeedSwirl; //是否需要漩涡
        this.isNeedGrayscale = builder.isNeedGrayscale; //是否需要亮度
        this.isNeedBrightness = builder.isNeedBrightness; //是否需要亮度
        this.brightnessLeve = builder.brightnessLeve; //亮度等级
        this.needBlur = builder.needBlur;//是否需要模糊
        this.needFilteColor = builder.needFilteColor;//是否需要滤镜颜色
        this.blurRadius = builder.blurRadius;

        this.shapeMode = builder.shapeMode;//默认矩形,可选直角矩形,圆形/椭圆
        this.rectRoundRadius = builder.rectRoundRadius;//圆角矩形时圆角的半径
        this.diskCacheStrategy = builder.diskCacheStrategy;
        this.skipMemoryCache = builder.skipMemoryCache;
        this.scaleMode = builder.scaleMode;//填充模式,默认centercrop,可选fitXY,centerInside...
        this.priority = builder.priority; //请求优先级
        this.filteColor = builder.filteColor; //滤镜颜色
        this.transitionOptions = builder.transitionOptions;
    }

    public void into() {
        ImageLoader.getActualLoader().showImage(this);
    }


    public boolean isAsGif() {
        return asGif;
    }

    public boolean isAsBitmap() {
        return asBitmap;
    }

    public boolean isAsDrawable() {
        return asDrawable;
    }

    public boolean isAsFile() {
        return asFile;
    }

    public ImageCallBackListener getImageCallBackListener() {
        return imageCallBackListener;
    }

    public OnProgressListener getOnProgressListener() {
        return onProgressListener;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getoWidth() {
        return oWidth;
    }

    public int getoHeight() {
        return oHeight;
    }

    public boolean isNeedVignette() {
        return isNeedVignette;
    }

    public boolean isNeedSketch() {
        return isNeedSketch;
    }

    public float getPixelationLevel() {
        return pixelationLevel;
    }

    public boolean isNeedPixelation() {
        return isNeedPixelation;
    }

    public boolean isNeedInvert() {
        return isNeedInvert;
    }

    public float getContrastLevel() {
        return contrastLevel;
    }

    public boolean isNeedContrast() {
        return isNeedContrast;
    }

    public boolean isNeedSepia() {
        return isNeedSepia;
    }

    public boolean isNeedToon() {
        return isNeedToon;
    }

    public boolean isNeedSwirl() {
        return isNeedSwirl;
    }

    public boolean isNeedGrayscale() {
        return isNeedGrayscale;
    }

    public boolean isNeedBrightness() {
        return isNeedBrightness;
    }

    public float getBrightnessLeve() {
        return brightnessLeve;
    }

    public boolean isNeedBlur() {
        return needBlur;
    }

    public boolean isNeedFilteColor() {
        return needFilteColor;
    }

    public int getBlurRadius() {
        return blurRadius;
    }

    public int getShapeMode() {
        return shapeMode;
    }

    public int getRectRoundRadius() {
        return rectRoundRadius;
    }

    public DiskCacheStrategy getDiskCacheStrategy() {
        return diskCacheStrategy;
    }

    public boolean isSkipMemoryCache() {
        return skipMemoryCache;
    }

    public int getScaleMode() {
        return scaleMode;
    }

    public int getPriority() {
        return priority;
    }

    public int getFilteColor() {
        return filteColor;
    }

    public TransitionOptions getTransitionOptions() {
        return transitionOptions;
    }

    /**
     * 生成器
     */
    public static final class Builder {

        protected Context context;

        //显示图片控件
        protected WeakReference<ImageView> targetView;

        private String url;
        private String filePath;
        private File file;
        private int resId;
        private String rawPath;
        private String assertspath;
        private String contentProvider;

        private int placeholder = R.mipmap.image_loading;
        private int errorPic = R.mipmap.image_load_failed;
        private float thumbnail; //缩略图缩放倍数

        /**
         * 默认最大缓存
         */
        private int CACHE_IMAGE_SIZE = 250;

        private boolean asGif = false;

        private boolean asBitmap;//只获取bitmap
        private boolean asDrawable = true;//只获取Drawable 默认
        private boolean asFile;//只获取File
        protected boolean saveGallery = false; //保存图片到图库

        private ImageCallBackListener imageCallBackListener;
        private OnProgressListener onProgressListener;

        // TODO: 2017/4/24 宽高的获取
        private int width = Target.SIZE_ORIGINAL;
        private int height = Target.SIZE_ORIGINAL;

        private int oWidth; //选择加载分辨率的宽
        private int oHeight; //选择加载分辨率的高

        //滤镜
        private boolean isNeedVignette; //是否需要晕映
        private boolean isNeedSketch; //是否需要素描
        private float pixelationLevel; //是否需要马赛克
        private boolean isNeedPixelation; //是否需要马赛克
        private boolean isNeedInvert; //是否需要胶片
        private float contrastLevel; //是否需要墨画
        private boolean isNeedContrast = false; //是否需要墨画
        private boolean isNeedSepia = false; //是否需要墨画
        private boolean isNeedToon = false; //是否需要油画
        private boolean isNeedSwirl = false; //是否需要漩涡
        private boolean isNeedGrayscale = false; //是否需要亮度
        private boolean isNeedBrightness = false; //是否需要亮度
        private float brightnessLeve; //亮度等级
        private boolean needBlur = false;//是否需要模糊
        private boolean needFilteColor = false;//是否需要滤镜颜色
        private int blurRadius;

        private int shapeMode;//默认矩形,可选直角矩形,圆形/椭圆
        private int rectRoundRadius;//圆角矩形时圆角的半径

        private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.AUTOMATIC;

        private boolean skipMemoryCache = false;

        private int scaleMode;//填充模式,默认centercrop,可选fitXY,centerInside...

        private int priority; //请求优先级

        private int filteColor; //滤镜颜色

        private TransitionOptions transitionOptions;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 不显示，只保存
         */
        public void into() {
            into(null);
        }

        public void into(ImageView targetView) {
            this.targetView = new WeakReference<>(targetView);
            new ImageConfigSpread(this).into();
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder filePath(String filePath) {

            if (!new File(filePath).exists()) {
                throw new RuntimeException("文件不存在");
            }
            if (filePath.startsWith("file:")) {
                this.filePath = filePath;
                return this;
            }

            this.filePath = filePath;
            return this;
        }

        public Builder file(File file) {
            this.file = file;
            return this;
        }

        public Builder resId(@DrawableRes int resId) {
            this.resId = resId;
            return this;
        }

        public Builder rawPath(String rawPath) {
            this.rawPath = rawPath;
            return this;
        }

        public Builder assertspath(String assertspath) {
            this.assertspath = assertspath;
            return this;
        }

        public Builder contentProvider(String contentProvider) {
            this.contentProvider = contentProvider;
            return this;
        }

        public Builder placeholder(int placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder errorPic(int errorPic) {
            this.errorPic = errorPic;
            return this;
        }

        public Builder thumbnail(float thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder getCacheImageSize(int cacheImageSize) {
            CACHE_IMAGE_SIZE = cacheImageSize;
            return this;
        }

        public Builder asGif(boolean asGif) {
            this.asGif = asGif;
            asDrawable = false;
            return this;
        }

        public Builder asBitmap(boolean asBitmap) {
            this.asBitmap = asBitmap;
            asDrawable = false;
            return this;
        }

        public Builder asDrawable(boolean asDrawable) {
            this.asDrawable = asDrawable;
            return this;
        }

        public Builder asFile(boolean asFile) {
            this.asFile = asFile;
            asDrawable = false;
            return this;
        }

        public Builder saveGallery(boolean saveGallery) {
            this.saveGallery = saveGallery;
            return this;
        }

        public <T> Builder imageCallBackListener(ImageCallBackListener<T> imageCallBackListener) {
            this.imageCallBackListener = imageCallBackListener;
            return this;
        }

        public Builder onProgressListener(OnProgressListener onProgressListener) {
            this.onProgressListener = onProgressListener;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder oWidth(int oWidth) {
            this.oWidth = oWidth;
            return this;
        }

        public Builder oHeight(int oHeight) {
            this.oHeight = oHeight;
            return this;
        }

        public Builder isNeedVignette(boolean isNeedVignette) {
            this.isNeedVignette = isNeedVignette;
            return this;
        }

        public Builder isNeedSketch(boolean isNeedSketch) {
            this.isNeedSketch = isNeedSketch;
            return this;
        }

        public Builder pixelationLevel(float pixelationLevel) {
            this.pixelationLevel = pixelationLevel;
            return this;
        }

        public Builder isNeedPixelation(boolean isNeedPixelation) {
            this.isNeedPixelation = isNeedPixelation;
            return this;
        }

        public Builder isNeedInvert(boolean isNeedInvert) {
            this.isNeedInvert = isNeedInvert;
            return this;
        }

        public Builder contrastLevel(float contrastLevel) {
            this.contrastLevel = contrastLevel;
            return this;
        }

        public Builder isNeedContrast(boolean isNeedContrast) {
            this.isNeedContrast = isNeedContrast;
            return this;
        }

        public Builder isNeedSepia(boolean isNeedSepia) {
            this.isNeedSepia = isNeedSepia;
            return this;
        }

        public Builder isNeedToon(boolean isNeedToon) {
            this.isNeedToon = isNeedToon;
            return this;
        }

        public Builder isNeedSwirl(boolean isNeedSwirl) {
            this.isNeedSwirl = isNeedSwirl;
            return this;
        }

        public Builder isNeedGrayscale(boolean isNeedGrayscale) {
            this.isNeedGrayscale = isNeedGrayscale;
            return this;
        }

        public Builder isNeedBrightness(boolean isNeedBrightness) {
            this.isNeedBrightness = isNeedBrightness;
            return this;
        }

        public Builder isNeedBrightness(float brightnessLeve) {
            this.brightnessLeve = brightnessLeve;
            return this;
        }

        public Builder needBlur(boolean needBlur) {
            this.needBlur = needBlur;
            return this;
        }

        public Builder needFilteColor(boolean needFilteColor) {
            this.needFilteColor = needFilteColor;
            return this;
        }

        public Builder blurRadius(int blurRadius) {
            this.blurRadius = blurRadius;
            return this;
        }

        public Builder shapeMode(int shapeMode) {
            this.shapeMode = shapeMode;
            return this;
        }

        public Builder rectRoundRadius(int rectRoundRadius) {
            this.rectRoundRadius = rectRoundRadius;
            return this;
        }

        public Builder diskCacheStrategy(DiskCacheStrategy diskCacheStrategy) {
            this.diskCacheStrategy = diskCacheStrategy;
            return this;
        }

        public Builder skipMemoryCache(boolean skipMemoryCache) {
            this.skipMemoryCache = skipMemoryCache;
            return this;
        }

        public Builder scaleMode(int scaleMode) {
            this.scaleMode = scaleMode;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder filteColor(int filteColor) {
            this.filteColor = filteColor;
            return this;
        }

        public Builder transitionOptions(TransitionOptions transitionOptions) {
            this.transitionOptions = transitionOptions;
            return this;
        }

    }
}
