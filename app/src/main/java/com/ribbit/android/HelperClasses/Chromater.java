package com.ribbit.android.HelperClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.path.android.jobqueue.AsyncAddCallback;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;

/**
 * Created by ArnoldB on 8/1/2015.
 *
 * Filters for Ribbit
 * 1. Sutro
 * 2. Lomo
 * 3. Saturation
 * 4. Grayscale
 * 5. Walden
 * 6. Warm
 */
public final class Chromater {

    private String[] filters = {"Sutro", "Grayscale", "Saturation", "Lomo", "Walden", "Warm"};
    static private int nextFilter = 0;
    private volatile Bitmap bmOutFromAutoRotate;

    private Chromater() {

    }

    /*
    * Only for auto rotate filters, do not use otherwise.
    * Runs in the background.
    *
    * */
    public Chromater(Context context, Bitmap sourceBitmap){
        new JobManager(context).addJobInBackground(new RotateFilterJob(sourceBitmap), new AsyncAddCallback() {
            @Override
            public void onAdded(long jobId) {

            }
        });
    }

    public Bitmap getBitmapWhenDone(){
        while (bmOutFromAutoRotate == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return bmOutFromAutoRotate;
    }

    public static Bitmap applyAutoFilter(Bitmap sourceBitmap){
        switch(nextFilter){
            case 0: nextFilter++; return applySutroFilter(sourceBitmap);
            case 1: nextFilter++; return applyGrayscaleFilter(sourceBitmap);
            case 2: nextFilter++; return applySaturationFilter(sourceBitmap);
            case 3: nextFilter++; return applyLomoFilter(sourceBitmap);
            case 4: nextFilter++; return applyWaldenFilter(sourceBitmap);
            case 5: nextFilter++; return applyWarmFilter(sourceBitmap);
            default: nextFilter = 0; return applyAutoFilter(sourceBitmap);
        }
    }

    public static Bitmap applyWarmFilter(Bitmap sourceBitmap) {
        // image size
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, sourceBitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = sourceBitmap.getPixel(x, y);
                // apply filtering on each channel R, G, B
                A = Color.alpha(pixel);
                R = (int) (Color.red(pixel) * 0.85);//0.16666
                G = (int) (Color.green(pixel) * 0.4);//0.5
                B = (int) (Color.blue(pixel) * 0.25);//0.83333
                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
                //bmOut.setPixel(x, y, Color.argb(80, 255,127,80));
            }
        }
        // return final image
        return bmOut;
    }

    public static Bitmap applySutroFilter(Bitmap sourceBitmap){
        // image size
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, sourceBitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = sourceBitmap.getPixel(x, y);
                // apply filtering on each channel R, G, B
                A = Color.alpha(pixel);
                R = (int) (Color.red(pixel) * 0.7);//0.16666
                G = (int) (Color.green(pixel) * 0.6);//0.5
                B = (int) (Color.blue(pixel) * 0.7);//0.83333
                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        bmOut = changeContrastBrightness(bmOut, 2, 5);
        bmOut = applyVignetteEffect(bmOut);
        return bmOut;
    }

    public static Bitmap applyLomoFilter(Bitmap sourceBitmap){
        // image size
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, sourceBitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = sourceBitmap.getPixel(x, y);
                // apply filtering on each channel R, G, B
                A = Color.alpha(pixel);
                R = (int) (Color.red(pixel) * 0.7);//0.16666
                G = (int) (Color.green(pixel) * 0.6);//0.5
                B = (int) (Color.blue(pixel) * 0.6);//0.83333
                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
                //bmOut.setPixel(x, y, Color.argb(80, 255,127,80));
            }
        }
        bmOut = changeContrastBrightness(bmOut, 3,2);
        bmOut = applyVignetteEffect(bmOut);
        return bmOut;
    }

    public static Bitmap applyVignetteEffect(Bitmap sourceBitmap){
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        Bitmap bmOut = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        float radius = (float) (sourceBitmap.getWidth()/1);//1.5
        RadialGradient gradient = new RadialGradient(bmOut.getWidth()/2, bmOut.getHeight()/2, radius, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);

        Canvas canvas = new Canvas(bmOut);
        canvas.drawARGB(1, 0, 0, 0);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setShader(gradient);

        final Rect rect = new Rect(0, 0, bmOut.getWidth(), bmOut.getHeight());
        final RectF rectf = new RectF(rect);

        canvas.drawRect(rectf, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bmOut, rect, rect, paint);

        return bmOut;
    }

    public static Bitmap applySaturationFilter(Bitmap sourceBitmap){
        // image size
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, sourceBitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = sourceBitmap.getPixel(x, y);
                // apply filtering on each channel R, G, B
                A = Color.alpha(pixel);
                R = (int) (Color.red(pixel) * 0.6);//0.16666
                G = (int) (Color.green(pixel) * 0.5);//0.5
                B = (int) (Color.blue(pixel) * 0.5);//0.83333
                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
                //bmOut.setPixel(x, y, Color.argb(80, 255,127,80));
            }
        }
        bmOut = changeContrastBrightness(bmOut, 3, 2);
        // return final image
        return bmOut;
    }

    public static Bitmap applyGrayscaleFilter(Bitmap sourceBitmap){
        int width, height;
        height = sourceBitmap.getHeight();
        width = sourceBitmap.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, sourceBitmap.getConfig());
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        c.drawBitmap(sourceBitmap, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap changeContrastBrightness(Bitmap sourceBitmap, float contrast, float brightness){
        //contast 0..10 1 is default
        //brightness -255..255 0 is default

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap bmOut = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), sourceBitmap.getConfig());
        Canvas canvas = new Canvas(bmOut);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(sourceBitmap, 0, 0, paint);
        return bmOut;
    }

    public static Bitmap applyWaldenFilter(Bitmap sourceBitmap){
        // image size
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, sourceBitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = sourceBitmap.getPixel(x, y);
                // apply filtering on each channel R, G, B
                A = Color.alpha(pixel);
                R = (int) (Color.red(pixel) * 0.8);//0.16666
                G = (int) (Color.green(pixel) * 0.5);//0.5
                B = (int) (Color.blue(pixel) * 1);//0.83333
                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
                //bmOut.setPixel(x, y, Color.argb(80, 255,127,80));
            }
        }
        bmOut = changeContrastBrightness(bmOut, 4, 2);
        // return final image
        return bmOut;
    }

    public class RotateFilterJob extends Job{

        private static final int PRIORITY = 1;
        private Bitmap sourceBitmap;

        public RotateFilterJob() {
            super(new Params(PRIORITY));
        }

        public RotateFilterJob(Bitmap sourceBitmap){
            super(new Params(PRIORITY));
            this.sourceBitmap = sourceBitmap;
        }

        @Override
        public void onAdded() {

        }

        @Override
        public void onRun() throws Throwable {
            bmOutFromAutoRotate = applyAutoFilter(sourceBitmap);
        }

        @Override
        protected void onCancel() {

        }

        @Override
        protected boolean shouldReRunOnThrowable(Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }
}
