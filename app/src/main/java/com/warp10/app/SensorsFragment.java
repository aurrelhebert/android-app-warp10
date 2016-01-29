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
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Created by ahebert on 1/8/16.
 * Class necessary that extends preference Fragment by loading the preference file of this application
 * Class loadind all details related to the sensors description
 */
public class SensorsFragment extends PreferenceFragment {

    /**
     * All the sharedPreferences not related to sensors description
     */
    public static List<String> preferencesList = Arrays.asList("checkedGTS", "token", "url", "isActive", "useInternet",
            "prefix", "flush", "keepValues", "limitSizeDisk");

    /**
     * Method on Create
     * Load a new preference Screen with all the items that are in file sensors.xml
     * Update the one modified by the user on sharedPreferences
     * Add the one added by the user on sharePreferences
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(com.warp10.app.R.xml.sensors);

        List<String> lString = preferencesList;
        PreferenceScreen myPreferenceScreen = getPreferenceScreen();

        /**
         * Get sharedPreferences
         */
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        Map<String,?> map = sharedPreferences.getAll();

        //Log.d("ALLPREF", lString.toString());
        /**
         * For all sharedPreferences
         */
        for (String pref:map.keySet()) {

            /**
             * If the preferences is related to the sensor description
             */
            if(!lString.contains(pref))
            {
                //Log.d("PREF", pref.toString());
                /**
                 * Get the preference
                 */
                SensorPreference myPref = (SensorPreference) getPreferenceManager().findPreference(pref);
                /**
                 * If the preference doesn't exist (a preference added manually by the user
                 */
                if(myPref == null ) {
                    /**
                     * Add it on the preference screen by creating a new sensorPreference
                     */
                    myPref = new SensorPreference(myPreferenceScreen.getContext(), null,
                            sharedPreferences.getString(pref,"NULL"), pref);
                    //Log.d("PREF + VALUE", myPref.toString() + " " + sharedPreferences.getString(pref, "NULL"));
                    myPreferenceScreen.addPreference(myPref);
                } else {
                    /**
                     * Otherwise update it with user modification
                     */
                    String sensorDescription = sharedPreferences.getString(pref,"NULL [NULL]");
                    myPref.setTitle(SensorPreference.parseValue(sensorDescription).first);
                }
            }
        }

        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String syncConnPref = sharedPref.getString(KEY_PREF_SYNC_CONN, "");
    }
}
