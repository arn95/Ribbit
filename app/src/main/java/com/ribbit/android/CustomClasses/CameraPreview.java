package com.ribbit.android.CustomClasses;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    public boolean inPreview = false;
    private Camera.Size correctPreviewSize;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        initiateCameraPreview();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context) {
        super(context);
    }

    public void initiateCameraPreview(){
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void setCamera(Camera camera){
        this.camera = camera;
    }

    public boolean isInPreview(){
        return inPreview;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (camera != null) {
            List<Camera.Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
            if (supportedPreviewSizes != null) {
                correctPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
            }
        }
        super.onMeasure(width, height);
    }

    public void removeHolderCallback(){
        surfaceHolder.removeCallback(this);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(correctPreviewSize.width, correctPreviewSize.height);
            camera.setParameters(parameters);

            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            inPreview = true;
        } catch (IOException e) {
            Log.d("Camera Activity: ", "Error setting camera preview: " + e.getMessage());
        }
        Log.d("Camera Preview: ", "Surface Created");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        inPreview = false;
        camera.release();
        Log.d("Camera Preview: ", "Surface Destroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (surfaceHolder.getSurface() == null){
            // preview surface does not exist
            inPreview = false;
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
            inPreview = false;
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(correctPreviewSize.width, correctPreviewSize.height);
        camera.setParameters(parameters);

        try {
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            inPreview = true;
        } catch (Exception e){
            Log.d("Camera Preview: ", "Error starting camera preview: " + e.getMessage());
        }
        Log.d("Camera Preview: ", "Surface Changed");
    }

    public Camera.Size getCorrectPreviewSize(){
        return correctPreviewSize;
    }
}