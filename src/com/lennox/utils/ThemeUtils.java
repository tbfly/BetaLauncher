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
import java.util.Collections;
import java.util.Comparator;
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

    public List<Theme> getThemePackageList() {

        List<Theme> mThemeList = new ArrayList<Theme>();
        List<String> tempList = new ArrayList<String>();
        final PackageManager pm = mContext.getPackageManager();

        // Initialise with Lennox Launcher themes
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lennox.launcher.THEME");
        try {
            mThemeList.add(new Theme(DEFAULT, DEFAULT_PACKAGE, intent, pm.getApplicationIcon(DEFAULT_PACKAGE)));
        } catch(PackageManager.NameNotFoundException expt) {
            mThemeList.add(new Theme(DEFAULT, DEFAULT_PACKAGE, intent, null));
        }
        List<ResolveInfo> themes = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : themes) {
            String packageName = ri.activityInfo.packageName.toString();
            String label = getThemeName(packageName);
            Drawable icon = ri.activityInfo.applicationInfo.loadIcon(pm);
            tempList.add(packageName);
            mThemeList.add(new Theme(label, packageName, intent, icon));
        }

        // Add Apex Themes
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.anddoes.launcher.THEME");
        themes = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : themes) {
            String packageName = ri.activityInfo.packageName.toString();
            if (!tempList.contains(packageName)) {
                String label = getThemeName(packageName);
                Drawable icon = ri.activityInfo.applicationInfo.loadIcon(pm);
                tempList.add(packageName);
                mThemeList.add(new Theme(label, packageName, intent, icon));
            }
        }

        // Add Go Launcher EX themes
        intent = new Intent("com.gau.go.launcherex.theme");
        themes = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : themes) {
            String packageName = ri.activityInfo.packageName.toString();
            if (!tempList.contains(packageName)) {
                String label = getThemeName(packageName);
                Drawable icon = ri.activityInfo.applicationInfo.loadIcon(pm);
                tempList.add(packageName);
                mThemeList.add(new Theme(label, packageName, intent, icon));
            }
        }

        tempList = null;
        Collections.sort(mThemeList, new Comparator<Theme>() {
            public int compare(Theme o1, Theme o2) {
                return o1.label.compareToIgnoreCase(o2.label);
            }
        });
        return mThemeList;
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
            canvas.drawBitmap(background, 0, 0, null);
            isComposed = true;
        }

        //Bitmap pattern = getCachedThemeIcon("icon_pattern.png");
        //if (pattern != null) {
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
        String themePackage = PreferencesProvider.Interface.Theme.getThemePackageName();
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
        String themePackage = PreferencesProvider.Interface.Theme.getThemePackageName();
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
        String themePackage = PreferencesProvider.Interface.Theme.getThemePackageName();
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

    public static class Theme {

        public String label;
        public String packageName;
        public String intent;
        public Drawable icon;

        Theme(String label, String packageName, Intent intent, Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.intent = intent.toString();
            this.icon = icon;
        }

    }

}
