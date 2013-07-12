package com.lennox.sunbeam;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.app.Service;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.renderscript.RenderScriptGL;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.android.launcher2.preference.PreferencesProvider;

public class PhaseBeamWallpaper extends WallpaperService {

    @Override
    public Engine onCreateEngine() {

        return new RenderScriptEngine();
    }

    private class RenderScriptEngine extends Engine implements OnSharedPreferenceChangeListener {
        private RenderScriptGL mRenderScript = null;
        private PhaseBeamRS mWallpaperRS = null;
        private int mDensityDPI;

        private Context mContext;
        private SharedPreferences mSharedPref;

        private static final int COLOUR_RED = 0;
        private static final int COLOUR_BLUE = 1;
        private static final int COLOUR_GREEN = 2;

        private int mHeight;
        private int mWidth;

        private int mBeamColour;
        private int mDotColour;
        private int mBackgroundColour;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
            surfaceHolder.setSizeFromLayout();
            surfaceHolder.setFormat(PixelFormat.OPAQUE);

            mContext = getApplicationContext();
            mSharedPref = mContext.getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, 0);
            mSharedPref.registerOnSharedPreferenceChangeListener(this);

            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) getApplication().getSystemService(Service.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(metrics);
            mDensityDPI = metrics.densityDpi;

        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            startUp(true);
        }

        private void startUp(boolean force) {
            if (mWallpaperRS != null && force) {
                mWallpaperRS.stop();
                mWallpaperRS = null;
            }
            mBeamColour = Integer.parseInt(mSharedPref.getString("livewallpaper_beam_colour","0"));
            mDotColour = Integer.parseInt(mSharedPref.getString("livewallpaper_dot_colour","0"));
            mBackgroundColour = Integer.parseInt(mSharedPref.getString("livewallpaper_background_colour","0"));
            if (mWallpaperRS == null) {
                mWallpaperRS = new PhaseBeamRS();
                mWallpaperRS.init(mDensityDPI, mRenderScript, getResources(),
                                  mWidth, mHeight, mBeamColour, mDotColour, mBackgroundColour);
                mWallpaperRS.start();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mSharedPref.unregisterOnSharedPreferenceChangeListener(this);
            destroyRenderer();
        }

        public void destroyRenderer() {
            if (mWallpaperRS != null) {
                mWallpaperRS.stop();
                mWallpaperRS = null;
            }

            if (mRenderScript != null) {
                mRenderScript.setSurface(null, 0, 0);
                mRenderScript.destroy();
                mRenderScript = null;
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);

            RenderScriptGL.SurfaceConfig sc = new RenderScriptGL.SurfaceConfig();
            mRenderScript = new RenderScriptGL(PhaseBeamWallpaper.this, sc);
            mRenderScript.setPriority(RenderScript.Priority.NORMAL);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
            super.onSurfaceDestroyed(surfaceHolder);
            destroyRenderer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int format, int width,
                int height) {
            super.onSurfaceChanged(surfaceHolder, format, width, height);

            if (mRenderScript != null) {
                mRenderScript.setSurface(surfaceHolder, width, height);
            }

            mHeight = height;
            mWidth = width;

            startUp(false);

            mWallpaperRS.resize(width, height);
        }

        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras,
                boolean resultRequested) {
            return null;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (mWallpaperRS != null) {
                if (visible) {
                    mWallpaperRS.start();
                } else {
                    mWallpaperRS.stop();
                }
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            // TODO: Uncomment this once we can work out framerate issues
            //mWallpaperRS.setOffset(xOffset, yOffset, xPixelOffset, yPixelOffset);
        }
    }
}