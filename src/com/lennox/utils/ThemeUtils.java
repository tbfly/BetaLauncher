/**
 * 
 */

package com.lennox.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

import com.lennox.launcher.preference.PreferencesProvider;
import com.lennox.launcher.LauncherApplication;

import org.xmlpull.v1.XmlPullParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Neal TODO - clean this up
 */
public class ThemeUtils {

    // SharedPreferences
    public final static String DEFAULT = "Default";
    public final static String DEFAULT_PACKAGE = "com.lennox.launcher";
    public final static String THEME_PACKAGE_NAME = "icon_theme";

    private static final Canvas sCanvas = new Canvas();
    public static final int sCustomizedIconHeight = scalePixel(96);
    public static final int sCustomizedIconWidth = scalePixel(96);
    private static final int sIconWidth = scalePixel(72);
    private static final int sIconHeight = scalePixel(72);
    private static final Resources sSystemResource = Resources.getSystem();
    private static final int sDensity = sSystemResource.getDisplayMetrics().densityDpi;
    private static final Rect sOldBounds = new Rect();

    private final LauncherApplication mContext;

    public ThemeUtils(LauncherApplication context) {
        mContext = context;
    }

    private static int scalePixel(int px) {
        int density = sDensity;
        if (density <= 0)
            density = 240;
        if (sDensity == 320) {
            density = 360;
        } 
        return px * density / 240;
    } 
    
    public String[] createThemeNameList(String[] packageNames) {
        String[] entries = new String[packageNames.length];
        for (int i = 0; i < packageNames.length; i++) {
            String themeName = getThemeName(packageNames[i]);
            entries[i] = themeName;
        }
        entries[0] = DEFAULT;
        return entries;
    }

    public String[] createThemePackageList() {

        List<String> themeList = new ArrayList<String>();

        // Initialise with Apex Themes
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.anddoes.launcher.THEME");
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> themes = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : themes) {
            themeList.add(ri.activityInfo.packageName.toString());
        }

