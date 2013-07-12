package com.lennox.livewallpaperpicker;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;

public class LiveWallpaperFix extends Activity {

    private static final int REQUEST_LIVE_WALLPAPER_CHOOSER = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        startActivityForResult(intent, REQUEST_LIVE_WALLPAPER_CHOOSER);
    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LIVE_WALLPAPER_CHOOSER) {
            finish();
        }
    }

}
