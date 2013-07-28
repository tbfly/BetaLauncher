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
        int color = PreferencesProvider.Interface.General.getThemeColor();
        drawable.setColorFilter(color,PorterDuff.Mode.MULTIPLY);
    }

    public static void themeColor ( ImageView view ) {
        int color = PreferencesProvider.Interface.General.getThemeColor();
        view.setColorFilter(color,PorterDuff.Mode.MULTIPLY);
    }


/**
 * Converts a immutable bitmap to a mutable bitmap. This operation doesn't allocates
 * more memory that there is already allocated.
 * 
 * @param imgIn - Source image. It will be released, and should not be used more
 * @return a copy of imgIn, but muttable.
 */
public static Bitmap adjustColor (Bitmap imgIn, float adjValue) {
    if (imgIn.isMutable()) return imgIn;
    try {
        //this is the file going to use temporally to save the bytes. 
        // This file will not be a image, it will store the raw image data.
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

        //Open an RandomAccessFile
        //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        //into AndroidManifest.xml file
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

        // get the width and height of the source bitmap.
        int width = imgIn.getWidth();
        int height = imgIn.getHeight();
        Config type = imgIn.getConfig();

        //Copy the byte to the file
        //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
        FileChannel channel = randomAccessFile.getChannel();
        MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
        imgIn.copyPixelsToBuffer(map);
        //recycle the source bitmap, this will be no longer used.
        imgIn.recycle();
        System.gc();// try to force the bytes from the imgIn to be released

        //Create a new bitmap to load the bitmap again. Probably the memory will be available. 
        imgIn = Bitmap.createBitmap(width, height, type);
        map.position(0);
        //load it back from temporary 
        imgIn.copyPixelsFromBuffer(map);
        //close the temporary file and channel , then delete that also
        channel.close();
        randomAccessFile.close();

        // delete the temp file
        file.delete();

        Paint paint = new Paint();
        paint.setColorFilter(LennoxColorFilter.adjustHue(adjValue));
        Canvas canvas = new Canvas(imgIn);
        canvas.drawBitmap(imgIn, 0, 0, paint);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } 

    return imgIn;
}

}
