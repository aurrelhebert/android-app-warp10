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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Service of the Application
 * Is the one starting Sensors services and/or Location service
 */
public class CollectService extends Service {

    /**
     * Current service is running ?
     */
    public static volatile boolean isRunning = false;

    /**
     * Intent parameter
     */
    private static final String ACTION_START = "com.cityzendata.warpapp.action.STARTCOLLECTSERVICE";
    private static final String EXTRA_URL = "com.cityzendata.warpapp.extra.URL";
    private static final String EXTRA_TOKEN = "com.cityzendata.warpapp.extra.TOKEN";
    private static final String EXTRA_SENSORS = "com.cityzendata.warpapp.extra.SENSORSCL";
    private static final String EXTRA_PREFIX = "com.cityzendata.warpapp.extra.PREFIXCL";
    private static final String EXTRA_STOP = "com.cityzendata.warpapp.extra.STOP";

    /**
     * Url to push data
     */
    protected String url;

    /**
     * Url to push data
     */
    protected String socketUrl;


    /**
     * Authenticate write token
     */
    protected String token;

    /**
     * Prefix of the current GTS set by the user
     */
    protected String prefixGts;

    /**
     * Time between 2 flush of the application
     */
    protected int flushTime;

    /**
     * Location service
     */
    protected LocationService locationService;

    /**
     * Intent service for sensors
     */
    protected SensorService sensorService;

    /**
     * Application context
     */
    protected Context context;

    /**
     * Socket
     */
    protected static WebSocketDataListener ws;

    /**
     * boolean to tell whether Collect with Post/Websocket is Active
     */
    protected static boolean isPostActive;

    protected static boolean isClosed;
    //protected static WebSocketJetty webSocket;

    /**
     * Empty constructor
     */
    public CollectService() {
    }

    /**
     * Set the parameter context
     * @param context new context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    @Deprecated
    public void startCollectService(Context context, ArrayList<CharSequence> sensorList, String prefixGts) {
        Intent intent = new Intent(context, CollectService.class);
        intent.putExtra(EXTRA_SENSORS, sensorList);
        intent.putExtra(EXTRA_PREFIX, prefixGts);
        context.startService(intent);
    }

    /**
     * Command to start Service
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        //final ArrayList<CharSequence> sensorNameList = intent.getCharSequenceArrayListExtra(EXTRA_SENSORS);
        //final String prefGTS = intent.getStringExtra(EXTRA_PREFIX);
        startCollectService();
        return START_STICKY;
    }

    /**
     * Function used to start service, start an intend for each sensors registered, and launch the location service
     * if needed
     */
    private void startCollectService() {
        // Initialisation
        int limit;
        context = this;
        sensorService = new SensorService();
        locationService = new LocationService();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.token = sharedPreferences.getString("token", "NULL");
        this.prefixGts = sharedPreferences.getString("prefix", "NULL");
        List<String> sensorName = WarpActivity.getSensorsCheckedBefore(sharedPreferences);
        ArrayList<CharSequence> sensorNameList = new ArrayList<>();
        sensorNameList.addAll(sensorName);
        this.flushTime = Integer.valueOf(sharedPreferences.getString("flush", "60"));
        this.isPostActive = sharedPreferences.getBoolean("postWS", true);
        this.url = sharedPreferences.getString("url", "NULL");
        this.socketUrl = sharedPreferences.getString("urlWS", "NULL");
        boolean useNet = sharedPreferences.getBoolean("useInternet", true);
        limit = Integer.valueOf(sharedPreferences.getString("limitSizeDisk", "100"));
        boolean mode = sharedPreferences.getBoolean("keepValues",false);
        FileService.setLimitSize(limit);
        FileService.setMODE(mode);
        if (isRunning) {
            if(sensorNameList.isEmpty())
            {
                this.onDestroy();
                return;
            } else {
                // Shut down all services still alive
                stopServices();
                commitPreferencesChange(sharedPreferences);
            }
        }
        // Start all the Services
        //FileService.setContext(context);
        startServices(sensorNameList);
        if(useNet) {
            //FileService.writeLogFile("using internet OK \n");
            startHandleTimer(url, token, flushTime, false);
        } else {
            FileService.FLUSH_TIME = Integer.MAX_VALUE;
        }

        // Edit value of collect to active
        isRunning = true;
        commitPreferencesChange(sharedPreferences);
    }

