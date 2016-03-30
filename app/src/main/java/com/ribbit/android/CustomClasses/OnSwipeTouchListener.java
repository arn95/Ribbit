package com.ribbit.android.CustomClasses;

import android.content.Context;
import android.hardware.Camera;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ribbit.android.Activities.CameraActivity;

import java.util.List;

/**
 * Created by aballiu_admin on 7/30/15.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private float mDist = 0;
    private CameraActivity cameraActivity;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        cameraActivity = ((CameraActivity) context);
    }

    public void onSwipeLeft() {
        Log.d("Swipe Direction: ", "Left");
    }

    public void onSwipeRight() {
        Log.d("Swipe Direction: ", "Right");
    }

    public boolean onTouch(View v, MotionEvent event) {
        CameraPreview cameraPreviewSurfaceView = ((CameraPreview)v);

        if (!cameraActivity.isTakingPicture()) {
            if (cameraPreviewSurfaceView.isInPreview()) {
                //for double tap camera switch
                gestureDetector.onTouchEvent(event);

                //for double finger zoom
                // Get the pointer ID
                Camera.Parameters params = cameraActivity.getCamera().getParameters();
                int action = event.getAction();

                if (event.getPointerCount() > 1) {
                    // handle multi-touch events
                    if (action == MotionEvent.ACTION_POINTER_DOWN) {
                        mDist = getFingerSpacing(event);
                    } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                        cameraActivity.getCamera().cancelAutoFocus();
                        handleZoom(event, params);
                    }
                } else {
                    // handle single touch events
                    if (action == MotionEvent.ACTION_UP) {
                        handleFocus(event, params);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        if (cameraActivity.getCameraPreviewSurfaceView().isInPreview()) {
            if (cameraActivity.getPreviewType() == 1) {
                int maxZoom = params.getMaxZoom();
                int zoom = params.getZoom();
                float newDist = getFingerSpacing(event);
                if (newDist > mDist) {
                    //zoom in
                    if (zoom < maxZoom)
                        zoom++;
                } else if (newDist < mDist) {
                    //zoom out
                    if (zoom > 0)
                        zoom--;
                }
                mDist = newDist;
                params.setZoom(zoom);
                cameraActivity.getCamera().setParameters(params);
            }
        }
    }

    private void handleFocus(MotionEvent event, Camera.Parameters params) {
        if (cameraActivity.getCameraPreviewSurfaceView().isInPreview()) {
            if (cameraActivity.getPreviewType() == 1) {
                int pointerId = event.getPointerId(0);
                int pointerIndex = event.findPointerIndex(pointerId);
                // Get the pointer's current position
                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);

                List<String> supportedFocusModes = params.getSupportedFocusModes();
                if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    cameraActivity.getCamera().autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean b, Camera camera) {
                            // currently set to auto-focus on single touch
                        }
                    });
                }
            }
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (cameraActivity.getPreviewType() == 2) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (cameraActivity.getPreviewType() == 1) {
                cameraActivity.switchCamera();
                YoYo.with(Techniques.RubberBand).playOn(cameraActivity.getSwitchCameraButton());
                return true;
            }
            return false;
        }
    }
}
