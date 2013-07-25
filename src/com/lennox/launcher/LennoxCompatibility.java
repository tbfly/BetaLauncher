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

package com.lennox.launcher;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