    /**
     * Commit is the service running to true
     * @param sharedPreferences
     */
    private void commitPreferencesChange(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean("isActive", true);
        //ed.putString("prefix", prefixGts);
        ed.apply();
    }

    /**
     * Start the services needed
     * @param sensorNameList
     */
    private void startServices(ArrayList<CharSequence> sensorNameList) {
        //String urlWebService = "wss://warp1.cityzendata.net/api/v0/streamupdate";
        //String valToken = this.token;
        if (!isPostActive) {
            FileService.setContext(getApplicationContext());
            ws = new WebSocketDataListener(this.socketUrl, this.token);
            if (null != ws.getError()) {
                FlushService.sendNotification(this,"Socket is closed", "An error occurred during the connection with the socket : " + ws.getError() + " Try to restart the collect.");
            }
            //ws.connectWebSocket();
        }
        /*webSocket = new WebSocketJetty(urlWebService,this.token);
        try {
            webSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        if(sensorNameList != null && !sensorNameList.isEmpty()) {
            for(CharSequence sensorName : sensorNameList) {
                sensorService.startActionStart(context, sensorName.toString(), prefixGts);
            }
        }
        locationService.startActionStart(context, sensorNameList, prefixGts);
        //FlushWithSocket.connectWebSocket(urlWebService,this.token);
    }

    /**
     * Stop the running services
     */
    private void stopServices() {
        if(SensorService.shouldContinue) {
            SensorService.startActionStop(context);
        }
        if(LocationService.isRunning) {
            Intent intent = new Intent(context, LocationService.class);
            stopService(intent);
        }
        if(!isPostActive) {
            FileService.setContext(getApplicationContext());
            CollectService.ws.closeWebSocket();
        }
        //webSocket.closeSockets();
    }

    /**
     * Methods to create the alarm which flush data
     * @param warpUrl url
     * @param warpToken authenticate write token
     */
    private void startHandleTimer(String warpUrl, String warpToken, int flushTime, boolean stop) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(EXTRA_URL, warpUrl);
        intent.putExtra(EXTRA_TOKEN, warpToken);
        intent.putExtra(EXTRA_STOP, stop);
        if(stop)
        {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_ONE_SHOT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + flushTime,
                    pendingIntent);
        }else {
            int flushTimeInSeconds = setFlushTime(flushTime);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), flushTimeInSeconds,
                    pendingIntent);
        }
    }

    private int setFlushTime(int flushTime) {
        if (flushTime < 60)
        {
            flushTime = 60;
        }
        int flushTimeInSeconds = flushTime * 1000;
        FileService.FLUSH_TIME = (flushTimeInSeconds*3)/4;
        return flushTimeInSeconds;
    }

    /**
     * Stop action handler to end the alarm which flush data at regular time
     */
    private void stopHandleTimer() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Method call to end the collect Main Service
     */
    public void onDestroy () {
        // update context
        context = this;
        if(isRunning)
        {
            // Stop timer
            stopHandleTimer();

            // Initialisation
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            this.url = sharedPreferences.getString("url", "NULL");
            this.token = sharedPreferences.getString("token", "NULL");
            boolean useNet = sharedPreferences.getBoolean("useInternet", false);

            // Exec last alarm
            if(useNet) {
                startHandleTimer(url, token, 1000, true);
            } else {
                SharedPreferences.Editor ed = sharedPreferences.edit();
                ed.putBoolean("isActive", false);
                ed.apply();
            }

            // Shut down all the services
            stopServices();

            // Service done
            isRunning = false;
        }else {
            // Shut down all services still alive
            stopServices();
        }
    }

    //TODO Use of bluetooth
}
