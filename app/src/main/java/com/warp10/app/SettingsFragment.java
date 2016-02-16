//
//   Copyright 2016  Cityzen Data
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package com.warp10.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

/**
 * Created by ahebert on 12/18/15.
 * Class necessary that extends preference Fragment by loading the preference file of this application
 */
//TODO Clean this class
public class SettingsFragment extends PreferenceFragment {

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
        Preference myPreference = myPreferenceScreen.findPreference("isActive");
        PreferenceScreen profileScreen = (PreferenceScreen) getPreferenceManager().findPreference("profile_screen_key");
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        profileScreen.setTitle(sharedPreferences.getString("profileName", "Profile settings"));
        Preference preference = profileScreen.findPreference("profileName");
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newVal) {
                final String value = (String) newVal;
                PreferenceScreen myPreferenceScreen = getPreferenceScreen();
                PreferenceScreen profileScreen = (PreferenceScreen) getPreferenceManager().findPreference("profile_screen_key");
                PreferenceCategory preferenceCategory =(PreferenceCategory) myPreferenceScreen.findPreference("profile_settings");
                preferenceCategory.removePreference(profileScreen);
                profileScreen.setTitle(value);
                preferenceCategory.addPreference(profileScreen);
                return true;
            }

        });
        myPreferenceScreen.removePreference(myPreference);
        myPreference = myPreferenceScreen.findPreference("checkedGTS");
        myPreferenceScreen.removePreference(myPreference);
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String syncConnPref = sharedPref.getString(KEY_PREF_SYNC_CONN, "");
    }
}
