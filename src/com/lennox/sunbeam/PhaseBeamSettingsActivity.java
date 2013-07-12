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
        getPreferenceManager().setSharedPreferencesName(PreferencesProvider.PREFERENCES_KEY);
        addPreferencesFromResource(R.xml.preferences_lwp);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        initColourPreference("livewallpaper_beam_colour");
        initColourPreference("livewallpaper_dot_colour");
        initColourPreference("livewallpaper_background_colour");
    }

    private void initColourPreference(String key) {
        ListPreference pref;
        pref = (ListPreference) getPreferenceScreen().findPreference(key);
        pref.setSummary(pref.getEntries()[Integer.parseInt(getPreferenceManager()
                        .getSharedPreferences().getString(key, "0"))]);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        ListPreference pref;
        pref = (ListPreference) getPreferenceScreen().findPreference(key);
        pref.setSummary(pref.getEntry());
    }
}
