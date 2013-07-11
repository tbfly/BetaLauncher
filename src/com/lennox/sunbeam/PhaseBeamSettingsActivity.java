package com.lennox.sunbeam;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.os.Bundle;

import com.android.launcher.R;
import com.android.launcher2.preference.PreferencesProvider;

public class PhaseBeamSettingsActivity extends PreferenceActivity
implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(
            PreferencesProvider.PREFERENCES_KEY);
        addPreferencesFromResource(R.xml.preferences_lwp);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
            this);

        // Load all preferences
        PreferencesProvider.load(this);

        initColourPreference("livewallpaper_beam_colour",
            PreferencesProvider.Interface.LiveWallpaper.getBeamColour());
        initColourPreference("livewallpaper_dot_colour",
            PreferencesProvider.Interface.LiveWallpaper.getDotColour());
        initColourPreference("livewallpaper_background_colour",
            PreferencesProvider.Interface.LiveWallpaper.getBackgroundColour());
    }

    private void initColourPreference(String key, int index) {
        ListPreference pref;
        pref = (ListPreference) getPreferenceScreen().findPreference(key);
        pref.setSummary(pref.getEntries()[index]);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
    }
}
