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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ahebert on 12/18/15.
 * Class used to create an alarm Listener used to push data on Warp
 * Delay execution time is set up on creation
 */
public class AlarmReceiver extends BroadcastReceiver
{

    private static final String EXTRA_URL = "com.cityzendata.warpapp.extra.URL";
    private static final String EXTRA_TOKEN = "com.cityzendata.warpapp.extra.TOKEN";
    private static final String EXTRA_STOP = "com.cityzendata.warpapp.extra.STOP";

    /**
     * Method flushing all data stored in application every time the alarm is triggered
     * @param context main application context
     * @param intent Intent containing application parameters as warp url and the application write token
     */
    public void onReceive(Context context, Intent intent)
    {
        String warpUrl = intent.getStringExtra(EXTRA_URL);
        String warpToken = intent.getStringExtra(EXTRA_TOKEN);
        Boolean stop = intent.getBooleanExtra(EXTRA_STOP, true);
        FlushService.flushAllFiles("fill", context, warpUrl, warpToken, stop);
        Log.d("ALARM", "STOP: " + stop);
        /**
         * This case is executed when it's the last push of the application (Stop button was pressed by the user
         */
        if(stop)
        {
            // When last alarm post exec, put collect active to false
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            SharedPreferences.Editor ed = sharedPrefs.edit();
            ed.putBoolean("isActive", false);
            ed.commit();
            if(!CollectService.isPostActive) {
                //TODO clean
                //FileService.setContext(context);
            }
        }
    }
}
