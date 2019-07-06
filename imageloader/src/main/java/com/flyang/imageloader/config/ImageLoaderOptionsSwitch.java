package com.flyang.imageloader.config;

import android.annotation.SuppressLint;
import android.graphics.PointF;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.Transformation;
import com.flyang.imageloader.config.inter.PriorityMode;
import com.flyang.imageloader.config.inter.ShapeMode;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ContrastFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.PixelationFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SwirlFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;


/**
 * @author caoyangfei
 * @ClassName ImageLoaderOptionsSwitch
 * @date 2019/7/5
 * ------------- Description -------------
 * glide配置
 */
public class ImageLoaderOptionsSwitch {

    /**
     * 设置图片滤镜和形状
     *
     * @param imageConfig
     * @param request
     */
    @SuppressLint("CheckResult")
    public static void setShapeModeAndBlur(ImageConfigSpread imageConfig, RequestBuilder request) {

        int count = 0;

        Transformation[] transformation = new Transformation[statisticsCount(imageConfig)];

        if (imageConfig.isNeedBlur()) {
            transformation[count] = new BlurTransformation(imageConfig.getBlurRadius());
            count++;
        }

        if (imageConfig.isNeedBrightness()) {
            transformation[count] = new BrightnessFilterTransformation(imageConfig.getBrightnessLeve()); //亮度
            count++;
        }

        if (imageConfig.isNeedGrayscale()) {
            transformation[count] = new GrayscaleTransformation(); //黑白效果
            count++;
        }

        if (imageConfig.isNeedFilteColor()) {
            transformation[count] = new ColorFilterTransformation(imageConfig.getFilteColor());
            count++;
        }

        if (imageConfig.isNeedSwirl()) {
            transformation[count] = new SwirlFilterTransformation(0.5f, 1.0f, new PointF(0.5f, 0.5f)); //漩涡
            count++;
        }

        if (imageConfig.isNeedToon()) {
            transformation[count] = new ToonFilterTransformation(); //油画
            count++;
        }

        if (imageConfig.isNeedSepia()) {
            transformation[count] = new SepiaFilterTransformation(); //墨画
            count++;
        }

        if (imageConfig.isNeedContrast()) {
            transformation[count] = new ContrastFilterTransformation(imageConfig.getContrastLevel()); //锐化
            count++;
        }

        if (imageConfig.isNeedInvert()) {
            transformation[count] = new InvertFilterTransformation(); //胶片
            count++;
        }

        if (imageConfig.isNeedPixelation()) {
            transformation[count] = new PixelationFilterTransformation(imageConfig.getPixelationLevel()); //马赛克
            count++;
        }

        if (imageConfig.isNeedSketch()) {
            transformation[count] = new SketchFilterTransformation(); //素描
            count++;
        }

        if (imageConfig.isNeedVignette()) {
            transformation[count] = new VignetteFilterTransformation(new PointF(0.5f, 0.5f),
                    new float[]{0.0f, 0.0f, 0.0f}, 0f, 0.75f);//晕映
            count++;
        }

        switch (imageConfig.getShapeMode()) {
            case ShapeMode.RECT:
                break;
            case ShapeMode.RECT_ROUND:
                transformation[count] = new RoundedCornersTransformation
                        (imageConfig.getRectRoundRadius(), 0, RoundedCornersTransformation.CornerType.ALL);
                count++;
                break;
            case ShapeMode.OVAL:
                transformation[count] = new CropCircleTransformation();
                count++;
                break;

            case ShapeMode.SQUARE:
                transformation[count] = new CropSquareTransformation();
                count++;
                break;
        }
        if (transformation.length != 0) {
            request.transform(transformation);
        }
    }

    private static int statisticsCount(ImageConfigSpread imageConfig) {
        int count = 0;

        if (imageConfig.getShapeMode() == ShapeMode.OVAL || imageConfig.getShapeMode() == ShapeMode.RECT_ROUND || imageConfig.getShapeMode() == ShapeMode.SQUARE) {
            count++;
        }

        if (imageConfig.isNeedBlur()) {
            count++;
        }

        if (imageConfig.isNeedFilteColor()) {
            count++;
        }

        if (imageConfig.isNeedBrightness()) {
            count++;
        }

        if (imageConfig.isNeedGrayscale()) {
            count++;
        }

        if (imageConfig.isNeedSwirl()) {
            count++;
        }

        if (imageConfig.isNeedToon()) {
            count++;
        }

        if (imageConfig.isNeedSepia()) {
            count++;
        }

        if (imageConfig.isNeedContrast()) {
            count++;
        }

        if (imageConfig.isNeedInvert()) {
            count++;
        }

        if (imageConfig.isNeedPixelation()) {
            count++;
        }

        if (imageConfig.isNeedSketch()) {
            count++;
        }

        if (imageConfig.isNeedVignette()) {
            count++;
        }
        return count;
    }

    /**
     * 设置加载优先级
     *
     * @param imageConfig
     * @param request
     */
    public static void setPriority(ImageConfigSpread imageConfig, RequestBuilder request) {
        switch (imageConfig.getPriority()) {
            case PriorityMode.PRIORITY_LOW:
                request.priority(Priority.LOW);
                break;
            case PriorityMode.PRIORITY_NORMAL:
                request.priority(Priority.NORMAL);
                break;
            case PriorityMode.PRIORITY_HIGH:
                request.priority(Priority.HIGH);
                break;
            case PriorityMode.PRIORITY_IMMEDIATE:
                request.priority(Priority.IMMEDIATE);
                break;
            default:
                request.priority(Priority.IMMEDIATE);
                break;
        }
    }
}
