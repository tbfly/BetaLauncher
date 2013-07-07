/**
 * 
 */

package com.lennox.utils;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.launcher2.preference.PreferencesProvider;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Neal TODO - clean this up
 */
public class ThemeUtils {

    // SharedPreferences
    public final static String DEFAULT = "Default";
    public final static String THEME_PACKAGE_NAME = "icon_theme";

    public static String[] createThemeNameList(Context context, String[] packageNames) {
        String[] entries = new String[packageNames.length];
        for (int i = 0; i < packageNames.length; i++) {
            String themeName = ThemeUtils.getThemeName(context, packageNames[i]);
            entries[i] = themeName;
        }
        entries[0] = DEFAULT;
        return entries;
    }

    public static String[] createThemePackageList(Context context) {

        List<String> themeList = new ArrayList<String>();

        // Initialise with Apex Themes
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.anddoes.launcher.THEME");
        PackageManager pm = context.getPackageManager();
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
        values[0] = context.getPackageName();
        for (int i = 0; i < themeList.size(); i++) {
            values[i + 1] = themeList.get(i);
        }
        return values;
    }

    /**
     * @param mContext
     * @param view
     * @param resourceName
     */
    public static Drawable getDrawable(Context mContext, String resourceName) {
        String packageName = mContext.getPackageName();
        String themePackage = getThemePackageName(mContext, packageName);
        PackageManager pm = mContext.getPackageManager();
        Resources themeResources = null;
        Resources appResources = mContext.getResources();
        try {
            themeResources = pm.getResourcesForApplication(themePackage);
        } catch (NameNotFoundException e) {
            setThemePackageName(mContext, packageName);
            themeResources = appResources;
        }
        if (themeResources != null) {
            int resourceID = themeResources.getIdentifier(resourceName, "drawable", themePackage);
            if (resourceID != 0) {
               return themeResources.getDrawable(resourceID);
            }
            resourceID = appResources.getIdentifier(resourceName, "drawable", packageName);
            if (resourceID != 0) {
               return appResources.getDrawable(resourceID);
            }
        }
        return null;
    }

    /**
     * @param mContext
     * @param view
     * @param resourceName
     */
    public static int getColor(Context mContext, String resourceName) {
        String packageName = mContext.getPackageName();
        String themePackage = getThemePackageName(mContext, packageName);
        PackageManager pm = mContext.getPackageManager();
        Resources themeResources = null;
        Resources appResources = mContext.getResources();
        try {
            themeResources = pm.getResourcesForApplication(themePackage);
        } catch (NameNotFoundException e) {
            setThemePackageName(mContext, packageName);
            themeResources = appResources;
        }
        if (themeResources != null) {
            int resourceID = themeResources.getIdentifier(resourceName, "color", themePackage);
            if (resourceID != 0) {
               return themeResources.getColor(resourceID);
            }
            resourceID = appResources.getIdentifier(resourceName, "color", packageName);
            if (resourceID != 0) {
               return appResources.getColor(resourceID);
            }
        }
        return 0;
    }

    /**
     * @param mContext
     * @param view
     * @param resourceName
     */
    public static XmlPullParser getXml(Context mContext, String resourceName) {
        String packageName = mContext.getPackageName();
        String themePackage = getThemePackageName(mContext, packageName);
        PackageManager pm = mContext.getPackageManager();
        Resources themeResources = null;
        Resources appResources = mContext.getResources();
        try {
            themeResources = pm.getResourcesForApplication(themePackage);
        } catch (NameNotFoundException e) {
            setThemePackageName(mContext, packageName);
            themeResources = appResources;
        }
        if (themeResources != null) {
            int resourceID = themeResources.getIdentifier(resourceName, "xml", themePackage);
            if (resourceID != 0) {
               return themeResources.getXml(resourceID);
            }
            resourceID = appResources.getIdentifier(resourceName, "xml", packageName);
            if (resourceID != 0) {
               return appResources.getXml(resourceID);
            }
        }
        return null;
    }

    /**
     * @param mContext
     * @param packageName
     */
    public static String getThemeName(Context mContext, String packageName) {
        PackageManager pm = mContext.getPackageManager();
        String thisPackageName = mContext.getPackageName();
        Resources themeResources = null;
        if (!packageName.equals(thisPackageName)) {
            try {
                themeResources = pm.getResourcesForApplication(packageName);
            } catch (NameNotFoundException e) {
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
     * @param context
     * @param default_theme
     * @return theme package name
     */
    public static String getThemePackageName(Context context, String default_theme) {
        SharedPreferences sp = context.getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        return sp.getString(THEME_PACKAGE_NAME, default_theme);
    }

    /**
     * @param context
     * @param packageName
     */
    public static void setThemePackageName(Context context, String packageName) {
        SharedPreferences sp = context.getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(THEME_PACKAGE_NAME, packageName);
        editor.commit();
    }

}