        // Add Go Launcher EX themes
        intent = new Intent("com.gau.go.launcherex.theme");
        themes = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : themes) {
            String packageName = ri.activityInfo.packageName.toString();
            if (!themeList.contains(packageName)) {
                themeList.add(packageName);
            }
        }

        String[] values = new String[themeList.size() + 1];
        values[0] = DEFAULT_PACKAGE;
        for (int i = 0; i < themeList.size(); i++) {
            values[i + 1] = themeList.get(i);
        }
        return values;
    }
    
    private Bitmap composeIcon(Bitmap base, Bitmap background) {
        int baseWidth = base.getWidth();
        int baseHeight = base.getHeight();
        int[] basePixels = new int[baseWidth * baseHeight];
        boolean isComposed = false;
        base.getPixels(basePixels, 0, baseWidth, 0, 0, baseWidth, baseHeight);

        Bitmap result = Bitmap.createBitmap(sCustomizedIconWidth, sCustomizedIconHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        //Bitmap background = getCachedThemeIcon("icon_background.png");
        if (background != null) {
            android.util.Log.d("LX", "composeIcon() drawing background");
            canvas.drawBitmap(background, 0, 0, null);
            isComposed = true;
        }

        //Bitmap pattern = getCachedThemeIcon("icon_pattern.png");
        //if (pattern != null) {
        //    android.util.Log.d("LX", "composeIcon() drawing icon pattern");
        //    canvas.drawBitmap(pattern, 0.0F, 0.0F, null);
         //   isComposed = true;
        //}
        canvas.drawBitmap(basePixels, 0, baseWidth, (sCustomizedIconWidth - baseWidth) / 2, 
                (sCustomizedIconHeight - baseHeight) / 2, baseWidth, baseHeight, true, null);

        //Bitmap foreground = getCachedThemeIcon("icon_border.png");
        //if (foreground != null) {
        //    android.util.Log.d("LX", "composeIcon() drawing foreground");
        //    canvas.drawBitmap(foreground, 0.0F, 0.0F, null);
        //    isComposed = true;
        //}
        if (result == null)
            android.util.Log.e("LX", "composeIcon: result bitmap is null");

        if (isComposed == false)
            result = scaleBitmap(base, 1.0f);
        base.recycle();
        return result;
    }
   
    private Bitmap drawableToBitmap(Drawable drawable) {
        int i = sIconWidth;
        int j = sIconHeight;
        int k = i;
        int l = j;
        int i1;
        int j1;
        if(drawable instanceof PaintDrawable)
        {
            PaintDrawable paintdrawable = (PaintDrawable)drawable;
            paintdrawable.setIntrinsicWidth(i);
            paintdrawable.setIntrinsicHeight(j);
        } else
        if(drawable instanceof BitmapDrawable)
        {
            BitmapDrawable bitmapdrawable = (BitmapDrawable)drawable;
            if(bitmapdrawable.getBitmap().getDensity() == 0)
                bitmapdrawable.setTargetDensity(sSystemResource.getDisplayMetrics());
        }
        i1 = drawable.getIntrinsicWidth();
        j1 = drawable.getIntrinsicHeight();
        Bitmap bitmap;
        Canvas canvas1;
        int k1;
        int l1;
        if(i1 > 0 && i1 > 0)
            if(k < i1 || l < j1)
            {
                float f = (float)i1 / (float)j1;
                if(i1 > j1)
                    l = (int)((float)k / f);
                else
                if(j1 > i1)
                    k = (int)(f * (float)l);
            } else
            if(i1 < k && j1 < l)
            {
                k = i1;
                l = j1;
            }
        bitmap = Bitmap.createBitmap(i, j, android.graphics.Bitmap.Config.ARGB_8888);
        canvas1 = sCanvas;
        canvas1.setBitmap(bitmap);
        k1 = (i - k) / 2;
        l1 = (j - l) / 2;
        sOldBounds.set(drawable.getBounds());
        drawable.setBounds(k1, l1, k1 + k, l1 + l);
        drawable.draw(canvas1);
        drawable.setBounds(sOldBounds);
        return bitmap;
    }
     
    public BitmapDrawable generateIconDrawable(String filename, Drawable icon,
                                                       Drawable image, float scale) {
        Bitmap cachedIcon = getCachedThemeIcon(filename);
        if (cachedIcon != null) {
            return bitmapToDrawable(cachedIcon);
        }
        if (image != null) {
            Bitmap iconBitmap = scaleBitmap(drawableToBitmap(icon), scale);
            Bitmap imageBitmap = scaleBitmap(drawableToBitmap(image), 1.0f);
            return scaleDrawable(composeIcon(iconBitmap, imageBitmap), 1.0f, filename);
        } else {
            return scaleDrawable(composeIcon(drawableToBitmap(icon), null), 1.0f, filename);
        }
    }

    //public static BitmapDrawable scaleIconDrawable(Drawable icon) {
    //    return scaleDrawable(drawableToBitmap(icon), 1.0f);
    //} 

    public Bitmap getCachedThemeIcon(String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap localBitmap = BitmapFactory.decodeFile(mContext.getFilesDir() + "/" + fileName, options);
        return scaleBitmap(localBitmap, 1.0f);
    } 

    private Bitmap scaleBitmap(Bitmap icon, float scale) {
        return scaleBitmap(icon, (int) ((float) sCustomizedIconWidth * (float) scale),
                                (int) ((float) sCustomizedIconHeight * (float) scale));
    } 
    
    private Bitmap scaleBitmap(Bitmap icon, int iconWidth, int iconHeight) {
        Bitmap bitmap = null;
        if (icon == null)
            return null;

        if ((icon.getWidth() == iconWidth) || (icon.getHeight() == iconHeight))
            return icon;

        bitmap = Bitmap.createScaledBitmap(icon, iconWidth, iconHeight, true);
        bitmap.setDensity(sDensity);
        return bitmap;
    }
      
    private BitmapDrawable bitmapToDrawable(Bitmap icon) {
        BitmapDrawable bd = null;
        if (icon != null)
        {
            Bitmap bitmap = scaleBitmap(icon, 1.0f);
            bd = new BitmapDrawable(sSystemResource, bitmap);
        } 
        return bd;
    }

    private BitmapDrawable scaleDrawable(Bitmap icon, float scale, String filename) {
        BitmapDrawable bd = null;
        if (icon != null)
        {
            Bitmap bitmap = scaleBitmap(icon, scale);
            saveCustomizedIconBitmap(filename, bitmap);
            bd = new BitmapDrawable(sSystemResource, bitmap);
        } 
        return bd;
    } 

    public void clearCache() {
        File dir = mContext.getFilesDir();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        });
        for (File pngfile : files) {
             pngfile.delete();
        }
    }

    public void saveCustomizedIconBitmap(String fileName, Bitmap icon) {
        FileOutputStream outputStream = null;
        try {
            outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
        } catch (FileNotFoundException fileNotFoundException) {
            //fileNotFoundException.printStackTrace();
            return;
        }
        if (outputStream == null) {
            //e.printStackTrace();
            return;
        }
        icon.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {}
    }

    /**
     * @param resourceName
     */
    public Drawable getDrawable(String resourceName) {
        String themePackage = PreferencesProvider.Interface.General.getThemePackageName();
        PackageManager pm = mContext.getPackageManager();
        Resources themeResources = null;
        try {
            themeResources = pm.getResourcesForApplication(themePackage);
        } catch (NameNotFoundException e) {
            setThemePackageName(DEFAULT_PACKAGE);
        }
        if (themeResources != null) {
            int resourceID = themeResources.getIdentifier(resourceName, "drawable", themePackage);
            if (resourceID != 0) {
               return themeResources.getDrawable(resourceID);
            }
        }
        return null;
    }

    /**
     * @param resourceName
     */
    public int getColor(String resourceName) {
        String themePackage = PreferencesProvider.Interface.General.getThemePackageName();
        PackageManager pm = mContext.getPackageManager();
        Resources themeResources = null;
        try {
            themeResources = pm.getResourcesForApplication(themePackage);
        } catch (NameNotFoundException e) {
            setThemePackageName(DEFAULT_PACKAGE);
        }
        if (themeResources != null) {
            int resourceID = themeResources.getIdentifier(resourceName, "color", themePackage);
            if (resourceID != 0) {
               return themeResources.getColor(resourceID);
            }
        }
        return 0;
    }

    /**
     * @param resourceName
     */
    public XmlPullParser getXml(String resourceName) {
        String themePackage = PreferencesProvider.Interface.General.getThemePackageName();
        PackageManager pm = mContext.getPackageManager();
        Resources themeResources = null;
        try {
            themeResources = pm.getResourcesForApplication(themePackage);
        } catch (NameNotFoundException e) {
            setThemePackageName(DEFAULT_PACKAGE);
        }
        if (themeResources != null) {
            int resourceID = themeResources.getIdentifier(resourceName, "xml", themePackage);
            if (resourceID != 0) {
               return themeResources.getXml(resourceID);
            }
        }
        return null;
    }

    /**
     * @param packageName
     */
    public String getThemeName(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        Resources themeResources = null;
        if (!packageName.equals(DEFAULT_PACKAGE)) {
            try {
                themeResources = pm.getResourcesForApplication(packageName);
            } catch (NameNotFoundException e) {
                setThemePackageName(DEFAULT_PACKAGE);
                return DEFAULT;
            }
        } else {
            return DEFAULT;
        }
        if (themeResources != null) {
            int resourceID = themeResources.getIdentifier("theme_title", "string", packageName);
            if (resourceID != 0) {
               return themeResources.getString(resourceID);
            }
            resourceID = themeResources.getIdentifier("theme_name", "string", packageName);
            if (resourceID != 0) {
               return themeResources.getString(resourceID);
            }
            resourceID = themeResources.getIdentifier("app_label", "string", packageName);
            if (resourceID != 0) {
               return themeResources.getString(resourceID);
            }
        }
        return packageName;
    }

    /**
     * @param packageName
     */
    public void setThemePackageName(String packageName) {
        SharedPreferences sp = mContext.getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(THEME_PACKAGE_NAME, packageName);
        editor.commit();
    }

}
