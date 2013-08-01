package com.lennox.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.MappedByteBuffer;

import com.lennox.launcher.R;
import com.lennox.launcher.preference.PreferencesProvider;

public class LennoxColorFilter
{

    public static ColorFilter adjustHue( float value ) {
        ColorMatrix cm = new ColorMatrix();
        adjustHue(cm, value);
        return new ColorMatrixColorFilter(cm);
    }

    public static void adjustHue(ColorMatrix cm, float value) {
        value = cleanValue(value, 180f) / 180f * (float) Math.PI;
        if (value == 0) {
            return;
            }
        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = new float[] { 
            lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0, 
            lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
            lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0, 
            0f, 0f, 0f, 1f, 0f, 
            0f, 0f, 0f, 0f, 1f };
        cm.postConcat(new ColorMatrix(mat));
    }

    protected static float cleanValue(float p_val, float p_limit) {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }

    public static void adjustColor ( ImageView view, float adjValue ) {
    	view.setColorFilter(LennoxColorFilter.adjustHue(adjValue));
    }

    public static void adjustColor ( Drawable drawable, float adjValue ) {
    	drawable.setColorFilter(LennoxColorFilter.adjustHue(adjValue));
    }

    public static void themeColor ( Drawable drawable ) {
        int color = PreferencesProvider.Interface.Theme.getThemeColor();
        drawable.setColorFilter(color,PorterDuff.Mode.MULTIPLY);
    }

    public static void themeColor ( ImageView view ) {
        int color = PreferencesProvider.Interface.Theme.getThemeColor();
        view.setColorFilter(color,PorterDuff.Mode.MULTIPLY);
    }

}
