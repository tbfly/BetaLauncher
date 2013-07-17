/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher2;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Gravity;

import com.android.launcher.R;
import com.lennox.utils.ThemeUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {
    @SuppressWarnings("unused")
    private static final String TAG = "Launcher.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private static class CacheEntry {
        public Bitmap icon;
        public String title;
    }

    private final Bitmap mDefaultIcon;
    private final LauncherApplication mContext;
    private final PackageManager mPackageManager;
    private final HashMap<ComponentName, CacheEntry> mCache =
            new HashMap<ComponentName, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);
    private int mIconDpi;
    private XmlPullParser xrp;
    private HashMap <String, String> iconThemeMap;
    private ArrayList<String> iconBackList = null;
    private ArrayList<String> iconFrontList = null;
    private float scaleFactor = 1.0f;

    public IconCache(LauncherApplication context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        mContext = context;
        mPackageManager = context.getPackageManager();
        mIconDpi = activityManager.getLauncherLargeIconDensity();

        // need to set mIconDpi before getting default icon
        mDefaultIcon = makeDefaultIcon();
        setupIconThemeMap();
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    private void setupIconThemeMap() {
        iconThemeMap = new HashMap<String, String>();
        iconBackList = new ArrayList<String>();
        iconFrontList = new ArrayList<String>();
        if (xrp == null) {
            xrp = ThemeUtils.getXml(mContext,"appfilter");
        }
        if ( xrp == null ) {
            try {
                Context otherContext = mContext.createPackageContext(ThemeUtils.getThemePackageName(mContext, mContext.getPackageName()), 0);
                AssetManager am = otherContext.getAssets();
                InputStream istr = am.open("appfilter.xml");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
                factory.setNamespaceAware(true); 
                xrp = factory.newPullParser(); 
                xrp.setInput(istr, "UTF-8");
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if ( xrp == null ) {
            return;
        }
        String componentInfo, drawableName;
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("item")) {
                        componentInfo = xrp.getAttributeValue(null, "component");
                        drawableName = xrp.getAttributeValue(null, "drawable");
                        iconThemeMap.put(componentInfo, drawableName);
                    } else if (s.equals("iconback")) {
                        int length = xrp.getAttributeCount();
                        for (int i = 0; i < length; i++) {
                            String attributeName = xrp.getAttributeName(i);
                            if (attributeName.startsWith("img")) {
                                iconBackList.add(xrp.getAttributeValue(null, attributeName));
                            }
                        }
                    } else if (s.equals("iconupon")) {
                        int length = xrp.getAttributeCount();
                        for (int i = 0; i < length; i++) {
                            String attributeName = xrp.getAttributeName(i);
                            if (attributeName.startsWith("img")) {
                                iconFrontList.add(xrp.getAttributeValue(null, attributeName));
                            }
                        }
                    } else if (s.equals("scale")) {
                        scaleFactor = Float.parseFloat(xrp.getAttributeValue(null, "factor"));
                    }
                }
                xrp.next();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Drawable getThemedIcon(Resources resources, int iconId, String packageName, String componentName) {
        String hashkey = "ComponentInfo{" + packageName + "/" + componentName + "}";
        Drawable themedIcon;
        if (iconThemeMap.containsKey(hashkey)) {
            String drawableName = iconThemeMap.get(hashkey);
            themedIcon = ThemeUtils.getDrawable(mContext, drawableName);
            if (themedIcon != null) {
                return themedIcon;
            }
        }
        if (iconBackList != null && iconBackList.size() > 0) {
            Random r = new Random();
            int iconBackListIndex = r.nextInt(iconBackList.size());
            Drawable image = ThemeUtils.getDrawable(mContext,iconBackList.get(iconBackListIndex));
            Drawable icon = getFullResIcon(resources, iconId);
            if (image != null && icon != null) {
                return createIconDrawable(image, icon);
            }
        }
        return null;
    }

    private Drawable createIconDrawable(Drawable background, Drawable icon) {

        int iconScaleLevel = (int) ((float) scaleFactor * 10000);
        if ( iconFrontList != null && iconFrontList.size() > 0) {
            ScaleDrawable sd = new ScaleDrawable(icon, Gravity.CENTER, scaleFactor, scaleFactor);
            sd.setLevel(iconScaleLevel);
            Drawable[] layers = new Drawable[2 + iconFrontList.size()];
            layers[0] = background;
            layers[1] = (Drawable) sd;
            for (int i = 0; i < iconFrontList.size(); i++) {
                layers[2+i] = ThemeUtils.getDrawable(mContext, iconFrontList.get(i));
            }
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            return layerDrawable;
        } else {
            ScaleDrawable sd = new ScaleDrawable(icon, Gravity.CENTER, scaleFactor, scaleFactor);
            sd.setLevel(iconScaleLevel);
            Drawable[] layers = new Drawable[2];
            layers[0] = background;
            layers[1] = (Drawable) sd;
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            return layerDrawable;
        }
    }

    public Drawable getFullResIcon(String packageName, int iconId) {
        android.content.pm.ApplicationInfo info = null;
        try{
            info = mPackageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            info = null;
        }
        if (info != null) {
            return getFullResIcon(info, packageName, iconId);
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }

    public Drawable getFullResIcon(ResolveInfo info, String className) {
        return getFullResIcon(info.activityInfo, className);
    }

    public Drawable getFullResIcon(ActivityInfo info) {

        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(
                    info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                Drawable dr = getThemedIcon(resources, iconId, info.packageName, info.targetActivity);
                if (dr == null)
                    dr = getFullResIcon(resources, iconId);
                return dr;
            }
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ActivityInfo info, String className) {

        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(
                    info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                Drawable dr = getThemedIcon(resources, iconId, info.packageName, className);
                if (dr == null)
                    dr = getFullResIcon(resources, iconId);
                return dr;
            }
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(android.content.pm.ApplicationInfo info,
            String packageName, int iconId) {

        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(info);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            if (iconId != 0) {
                Drawable dr = getThemedIcon(resources, iconId, packageName, packageName);
                if (dr == null)
                    dr = getFullResIcon(resources, iconId);
                return dr;
            }
        }
        return getFullResDefaultActivityIcon();
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info,
            HashMap<Object, CharSequence> labelCache) {
        synchronized (mCache) {
            CacheEntry entry = cacheLocked(application.componentName, info, labelCache);

            application.title = entry.title;
            application.iconBitmap = entry.icon;
        }
    }

    public Bitmap getIcon(Intent intent) {
        synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
                return mDefaultIcon;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo, null);
            return entry.icon;
        }
    }

    public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo,
            HashMap<Object, CharSequence> labelCache) {
        synchronized (mCache) {
            if (resolveInfo == null || component == null) {
                return null;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo, labelCache);
            return entry.icon;
        }
    }

    public boolean isDefaultIcon(Bitmap icon) {
        return mDefaultIcon == icon;
    }

    private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info,
            HashMap<Object, CharSequence> labelCache) {
        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();

            mCache.put(componentName, entry);

            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(info);
            if (labelCache != null && labelCache.containsKey(key)) {
                entry.title = labelCache.get(key).toString();
            } else {
                entry.title = info.loadLabel(mPackageManager).toString();
                if (labelCache != null) {
                    labelCache.put(key, entry.title);
                }
            }
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }

            entry.icon = Utilities.createIconBitmap(
                    getFullResIcon(info, componentName.getClassName()), mContext);
        }
        return entry;
    }

    public HashMap<ComponentName, Bitmap> getAllIcons() {
        synchronized (mCache) {
            HashMap<ComponentName, Bitmap> set = new HashMap<ComponentName,Bitmap>();
            for (ComponentName cn : mCache.keySet()) {
                final CacheEntry e = mCache.get(cn);
                set.put(cn, e.icon);
            }
            return set;
        }
    }
}
