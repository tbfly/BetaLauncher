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

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.android.launcher.R;
import com.android.launcher2.LauncherSettings.Favorites;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class LennoxCompatibility {

    public static int getResourceId ( Context context, String name ) {
    	String[] seperated = name.split(":");
    	String[] seperated2 = seperated[1].split("/");
    	String resourcePackage = seperated[0];
    	
        if ( resourcePackage.equalsIgnoreCase("custom")) {
        	resourcePackage = "xtreamer";
        }

    	String resourceType = seperated2[0];
    	String resourceName = seperated2[1];
    	return context.getResources().getIdentifier(resourceName, resourceType, resourcePackage);
    }

    public static boolean bindAppWidgetIdIfAllowed(AppWidgetManager appWidgetManager,
                            int appWidgetId, ComponentName componentName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,componentName);
        } else {
            try {
                Method m=AppWidgetManager.class.getDeclaredMethod("bindAppWidgetId", Integer.TYPE, ComponentName.class);
                //m.setAccessible(true);
                m.invoke(appWidgetManager,appWidgetId,componentName);
                return true;
            //appWidgetManager.bindAppWidgetId(appWidgetId,componentName);
            //} catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                //    e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
    }
}
