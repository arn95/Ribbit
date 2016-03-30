package com.ribbit.android.Activities;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.ribbit.R;
import com.ribbit.android.CustomClasses.AnimatedCameraFooterButton;
import com.ribbit.android.CustomClasses.AnimatedCameraPreviewButton;
import com.ribbit.android.CustomClasses.CameraPreview;
import com.ribbit.android.CustomClasses.FlashScreenImageView;
import com.ribbit.android.CustomClasses.OnSwipeTouchListener;
import com.ribbit.android.HelperClasses.Chromater;
import com.ribbit.android.ParseObjects.Post;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity {

    @Bind(R.id.camera_capture_button)
    AnimatedCameraFooterButton captureButton;
    @Bind(R.id.camera_change_camera_button) AnimatedCameraPreviewButton switchCameraButton;
    @Bind(R.id.camera_flash_button) AnimatedCameraPreviewButton flashButton;
    @Bind(R.id.camera_front_flash)
    FlashScreenImageView frontFlashView;
    @Bind(R.id.camera_preview) RelativeLayout cameraPreviewRelativeLayout;
    @Bind(R.id.camera_close_preview_button)
    AnimatedCameraPreviewButton closePreviewButton;
    @Bind(R.id.camera_done_button)
    AnimatedCameraFooterButton doneButton;
    @Bind(R.id.camera_preview_picture)
    ImageView previewImageView;


    static final int MEDIA_TYPE_IMAGE = 1;
    static final int MEDIA_TYPE_VIDEO = 2;

    private int previewType = 1;
    private int currentCameraId;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private boolean hasFlash;
    private boolean backFlashOn = false;
    private boolean frontFlashOn = false;
    private boolean takingPicture = false;
    private boolean activityPaused = false;
    private boolean isFocusing = false;
    private CameraPreview cameraPreviewSurfaceView;
    private Uri currentPicturePreviewUri;
    private File currentPictureFile;
    private boolean isFiltered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(CameraActivity.this);

        //fullscreen activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create an instance of Camera
        if (savedInstanceState != null) {
            if (savedInstanceState.getInt("cameraId") == Camera.CameraInfo.CAMERA_FACING_BACK) {
                currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            else
                currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        camera = getCameraInstance();
        applyCorrectPictureRotation();
        addCameraSurfaceViewToScreen();
        setCameraPreviewListeners();
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraPreviewSurfaceView.isInPreview()) {
                    if (!takingPicture) {
                        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT && frontFlashOn) {
                            YoYo.with(Techniques.ZoomOut).playOn(captureButton);
                            frontFlashView.setVisibility(View.VISIBLE);
                            takePicture();
                            //Note: camera.startPreview called after onPictureTaken
                        }
                        else {
                            YoYo.with(Techniques.ZoomOut).playOn(captureButton);
                            takePicture();
                        }
                    }
                }
            }
        });

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            flashButton.setVisibility(View.VISIBLE);
            flashButton.setImageResource(R.drawable.ic_flash_off_white_36dp);
        }

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!takingPicture) {
                    if (cameraPreviewSurfaceView.isInPreview()) {
                        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            if (backFlashOn) {
                                flashButton.setImageResource(R.drawable.ic_flash_off_white_36dp);
                                flashButton.setVisibility(View.VISIBLE);
                                camera.stopPreview();
                                Camera.Parameters parameters = camera.getParameters();
                                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                camera.setParameters(parameters);
                                camera.startPreview();
                                backFlashOn = false;
                            } else {
                                flashButton.setImageResource(R.drawable.ic_flash_on_white_36dp);
                                flashButton.setVisibility(View.VISIBLE);
                                camera.stopPreview();
                                Camera.Parameters parameters = camera.getParameters();
                                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                camera.setParameters(parameters);
                                camera.startPreview();
                                backFlashOn = true;
                            }
                        } else if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            if (frontFlashOn) {
                                flashButton.setImageResource(R.drawable.ic_flash_off_white_36dp);
                                flashButton.setVisibility(View.VISIBLE);
                                frontFlashOn = false;
                            } else {
                                flashButton.setImageResource(R.drawable.ic_flash_on_white_36dp);
                                flashButton.setVisibility(View.VISIBLE);
                                frontFlashOn = true;
                            }
                        }
                    }
                }
            }
        });

        if (Camera.getNumberOfCameras() > 1) {
            switchCameraButton.setVisibility(View.VISIBLE);
            switchCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!takingPicture) {
                        if (cameraPreviewSurfaceView.isInPreview()) {
                            switchCamera();
                            YoYo.with(Techniques.RubberBand).playOn(switchCameraButton);
                        }
                    }
                }
            });
        }
        Log.d("Activity State: ", "onCreate");
    }

    public void applyCorrectPictureRotation(){
        Camera.Parameters rotationParam = camera.getParameters();
        rotationParam.setRotation(getCorrectPictureRotation());
        camera.setParameters(rotationParam);
    }

    public void setCameraPreviewListeners(){
        cameraPreviewSurfaceView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                Log.d("Swipe ", "Left");
                isFiltered = true;
            }

            @Override
            public void onSwipeRight() {
                Log.d("Swipe ", "Right");
                isFiltered = true;
            }
        });

    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap rotateImageCorrectly(String pictureFilePath){
        ExifInterface ei = null;
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFilePath);
        try {
            ei = new ExifInterface(pictureFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);
            default: return null;
            // etc.
        }
    }

    public int getCorrectPictureRotation(){
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraId, info);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break; //Natural orientation
            case Surface.ROTATION_90: degrees = 90; break; //Landscape left
            case Surface.ROTATION_180: degrees = 180; break;//Upside down
            case Surface.ROTATION_270: degrees = 270; break;//Landscape right
        }
        int rotate = (info.orientation - degrees + 360) % 360;
        return rotate;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
        camera.stopPreview();
        releaseCamera();
        releaseMediaRecorder();
        cameraPreviewSurfaceView.removeHolderCallback();
        cameraPreviewRelativeLayout.removeView(cameraPreviewSurfaceView);
        Log.d("Activity State: ", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityPaused) {
            camera = getCameraInstance();
            applyCorrectPictureRotation();
            addCameraSurfaceViewToScreen();
            setCameraPreviewListeners();
            activityPaused = false;
        }
        Log.d("Activity State: ", "onResume");
    }

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "RibbitMedia");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (mediaStorageDir != null) {
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("Ribbit Media: ", "failed to create directory");
                    return null;
                }
            }
        }
        else {
            Log.d("Ribbit Media: ", "failed to create directory, external storage not mounted");
            return null;
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        try {
            mediaFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaFile;
    }


    public void takePicture() {
        takingPicture = true;
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                switchToPicturePreview(data);
                takingPicture = false;
            }
        });
    }

    public File savePictureLocally(byte[] data, boolean isFiltered){
        //put the data in Bitmap
        //Bitmap pictureInBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        //pictureInBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        //make the bitmap a file and save it
        byte[] filteredImageData;
        File pictureFile;

        if (isFiltered){
            Bitmap pictureInBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
            pictureInBitmap = new Chromater(this, pictureInBitmap).getBitmapWhenDone();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            pictureInBitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
            filteredImageData = bos.toByteArray();
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("Ribbit Media: ", "Error creating media file, check storage permissions");
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(filteredImageData);
                fos.close();
                Log.d("New Image saved: ", pictureFile.getPath());
            } catch (Exception error) {
                Log.d("Take pic: ", "File" + pictureFile.getPath() + " not saved: "
                        + error.getMessage());
            }
        }
        else {
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("Ribbit Media: ", "Error creating media file, check storage permissions");
                return null;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.d("New Image saved: ", pictureFile.getPath());
            } catch (Exception error) {
                Log.d("Take pic: ", "File" + pictureFile.getPath() + " not saved: "
                        + error.getMessage());
            }
        }
        return pictureFile;
    }

    public void switchToPicturePreview(final byte[] data){
        previewType = 2;
        captureButton.setVisibility(View.INVISIBLE);
        flashButton.setVisibility(View.INVISIBLE);
        switchCameraButton.setVisibility(View.INVISIBLE);

        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File pictureFile = savePictureLocally(data, isFiltered);
                Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getPath());
                previewImageView.setImageBitmap(bitmap);
                previewImageView.setVisibility(View.VISIBLE);
                currentPicturePreviewUri = Uri.fromFile(pictureFile);
                /*uploadImageToParse(pictureFile.getPath());
                switchToCameraPreview();*/
            }
        });
        closePreviewButton.setVisibility(View.VISIBLE);
        closePreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToCameraPreview();
            }
        });
    }

    public void switchToCameraPreview(){
        previewType = 1;
        doneButton.setVisibility(View.INVISIBLE);
        closePreviewButton.setVisibility(View.INVISIBLE);

        captureButton.setVisibility(View.VISIBLE);
        flashButton.setVisibility(View.VISIBLE);
        switchCameraButton.setVisibility(View.VISIBLE);

        camera.startPreview();
    }

    public void uploadImageToParse(String pictureFilePath){
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFilePath);
        // Convert it to byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] image = stream.toByteArray();

        // Create the ParseFile
        ParseFile file = new ParseFile(image);
        // Upload the image into Parse Cloud
        file.saveInBackground();

        Post post = new Post();
        post.setOwner(ParseUser.getCurrentUser());
        post.setFile(file);
        post.setType("image");
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    Log.i("Ribbit Media: ", "Image uploaded");
                else
                    e.printStackTrace();
            }
        });
    }

    public void switchCamera(){
        //Bundle bundle = new Bundle();
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        else
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        //stop preview
        camera.stopPreview();
        //release current camera
        releaseCamera();
        //remove the callback to the current camera preview
        cameraPreviewSurfaceView.removeHolderCallback();
        //remove the surface view to clear everything from the current camera
        cameraPreviewRelativeLayout.removeView(cameraPreviewSurfaceView);
        //get the new camera the user requires
        camera = getCameraInstance();
        //apply the correct rotation
        applyCorrectPictureRotation();
        //add the surface view to screen again with the new camera preview
        addCameraSurfaceViewToScreen();
        //set the double tap and single tap listeners
        setCameraPreviewListeners();
    }

    public void addCameraSurfaceViewToScreen(){
        cameraPreviewSurfaceView = new CameraPreview(this,camera);
        RelativeLayout.LayoutParams surfaceViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        //surfaceViewParams.addRule(RelativeLayout.ABOVE, R.id.camera_footer_bar);
        surfaceViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        surfaceViewParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        surfaceViewParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        surfaceViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        cameraPreviewRelativeLayout.addView(cameraPreviewSurfaceView, 0, surfaceViewParams);
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (camera != null){
            cameraPreviewSurfaceView.setCamera(null);
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    public Camera getBackCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance
        }
        catch (Exception e){
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        return c; // returns null if camera is unavailable
    }

    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(currentCameraId); // attempt to get a Camera instance
        }
        catch (Exception e){
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public boolean isTakingPicture() {
        return takingPicture;
    }

    public boolean isActivityPaused() {
        return activityPaused;
    }

    public CameraPreview getCameraPreviewSurfaceView() {
        return cameraPreviewSurfaceView;
    }

    public int getPreviewType(){
        return previewType;
    }

    public Camera getCamera() {
        return camera;
    }

    public int getCurrentCameraId() {
        return currentCameraId;
    }

    public ImageButton getSwitchCameraButton() {
        return switchCameraButton;
    }

    public Camera getFrontCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
        }
        catch (Exception e){
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        return c; // returns null if camera is unavailable
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
