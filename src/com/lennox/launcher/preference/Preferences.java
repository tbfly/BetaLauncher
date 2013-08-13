/*
 * Copyright (C) 2011 The CyanogenMod Project
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

package com.lennox.launcher.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lennox.launcher.LauncherProvider;
import com.lennox.launcher.LauncherApplication;
import com.lennox.launcher.R;
import com.lennox.utils.ThemeUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class Preferences extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Launcher.Preferences";

    private SharedPreferences mPreferences;
    private static List<Header> mHeaders = new ArrayList<Header>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PreferencesProvider.PREFERENCES_VISITED, true);
        editor.commit();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
        mHeaders = target;
        updateHeaders();
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.id == R.id.preferences_backup_section) {
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (header.id == R.id.preferences_application_version) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=lennykano%40gmail%2ecom&lc=AU&item_name=Lennox%20Corporation&item_number=LENNOX_LAUNCHER&currency_code=AUD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
            startActivity(browserIntent);
        }
    }

    private void updateHeaders() {
        int i = 0;
        List<Header> newHeaders = new ArrayList<Header>();
        while (i < mHeaders.size()) {
            Header header = mHeaders.get(i);
            newHeaders.add(header);

            // Version preference
            if (header.id == R.id.preferences_application_version) {
                header.title = getString(R.string.application_name) + " " + getString(R.string.application_version);
            }

            if (header.id == R.id.preferences_application_version ||
                        header.id == R.id.preferences_application_section) {
                String lennoxDevice = "";
                try {
                    Class clazz = Class.forName("android.os.SystemProperties");
                    Method method = clazz.getDeclaredMethod("get", String.class);
                    lennoxDevice = (String) method.invoke(null, "ro.lennox.device");
                    if (!lennoxDevice.equals("")) {
                        newHeaders.remove(header);
                    }
                } catch (ClassNotFoundException e) {
                    // Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (header.id == R.id.preferences_backup_section) {
                try {
                    getPackageManager().getPackageInfo("com.lennox.backup", PackageManager.GET_ACTIVITIES);
                } catch (PackageManager.NameNotFoundException e) {
                    newHeaders.remove(header);
                }
            }

            // Increment if not removed
            if (mHeaders.get(i) == header) {
                i++;
            }
        }
        mHeaders = newHeaders;
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, mHeaders));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
        editor.commit();
    }

    public static class HomescreenFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_homescreen);

        }
    }

    public static class FolderFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_folder);

            ImageListPreference pref = (ImageListPreference) getPreferenceScreen().findPreference("lennox_homescreen_folder_style");

            int[] images = new int[]{R.drawable.folder_icon_style_grid, R.drawable.folder_icon_style_stacked,
                                          R.drawable.folder_icon_style_carousel, R.drawable.folder_icon_style_fan,
                                          R.drawable.folder_icon_style_ios};
            Drawable[] previewDrawables = new Drawable[images.length];

            for (int i = 0; i < images.length; i++) {
                previewDrawables[i] = getActivity().getResources().getDrawable(images[i]);
            }

            pref.setEntryIcons(previewDrawables);

        }
    }

    public static class DrawerFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_drawer);
        }
    }

    public static class DockFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        private static DoubleNumberPickerPreference mDefaultPages;
        private static DoubleNumberPickerPreference mNumberPages;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_dock);

            PreferenceScreen prefSet = getPreferenceScreen();

            mDefaultPages = (DoubleNumberPickerPreference)prefSet.findPreference("lennox_dock_default_page");
            mNumberPages = (DoubleNumberPickerPreference)prefSet.findPreference("lennox_dock_pages");
            mNumberPages.setOnPreferenceChangeListener(this);
            mDefaultPages.setMax1(PreferencesProvider.Interface.Dock.getNumberPages(false));
            mDefaultPages.setMax2(PreferencesProvider.Interface.Dock.getNumberPages(true));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ( preference instanceof DoubleNumberPickerPreference && preference.getKey().equals("lennox_dock_pages") ) {
                int portMax, landMax;
                String[] values = ((String) newValue).split("\\|");
                try {
                    portMax = Integer.parseInt(values[0]);
                    landMax = Integer.parseInt(values[1]);
                } catch (NumberFormatException e) {
                    portMax = 1;
                    landMax = 1;
                }
                mDefaultPages.setMax1(portMax);
                mDefaultPages.setMax2(landMax);
                return true;
            }
            return false;
        }

    }

    public static class GeneralFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_general);
        }
    }

    public static class ThemeFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        private ImageListPreference mIconTheme;
        private ColorPickerPreference mColorTheme;
        private LauncherApplication context;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_theme);

            PreferenceScreen prefSet = getPreferenceScreen();

            mIconTheme = (ImageListPreference) prefSet.findPreference("icon_theme");
            mIconTheme.setOnPreferenceChangeListener(this);
            mColorTheme = (ColorPickerPreference) prefSet.findPreference("theme_color");
            mColorTheme.setOnPreferenceChangeListener(this);

            context = (LauncherApplication) (getActivity().getApplicationContext());

            if (context != null) {
                List<ThemeUtils.Theme> themeList = context.mThemeUtils.getThemePackageList();
                String[] packageList = new String[themeList.size()];
                String[] labelList = new String[themeList.size()];
                Drawable[] iconList = new Drawable[themeList.size()];
                for (int i = 0; i < themeList.size(); i++) {
                    ThemeUtils.Theme theme = themeList.get(i);
                    packageList[i] = theme.packageName;
                    labelList[i] = theme.label;
                    iconList[i] = theme.icon;
                }
                mIconTheme.setEntryValues(packageList);
                mIconTheme.setEntryIcons(iconList);
                mIconTheme.setEntries(labelList);
                String currentPackage = PreferencesProvider.Interface.Theme.getThemePackageName();
                String currentThemeName = context.mThemeUtils.getThemeName(currentPackage);
                mIconTheme.setSummary(currentThemeName);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ( preference instanceof ImageListPreference && preference.getKey().equals("icon_theme")) {
                mIconTheme.setSummary(mIconTheme.getEntries()[mIconTheme.findIndexOfValue((String) newValue)]);
                context.mThemeUtils.clearCache();
                return true;
            /*try {
                android.os.Debug.dumpHprofData("/sdcard/hprof");
            } catch (IOException e) {
                // TODO Auto-generated catch block
               Log.e(TAG, "Error backuping up database: " + e.getMessage(), e);
            }*/
            } else if ( preference instanceof ColorPickerPreference && preference.getKey().equals("theme_color")) {
                //mColorTheme.onColorChanged(PreferencesProvider.Interface.Theme.getThemeColor());
                return true;
            }
            return false;
        }

    }

    public static class GesturesFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        private ListPreference mHomescreenDoubleTap;
        private ListPreference mHomescreenSwipeUp;
        private ListPreference mHomescreenSwipeDown;
        private ListPreference mHomescreenPinch;
        private ListPreference mHomescreenSpread;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_gestures);

            PreferenceScreen prefSet = getPreferenceScreen();

            mHomescreenDoubleTap = (ListPreference) prefSet.findPreference("lennox_homescreen_double_tap_gesture");
            mHomescreenDoubleTap.setOnPreferenceChangeListener(this);
            initGesturePreference(mHomescreenDoubleTap);
            mHomescreenDoubleTap.setSummary(mHomescreenDoubleTap.getEntry());
            mHomescreenSwipeDown = (ListPreference) prefSet.findPreference("lennox_homescreen_down_gesture");
            mHomescreenSwipeDown.setOnPreferenceChangeListener(this);
            initGesturePreference(mHomescreenSwipeDown);
            mHomescreenSwipeDown.setSummary(mHomescreenSwipeDown.getEntry());
            mHomescreenSwipeUp = (ListPreference) prefSet.findPreference("lennox_homescreen_up_gesture");
            mHomescreenSwipeUp.setOnPreferenceChangeListener(this);
            initGesturePreference(mHomescreenSwipeUp);
            mHomescreenSwipeUp.setSummary(mHomescreenSwipeUp.getEntry());
            mHomescreenSpread = (ListPreference) prefSet.findPreference("lennox_homescreen_spread_gesture");
            mHomescreenSpread.setOnPreferenceChangeListener(this);
            initGesturePreference(mHomescreenSpread);
            mHomescreenSpread.setSummary(mHomescreenSpread.getEntry());
            mHomescreenPinch = (ListPreference) prefSet.findPreference("lennox_homescreen_pinch_gesture");
            mHomescreenPinch.setOnPreferenceChangeListener(this);
            initGesturePreference(mHomescreenPinch);
            mHomescreenPinch.setSummary(mHomescreenPinch.getEntry());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ( preference instanceof ListPreference ) {
                ListPreference gesturePref = (ListPreference)preference;
                gesturePref.setSummary(gesturePref.getEntries()[gesturePref.findIndexOfValue((String) newValue)]);
                return true;
            }
            return false;
        }

        private void initGesturePreference(ListPreference preference) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                preference.setEntries(getResources().getStringArray(R.array.gesture_target_names_pre42));
                preference.setEntryValues(getResources().getStringArray(R.array.gesture_target_values_pre42));
            }
        }
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        private static final int HEADER_TYPE_NORMAL = 0;
        private static final int HEADER_TYPE_CATEGORY = 1;

        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_CATEGORY + 1;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.id == R.id.preferences_application_section) {
                return HEADER_TYPE_CATEGORY;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);

            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;

                case HEADER_TYPE_NORMAL:
                    holder.icon.setImageResource(header.iconRes);
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                    break;
            }

            return view;
        }
    }
}
