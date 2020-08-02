package com.example.epic.ui.Activities;

import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.epic.R;
import com.example.epic.ui.common.helpers.CameraPermissionHelper;
import com.example.epic.ui.common.helpers.DisplayRotationHelper;
import com.example.epic.ui.common.helpers.FullScreenHelper;
import com.example.epic.ui.common.helpers.SnackbarHelper;
import com.example.epic.ui.common.helpers.TrackingStateHelper;
import com.example.epic.ui.common.rendering.AugmentedImageRenderer;
import com.example.epic.ui.common.rendering.BackgroundRenderer;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * The Majority of this code was written by Google and has been reused/edited to fit this project
 * If you wish to view the pre-edited code it can be found here: https://github.com/google-ar/arcore-android-sdk/tree/master/samples/augmented_image_java
 * Also, where comments do not exist, I have commented on them to exhibit my understanding of the work.
 */
public class AugmentedImageActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private static final String TAG = AugmentedImageActivity.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;
    private ImageView fitToScanView;
    private RequestManager glideRequestManager;

    private boolean installRequested;

    // Current session
    private Session session;

    //Displays to the user what image has been recognised
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    //Deals with rotation of the device and the camera Feed
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final AugmentedImageRenderer augmentedImageRenderer = new AugmentedImageRenderer();

    private boolean shouldConfigureSession = false;

    // Augmented image and its associated center pose anchor, keyed by index of the augmented image in
    // the
    // database.
    private final Map<Integer, Pair<AugmentedImage, Anchor>> augmentedImageMap = new HashMap<>();

    /**
     * Initialises the different views for the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_image);
        surfaceView = findViewById(R.id.glSurfaceView);
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);

        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        glideRequestManager = Glide.with(this);
        glideRequestManager
                .load(Uri.parse("file:///android_asset/fit_to_scan.png"))
                .into(fitToScanView);
        installRequested = false;
    }

    /*
     * Check that the user has the AR CORE API installed, if not request they install it or they cannot use the program,
     * There are also checks within the home activity for this concern.
     */
    @Override
        protected void onResume() {
            super.onResume();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                session = new Session(/* context = */ this);
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (Exception e) {
                message = "This device does not support AR";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }

            shouldConfigureSession = true;
        }

        if (shouldConfigureSession) {
            configureSession();
            shouldConfigureSession = false;
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }
        surfaceView.onResume();
        displayRotationHelper.onResume();

        fitToScanView.setVisibility(View.VISIBLE);
    }

    /*
     * Pausing renderers depending on activity state.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    /*
     * Checking that the application has camera permissions, not they will not be able to access this activity without giving permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                    this, "Camera permissions are needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }


    /**
     * Called when the current Window of the activity gains or loses focus.
     * This is the best indicator of whether this activity is visible to the user.
     * The default implementation clears the key tracking state, so should always be called
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    /*
    This method initialises the two rendering objects
    BackgroundRenderer -
    Augmented Image Renderer
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(/*context=*/ this);
            augmentedImageRenderer.createOnGlThread(/*context=*/ this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    /*
     Updates the viewport with the new surface
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    /*
       Stores the current camera state in a frame.
       Then renders a preview image onto the recognised surface object
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
            trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

            // If frame is ready, render camera preview image to the GL surface.
            backgroundRenderer.draw(frame);

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            // Visualize augmented images.
            drawAugmentedImages(frame, projmtx, viewmtx, colorCorrectionRgba);
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    /*
        Configuration to ensure database of images has been read in correctly.
     */
    private void configureSession() {
        Config config = new Config(session);
        config.setFocusMode(Config.FocusMode.AUTO);
        if (!setupAugmentedImageDatabase(config)) {
            messageSnackbarHelper.showError(this, "Could not setup augmented image database");
        }
        session.configure(config);
    }

    /*
        Logic for detecting Images.
     */
    private void drawAugmentedImages(
            Frame frame, float[] projmtx, float[] viewmtx, float[] colorCorrectionRgba) {
        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        // Iterate to update augmentedImageMap, remove elements we cannot draw.
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = String.format("Detected Image %s",  augmentedImage.getName());
                    messageSnackbarHelper.showMessage(this, text);
                    Log.d(TAG, "display3DModel: here call outside method");
                    break;
                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    this.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    fitToScanView.setVisibility(View.GONE);
                                }
                            });


                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage.getIndex())) {
                        Anchor centerPoseAnchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());
                        augmentedImageMap.put(
                                augmentedImage.getIndex(), Pair.create(augmentedImage, centerPoseAnchor));
                    }

                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage.getIndex());
                    break;

                default:
                    break;
            }
        }

        // Draw all images in augmentedImageMap
        for (Pair<AugmentedImage, Anchor> pair : augmentedImageMap.values()) {
            AugmentedImage augmentedImage = pair.first;
            Anchor centerAnchor = augmentedImageMap.get(augmentedImage.getIndex()).second;
            switch (augmentedImage.getTrackingState()) {
                case TRACKING:

                    //https://www.appfutura.com/blog/arcore-loading-3d-models-at-runtime/
                    Log.d(TAG, "drawAugmentedImages: drawing immage");
                    augmentedImageRenderer.draw(
                            viewmtx, projmtx, augmentedImage, centerAnchor, colorCorrectionRgba);
                    break;
                default:
                    break;
            }
        }
    }

    /*
     Simple Method To load in image database.
     I have generated this database using the ARcore Database generator tool
     It currently only contains one image, map.png for testing purposes but it can house up to 1000 images.
     */
    private boolean setupAugmentedImageDatabase(Config config) {
        AugmentedImageDatabase augmentedImageDatabase;
            // This is an alternative way to initialize an AugmentedImageDatabase instance,
            // load a pre-existing augmented image database.
            try (InputStream is = getAssets().open("Maptest.imgdb")) {
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
            } catch (IOException e) {
                Log.e(TAG, "IO exception loading augmented image database.", e);
                return false;
            }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    /*
    * This was the initial method I wrote to display 3D models once the image had been recognised.
    * The issue with this method was that it opened a new activity and did not see user friendly.
    * To fix this issue ObjectRenderers had to be implemented, a far more complex piece of code.
    public void display3DModel(){
        Intent sceneViewerIntent = new Intent(Intent.ACTION_VIEW);
        sceneViewerIntent.setData(Uri.parse("https://arvr.google.com/scene-viewer/1.0?file=https://raw.githubusercontent.com/JackGH97/3DRenderablesTest/master/Fox/model.gltf"));
        sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox");
        startActivity(sceneViewerIntent);
    }
    */
}
