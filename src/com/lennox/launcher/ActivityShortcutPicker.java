/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.graphics.ColorFilter;
import android.util.DisplayMetrics;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.lennox.launcher.R;

/**
 * Displays a list of all activities matching the incoming
 * {@link Intent#EXTRA_INTENT} query, along with any injected items.
 */
public class ActivityShortcutPicker extends ExpandableListActivity  {
    
    /**
     * Adapter of items that are displayed in this dialog.
     */
    private PickAdapter mAdapter;

    private List<String> mPackageNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Intent intent = getIntent();

        // Use custom title if provided, otherwise default window title
        if (intent.hasExtra(Intent.EXTRA_TITLE)) {
            setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));
        }

        // Build list adapter of pickable items
        HashMap<String, PickAdapter.Group> groups = getGroups();
        mAdapter = new PickAdapter(this, groups, mPackageNames);
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view,
        int groupPosition, int childPosition, long id) {
        Intent intent = getIntentForPosition(groupPosition, childPosition);
        setResult(Activity.RESULT_OK, intent);
        finish();
        return true;
    }

    /**
     * Build the specific {@link Intent} for a given list position. Convenience
     * method that calls through to {@link PickAdapter.Item#getIntent(Intent)}.
     */
    protected Intent getIntentForPosition(int groupPosition, int childPosition) {
        PickAdapter.Item item = (PickAdapter.Item) mAdapter.getChild(groupPosition, childPosition);
        return item.getIntent();
    }

    protected HashMap<String, PickAdapter.Group> getGroups() {
        final PackageManager packageManager = getPackageManager();
        HashMap<String, PickAdapter.Group> groups = new HashMap<String, PickAdapter.Group>();

        List<PackageInfo> list = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        Collections.sort(list, new Comparator<PackageInfo>() {
            public int compare(PackageInfo o1, PackageInfo o2) {
                String appName1 = (String) o1.applicationInfo.loadLabel(packageManager);
                String appName2 = (String) o2.applicationInfo.loadLabel(packageManager);
                return appName1.compareToIgnoreCase(appName2);
            }
        });

        /*for (int i = 0; i < list.size(); i++) {
            PackageInfo packageInfo = list.get(i);
            if (packageInfo.activities == null) continue;
            String packageName = packageInfo.packageName;

            Intent intent = new Intent();
            intent.setPackage(packageName);
            List<ResolveInfo> resolveList = packageManager.queryIntentActivities(intent,0);

            CharSequence applicationName = packageInfo.applicationInfo.loadLabel(packageManager);
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            mPackageNames.add(packageName);
            PickAdapter.Group group = new PickAdapter.Group(this, applicationName, packageName, icon);
            for (int j = 0; j < resolveList.size(); j++) {
                final ActivityInfo activityInfo = resolveList.get(j).activityInfo;
                if (activityInfo.enabled && activityInfo.exported) {
                    group.addItem(new PickAdapter.Item(this, packageManager, activityInfo));
                }
            }
            groups.put(packageName, group);
        }*/

        for (int i = 0; i < list.size(); i++) {
            PackageInfo packageInfo = list.get(i);
            if (packageInfo.activities == null) continue;
            String packageName = packageInfo.packageName;
            CharSequence applicationName = packageInfo.applicationInfo.loadLabel(packageManager);
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            PickAdapter.Group group = new PickAdapter.Group(this, applicationName, packageName, icon);
            for (int j = 0; j < packageInfo.activities.length; j++) {
                final ActivityInfo activityInfo = packageInfo.activities[j];
                if (activityInfo.enabled && activityInfo.exported) {
                    group.addItem(new PickAdapter.Item(this, packageManager, activityInfo));
                }
            }
            if (group.getSize() > 0) {
                mPackageNames.add(packageName);
                groups.put(packageName, group);
            }
        }
        return groups;
    }

    /**
     * Adapter which shows the set of activities that can be performed for a
     * given {@link Intent}.
     */
    protected static class PickAdapter extends BaseExpandableListAdapter {
        
        /**
         * Item that appears in a {@link PickAdapter} list.
         */
        public static class Item {

            CharSequence label;
            Drawable icon;
            String packageName;
            String className;
            Bundle extras;

            Item(Context context, PackageManager pm, ActivityInfo activityInfo) {
                label = activityInfo.loadLabel(pm);
                if (label == null && activityInfo != null) {
                    label = activityInfo.name;
                }
                if (label == "") {
                    label = activityInfo.applicationInfo.loadLabel(pm);
                }

                icon = activityInfo.loadIcon(pm);
                packageName = activityInfo.applicationInfo.packageName;
                className = activityInfo.name;
            }

            /**
             * Build the {@link Intent} described by this item. If this item
             * can't create a valid {@link android.content.ComponentName}, it will return
             * {@link Intent#ACTION_CREATE_SHORTCUT} filled with the item label.
             */
            Intent getIntent() {
                Intent intent = new Intent();
                if (packageName != null && className != null) {
                    // Valid package and class, so fill details as normal intent
                    intent.setClassName(packageName, className);
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                }
                return intent;
            }
        }

        public static class Group {

            CharSequence label;
            Drawable icon;
            String packageName;
            List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();

            Group(Context context, CharSequence label, String packageName, Drawable icon) {
                this.label = label;
                this.packageName = packageName;
                this.icon = icon;
            }

            void addItem(PickAdapter.Item item) {
                items.add(item);
            }

            int getSize() {
                return items.size();
            }
        }
        
        private final LayoutInflater mInflater;
        private final HashMap<String, PickAdapter.Group> mGroups;
        private final List<String> mPackageNames;

        public PickAdapter(Context context, HashMap<String, PickAdapter.Group> groups, List<String> packageNames) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mGroups = groups;
            mPackageNames = packageNames;
        }

        @Override
            public Object getChild(int groupPosition, int childPosition) {
            List<PickAdapter.Item> items = mGroups.get(mPackageNames.get(groupPosition)).items;
            return items.get(childPosition);
        }

        @Override
            public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }
 
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
               View view, ViewGroup parent) {

            if (view == null) {
                view = mInflater.inflate(R.layout.pick_activity, null);
            }
   
            Item item = (Item) getChild(groupPosition, childPosition);
            TextView title = (TextView) view.findViewById(R.id.activity_picker_title);
            TextView summary = (TextView) view.findViewById(R.id.activity_picker_summary);
            ImageView icon = (ImageView) view.findViewById(R.id.activity_picker_icon);

            title.setText(item.label);
            summary.setText(item.className);
            icon.setImageDrawable(item.icon);
            
            return view;
        }
 
        @Override
        public int getChildrenCount(int groupPosition) {
            List<PickAdapter.Item> items = mGroups.get(mPackageNames.get(groupPosition)).items;
            return items.size();
        }
 
        @Override
        public int getGroupCount() {
            return mGroups.size();
        }
 
        @Override
        public Object getGroup(int groupPosition) {
            return mGroups.get(mPackageNames.get(groupPosition));
        }
 
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
 
        @Override
        public View getGroupView(int groupPosition, boolean isLastChild, View view,
            ViewGroup parent) {

            if (view == null) {
                view = mInflater.inflate(R.layout.pick_activity, null);
            }
   
            Group group = (Group) getGroup(groupPosition);
            TextView title = (TextView) view.findViewById(R.id.activity_picker_title);
            TextView summary = (TextView) view.findViewById(R.id.activity_picker_summary);
            ImageView icon = (ImageView) view.findViewById(R.id.activity_picker_icon);

            title.setText(group.label);
            summary.setText(group.packageName);
            icon.setImageDrawable(group.icon);
            
            return view;
        }
 
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
  
    }
}
