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

package com.android.launcher2.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.launcher2.LauncherApplication;
import com.android.launcher.R;
import com.lennox.utils.ThemeUtils;

import java.util.List;

public class Preferences extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Launcher.Preferences";

    private SharedPreferences mPreferences;
    private static Context mContext;
    private List<Header> mHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        mContext = getApplicationContext();
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
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this); 
        mContext = null;
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
        mHeaders = target;
        updateHeaders();
    }

    private void updateHeaders() {
        int i = 0;
        while (i < mHeaders.size()) {
            Header header = mHeaders.get(i);

            // Version preference
            if (header.id == R.id.preferences_application_version) {
                header.title = getString(R.string.application_name) + " " + getString(R.string.application_version);
            }

            // Increment if not removed
            if (mHeaders.get(i) == header) {
                i++;
            }
        }
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

            PreferenceCategory general = (PreferenceCategory)findPreference("ui_homescreen_general");
            if (general != null && LauncherApplication.isScreenLarge()) {
                boolean workspaceTabletGrid = getResources().getBoolean(R.bool.config_workspaceTabletGrid);
                if (workspaceTabletGrid == false) {
                    Preference grid = findPreference("ui_homescreen_grid");
                    if (grid != null) {
                        general.removePreference(grid);
                    }
                    Preference stretch = findPreference("ui_homescreen_stretch_screens");
                    if (stretch != null) {
                        general.removePreference(stretch);
                    }
                }
            }
        }
    }

    public static class DrawerFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_drawer);
        }
    }

    public static class DockFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_dock);
        }
    }

    public static class GeneralFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_general);
        }

        @Override
        public void onResume() {
            super.onResume();
            String[] packageList = ThemeUtils.createThemePackageList(mContext);
            ListPreference pref = (ListPreference)getPreferenceScreen().findPreference("icon_theme");
            pref.setEntryValues(packageList);
            pref.setEntries(ThemeUtils.createThemeNameList(mContext, packageList));
            String currentPackage = ThemeUtils.getThemePackageName(mContext, ThemeUtils.DEFAULT); 
            String currentThemeName = ThemeUtils.getThemeName(mContext, currentPackage); 
            pref.setSummary(currentThemeName);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String key = preference.getKey();
            if (key.equals("icon_theme")) {
                ListPreference pref = (ListPreference)preference;
                ThemeUtils.setThemePackageName(mContext, pref.getValue());
                pref.setSummary(pref.getEntry());
                return true;
            }
            return false;
        }

    }

    public static class GesturesFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_gestures);

            initGesturePreference("ui_homescreen_up_gesture",
                PreferencesProvider.Interface.Homescreen.Gestures.getUpGestureAction());
            initGesturePreference("ui_homescreen_down_gesture",
                PreferencesProvider.Interface.Homescreen.Gestures.getDownGestureAction());
            initGesturePreference("ui_homescreen_pinch_gesture",
                PreferencesProvider.Interface.Homescreen.Gestures.getPinchGestureAction());
            initGesturePreference("ui_homescreen_spread_gesture",
                PreferencesProvider.Interface.Homescreen.Gestures.getSpreadGestureAction());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String key = preference.getKey();
            if (key.equals("ui_homescreen_up_gesture") || key.equals("ui_homescreen_down_gesture") ||
                    key.equals("ui_homescreen_pinch_gesture") || key.equals("ui_homescreen_spread_gesture")) {
                ListPreference gesturePref = (ListPreference)preference;
                gesturePref.setSummary(gesturePref.getEntries()[gesturePref.findIndexOfValue((String)o)]);
                return true;
            }
            return false;
        }

        private void initGesturePreference(String key, String action) {
            ListPreference pref;
            pref = (ListPreference)getPreferenceScreen().findPreference(key);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                pref.setEntries(getResources().getStringArray(R.array.gesture_target_names_pre42));
                pref.setEntryValues(getResources().getStringArray(R.array.gesture_target_values_pre42));
            }
            pref.setSummary(pref.getEntries()[pref.findIndexOfValue(action)]);
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
