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

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

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
        if (!key.equals("restore_workspace") && !key.equals("backup_workspace")) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
            editor.commit();
        }
    }

    public static class HomescreenFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_homescreen);

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

    public static class GeneralFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        private ListPreference mIconTheme;
        private LauncherApplication context;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_general);

            PreferenceScreen prefSet = getPreferenceScreen();

            mIconTheme = (ListPreference) prefSet.findPreference("icon_theme");
            mIconTheme.setOnPreferenceChangeListener(this);

            context = (LauncherApplication) (getActivity().getApplicationContext());

            if (context != null) {
                String[] packageList = context.mThemeUtils.createThemePackageList();
                mIconTheme.setEntryValues(packageList);
                mIconTheme.setEntries(context.mThemeUtils.createThemeNameList(packageList));
                String currentPackage = PreferencesProvider.Interface.General.getThemePackageName();
                String currentThemeName = context.mThemeUtils.getThemeName(currentPackage);
                mIconTheme.setSummary(currentThemeName);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ( preference instanceof ListPreference && preference.getKey().equals("icon_theme")) {
                mIconTheme.setSummary(mIconTheme.getEntries()[mIconTheme.findIndexOfValue((String) newValue)]);
                context.mThemeUtils.clearCache();
            /*try {
                android.os.Debug.dumpHprofData("/sdcard/hprof");
            } catch (IOException e) {
                // TODO Auto-generated catch block
               Log.e(TAG, "Error backuping up database: " + e.getMessage(), e);
            }*/
                return true;
            }
            return false;
        }

    }

    public static class BackupFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener{
        private Preference mBackup;
        private ListPreference mRestore;
        private LauncherApplication context;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_backup);

            context = (LauncherApplication) (getActivity().getApplicationContext());

            PreferenceScreen prefSet = getPreferenceScreen();

            mBackup = prefSet.findPreference("backup_workspace");
            mBackup.setOnPreferenceChangeListener(this);
            mRestore = (ListPreference) prefSet.findPreference("restore_workspace");
            mRestore.setOnPreferenceChangeListener(this);

            getBackupList();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (key.equals("restore_workspace")) {
                final String restoreDir = (String) newValue;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.preferences_restore_workspace_confirm_dialog)
                .setTitle(String.format(context.getString(R.string.preferences_restore_workspace_dialog_title),restoreDir))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        backupDatabase(true, false, restoreDir);
                        backupDatabase(true, true, restoreDir);
                        backupPreference(true, restoreDir);
                        Toast.makeText(context, context.getString(R.string.preferences_restore_workspace_complete), Toast.LENGTH_LONG).show();
                        context.mThemeUtils.clearCache();
                        LauncherApplication.setPerformedRestore(true);
                        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
                builder.show();
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
            if (preference == mBackup) {
                final String backupDir = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.backup_confirm, null);
                final EditText backupName = (EditText) layout.findViewById(R.id.backup_title);
                backupName.setText(backupDir);
                builder.setView(layout)
                .setTitle(R.string.preferences_backup_workspace_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String chosenBackupDir = backupName.getText().toString();
                        backupDatabase(false, false, chosenBackupDir);
                        backupDatabase(false, true, chosenBackupDir);
                        backupPreference(false, chosenBackupDir);
                        Toast.makeText(context, String.format(context.getString(R.string.preferences_backup_workspace_complete),getBackupDatabaseFile(chosenBackupDir, false).getParent()), Toast.LENGTH_LONG).show();
                        getBackupList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
            return true;
        }

        public void getBackupList() {
            File file = new File( Environment.getExternalStorageDirectory().getAbsolutePath()
                          + "/LennoxLauncher" );
            File[] list = null;
            if (file.exists()) {
                list = file.listFiles();
            }
            if (list != null && list.length > 0) {
                String[] preferenceList = new String[list.length];   
                for( int i=0; i< list.length; i++) {
                    preferenceList[i] = list[i].getName();
                }
                mRestore.setEntryValues(preferenceList);
                mRestore.setEntries(preferenceList);
                mRestore.setEnabled(true);
            } else {
                mRestore.setEnabled(false);
            }

        }

        public File getCurrentDatabaseFile(boolean landscape) {
            return new File(Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/", landscape ? LauncherProvider.DATABASE_LANDSCAPE_NAME : LauncherProvider.DATABASE_NAME);
        }

        public File getCurrentPreferenceFile() {
            return new File(Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/shared_prefs/", PreferencesProvider.PREFERENCES_KEY + ".xml");
        }

        public File getBackupDatabaseFile(String dirname, boolean landscape) {
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(baseDir + "/LennoxLauncher/" + dirname);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return new File(dir, landscape ? LauncherProvider.DATABASE_LANDSCAPE_NAME : LauncherProvider.DATABASE_NAME);
        }

        public File getBackupPreferenceFile(String dirname) {
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(baseDir + "/LennoxLauncher/" + dirname);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return new File(dir, PreferencesProvider.PREFERENCES_KEY + ".xml");
        }

        public final boolean backupDatabase(boolean restore, boolean landscape, String dirname) {
            File from = restore ? getBackupDatabaseFile(dirname, landscape) : getCurrentDatabaseFile(landscape);
            File to = restore ? getCurrentDatabaseFile(landscape) : getBackupDatabaseFile(dirname, landscape);
            try {
                copyFile(from, to);
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
               Log.e(TAG, "Error backuping up database: " + e.getMessage(), e);
            }
            return false;
        }

        public final boolean backupPreference(boolean restore, String dirname) {
            File from = restore ? getBackupPreferenceFile(dirname) : getCurrentPreferenceFile();
            File to = restore ? getCurrentPreferenceFile() : getBackupPreferenceFile(dirname);
            try {
                copyFile(from, to);
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
               Log.e(TAG, "Error backuping up database: " + e.getMessage(), e);
            }
            return false;
        }

        public static void copyFile(File src, File dst) throws IOException {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dst);
            FileChannel fromChannel = null, toChannel = null;
            try {
                fromChannel = in.getChannel();
                toChannel = out.getChannel();
                fromChannel.transferTo(0, fromChannel.size(), toChannel); 
            } finally {
                if (fromChannel != null) 
                    fromChannel.close();
                if (toChannel != null) 
                    toChannel.close();
            }
            in.close();
            out.close();
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
