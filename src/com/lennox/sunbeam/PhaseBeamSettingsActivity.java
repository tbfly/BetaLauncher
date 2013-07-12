package com.lennox.sunbeam;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.os.Bundle;

import com.android.launcher.R;

public class PhaseBeamSettingsActivity extends PreferenceActivity
implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(PhaseBeamWallpaper.LWP_PREFS);
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
