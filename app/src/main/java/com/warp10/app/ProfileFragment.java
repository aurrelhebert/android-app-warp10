package com.warp10.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import java.util.Map;

/**
 * Created by ahebert on 2/12/16.
 * Profile Fragment loading the Load Profile Menu
 */
public class ProfileFragment extends PreferenceFragment {

    protected static String NAME_SHARED_FILE_PROFILE = "profile";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.profile);
        // Define the settings file to use by this settings fragment
        getPreferenceManager().setSharedPreferencesName(NAME_SHARED_FILE_PROFILE);

        PreferenceScreen preferenceScreen = (PreferenceScreen)
                getPreferenceManager().findPreference("profilePrefScreen");

        /**
         * Get sharedPreferences
         */
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        Map<String,?> map = sharedPreferences.getAll();

        for (String pref:map.keySet()) {

            //Log.d("PREF", pref.toString());
            /**
             * Get the preference
             */
            LoadProfile myPref = (LoadProfile) getPreferenceManager().findPreference(pref);
            /**
             * If the preference doesn't exist (a preference added manually by the user
             */
            if(myPref == null ) {
                /**
                 * Add it on the preference screen by creating a new sensorPreference
                 */
                if(!sharedPreferences.getString(pref ,"NULL").equals("currentKey")) {
                    myPref = new LoadProfile(preferenceScreen.getContext(), null,
                            sharedPreferences.getString(pref ,"NULL"), pref);
                    //Log.d("PREF + VALUE", myPref.toString() + " " + sharedPreferences.getString(pref, "NULL"));
                    preferenceScreen.addPreference(myPref);
                }
            } else {
                /**
                 * Otherwise update it with user modification
                 */
                String desc = sharedPreferences.getString(pref,"NULL");
                if("NULL"!=desc) {
                    myPref.setTitle(LoadProfile.getName(desc));
                }
            }
        }
        Preference myPreference = preferenceScreen.findPreference("currentKey");
        preferenceScreen.removePreference(myPreference);
    }
}
