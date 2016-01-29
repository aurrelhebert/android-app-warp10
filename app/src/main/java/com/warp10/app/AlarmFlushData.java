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
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * Created by ahebert on 1/13/16.
 */
public class AlarmFlushData extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        FileService.writeToFile("alarm", context);
        this.context = context;
        Handler h = new Handler();
        h.postDelayed(r, 1000);
    }

    Runnable r = new Runnable() {
        @Override
        public void run(){
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String url = sharedPrefs.getString("url", "NULL");
            String token = sharedPrefs.getString("token","NULL");
            FlushService.flushAllFiles("fill", context, url, token, false);
            FlushService.sendNotification(context, "End of manuel flush", "Flush ended");
        }
    };
}
