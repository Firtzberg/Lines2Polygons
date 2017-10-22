package com.firtzberg.lines2polygons;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.firtzberg.lines2polygons.elements.Grid;
import com.firtzberg.lines2polygons.opengl.GridRenderer;

/**
 * Taken from www.learnopengles.com
 */
public class OpenGLActivity extends Activity {

    public static final String PARCELABLE_GRID_KEY = "grid";
    /**
     * Hold a reference to our GLSurfaceView
     */
    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new GLSurfaceView(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);

            mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);


            Grid grid = this.getIntent().getParcelableExtra(PARCELABLE_GRID_KEY);

            // Set the renderer to our demo renderer, defined below.
            mGLSurfaceView.setRenderer(new GridRenderer(this, grid));
        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }
}