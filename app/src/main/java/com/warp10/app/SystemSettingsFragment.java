package com.warp10.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

/**
 * Created by ahebert on 2/17/16.
 */
public class SystemSettingsFragment extends PreferenceFragment {

    /**
     * On create add all items registered in preferences.xml
     * Doesn't print on screen isActive nor checkedGTD
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(com.warp10.app.R.xml.preferences);
        PreferenceScreen myPreferenceScreen = getPreferenceScreen();

        // Remove Profile category of the preference screen
        Preference cat = myPreferenceScreen.findPreference("profile_settings");
        myPreferenceScreen.removePreference(cat);

        // Remove isActive and checked GTS of the preference screen
        PreferenceCategory preferenceCategory = (PreferenceCategory)
                myPreferenceScreen.findPreference("system_settings");
        Preference myPreference = preferenceCategory.findPreference("isActive");
        preferenceCategory.removePreference(myPreference);
        myPreference = preferenceCategory.findPreference("checkedGTS");
        preferenceCategory.removePreference(myPreference);
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String syncConnPref = sharedPref.getString(KEY_PREF_SYNC_CONN, "");
    }
}
