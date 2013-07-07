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
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Preferences extends PreferenceActivity implements
        OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Launcher.Preferences";

    private Context mContext;
    private SharedPreferences mPreferences;
    private ListPreference mIconTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mPreferences = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        addPreferencesFromResource(R.xml.preferences_launcher);
        mIconTheme = (ListPreference) findPreference("icon_theme");
        mIconTheme.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] packageList = ThemeUtils.createThemePackageList(mContext);
        mIconTheme.setEntryValues(packageList);
        mIconTheme.setEntries(ThemeUtils.createThemeNameList(mContext, packageList));
        String currentPackage = ThemeUtils.getThemePackageName(mContext, ThemeUtils.DEFAULT); 
        String currentThemeName = ThemeUtils.getThemeName(mContext, currentPackage); 
        mIconTheme.setSummary(currentThemeName);

        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
        if (key.equals("icon_theme")) {
            ThemeUtils.setThemePackageName(mContext, mIconTheme.getValue());
            mIconTheme.setSummary(mIconTheme.getEntry());
        }
        editor.commit();
    }

}
