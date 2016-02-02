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

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. This service collect data on all sensors the application listen
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SensorService extends IntentService implements SensorEventListener {
    // TODO: Rename actions, choose action names that describe tasks that this

    // Boolean used to stop intent service
    public static volatile boolean shouldContinue = true;

    /**
     * Boolean used to know if user want to listen to the GPS
     */
    protected static boolean isListenGPS = false;

    /**
     * Boolean used to know if user want to listen to the Network position
     */
    protected static boolean isListenNetWork = false;

    /**
     * Connection with location Service
     */
    protected boolean mBound = false;

    /**
     *
     */
    protected static boolean isLocationServiceStarted = false;

    // MAX Buffer size
    private final static int BUFFER_SIZE = 1000;

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_START = "com.cityzendata.warpapp.action.STARTLOC";
    private static final String ACTION_STARTB = "com.cityzendata.warpapp.action.STARTSENSORB";
    private static final String ACTION_STOP = "com.cityzendata.warpapp.action.STOP";

    /**
     * Location service
     */
    private static LocationService locationService = new LocationService();

    /**
     * Warp url parameter
     */
    protected static String url = "localhost:4242";

    /**
     * Write token of the application
     */
    protected static String token = "token";

    /**
     * Prefix of the GTS given by the user
     */
    protected String prefixGTS;

    // String Buffer
    protected StringBuffer stringBuffer;

    // Parameters of start/stop service function
    private static final String EXTRA_SENSORS = "com.cityzendata.warpapp.extra.SENSORS";
    private static final String EXTRA_PREFIX = "com.cityzendata.warpapp.extra.PREFIX";
    private static final String EXTRA_URL = "com.cityzendata.warpapp.extra.URL";
    private static final String EXTRA_TOKEN = "com.cityzendata.warpapp.extra.TOKEN";
    private static final String EXTRA_FLUSH = "com.cityzendata.warpapp.extra.FLUSH";

    // Sensor Manager to detect captors to collect data
    private SensorManager mSensorManager = null;

    protected static Map<String, Sensor> sensorMap;

    protected static Map<String, String> mapSensorDescription;
    protected static Map<String, String> mapSensorNameType;




    /**
     * Connection with the location Service
     */

    /**
     * Basic Constructor
     */
    public SensorService() {
        super("SensorService");
    }

    /**
     * Starts this service to perform action Start with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    @Deprecated
    public static void startActionStart(Context context, List<CharSequence> sensorNameList, String prefixGTS, String warpUrl, String warpToken, int flushTime) {
        Intent intent = new Intent(context, SensorService.class);
        intent.setAction(ACTION_START);
        intent.putCharSequenceArrayListExtra(EXTRA_SENSORS, (ArrayList<CharSequence>) sensorNameList);
        intent.putExtra(EXTRA_PREFIX, prefixGTS);
        intent.putExtra(EXTRA_URL, warpUrl);
        intent.putExtra(EXTRA_TOKEN, warpToken);
        intent.putExtra(EXTRA_FLUSH, flushTime);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Stop with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStop(Context context) {
        Intent intent = new Intent(context, SensorService.class);
        intent.setAction(ACTION_STOP);
        //intent.putCharSequenceArrayListExtra(EXTRA_PARAM1, (ArrayList<CharSequence>) param1);
        context.startService(intent);
    }

    /**
     * Handle intent that can be start or stop an action
     * @param intent contain an action, and it's parameter
     */
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                final ArrayList<CharSequence> sensorNameList = intent.getCharSequenceArrayListExtra(EXTRA_SENSORS);
                final String prefixGTS = intent.getStringExtra(EXTRA_PREFIX);
                final String warpUrl = intent.getStringExtra(EXTRA_URL);
                final String warpToken = intent.getStringExtra(EXTRA_TOKEN);
                final int flushTime = intent.getIntExtra(EXTRA_FLUSH,60);
                handleActionStart(sensorNameList, prefixGTS, warpUrl, warpToken, flushTime);
            } else if (ACTION_STOP.equals(action)) {
                handleActionStop();
            } else if (ACTION_STARTB.equals(action)) {
                final String sensorName = intent.getStringExtra(EXTRA_SENSORS);
                final String prefixGTS = intent.getStringExtra(EXTRA_PREFIX);
                handleActionStartB(sensorName, prefixGTS);
            }
        }
    }

    private void handleActionStartB(String sensorName, String prefixGTS) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            setUpMap();
            registerListeners(sensorName);
        }
        Log.d("SensorService", "Action START");
        shouldContinue = true;
        this.prefixGTS = prefixGTS;
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        initialiseHashMap(sharedPrefs);
        createMapSensorType();
        //wsDL = new WebSocketDataListener(sharedPrefs.getString("url", "NULL"),sharedPrefs.getString("token", "NULL"));
    }

    protected void setUpMap()
    {
        if(sensorMap == null || sensorMap.isEmpty()) {
            sensorMap = new HashMap<>();
            for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
                sensorMap.put(sensor.getName(), sensor);
            }
        }
    }
    /**
     * Handle action Start in the provided background thread with the provided
     * parameters.
     */
    @Deprecated
    private void handleActionStart(List<CharSequence> sensorNameList, String prefixGTS, String warpUrl, String warpToken, int flushTime) {
        if (flushTime < 60)
        {
            flushTime = 60;
        }
        int flushTimeInSeconds = flushTime * 1000;
        FileService.FLUSH_TIME = (flushTimeInSeconds*3)/4;
        //Log.v("SensorService", "SensingService.onHandleIntent");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            registerListeners(sensorNameList);
        }
        Log.d("SensorService", "Action START");
        shouldContinue = true;
        url = warpUrl;
        token = warpToken;
        this.prefixGTS = prefixGTS;
        if (stringBuffer == null) {
            stringBuffer = new StringBuffer();
        }
        if(sensorNameList.contains("GPS"))
        {
            isListenGPS = true;
        }
        if(sensorNameList.contains("NETWORK")){
            isListenNetWork = true;
        }
        boolean recGPS= false, recNetwork = false;
        if(sensorNameList.contains("GPS GTS"))
        {
            recGPS = true;
        }
        if(sensorNameList.contains("NETWORK GTS")){
            recNetwork = true;
        }
        if(isListenGPS || isListenNetWork || recGPS || recNetwork) {
            locationService.handleStartCommand(this, isListenGPS, isListenNetWork, recGPS,
                   recNetwork, prefixGTS);
            isLocationServiceStarted = true;
        }
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        //SharedPreferences.Editor ed = sharedPrefs.edit();
        //ed.putBoolean("isActive", true);
        //ed.commit();
    }

    /**
     * Handle action Stop in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStop() {
        shouldContinue = false;
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        //boolean isActive = sharedPrefs.getBoolean("isActive", false);
        Log.d("ActionSTOP", "Handled");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            mSensorManager.unregisterListener(this);
            //Log.d("SensorService", sensor.getName().toString());
        }
        /*
        if (isLocationServiceStarted) {
            stopService(new Intent(this, LocationService.class));
        }
        */
        //FlushService.flushAllFiles("fill", this, url, token, true);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    /**
     * Action executed every time one value of a recorded sensor changed
     * @param currentEvent event
     */
    public void onSensorChanged(SensorEvent currentEvent) {
        // If app continue
        if (shouldContinue == true) {
            String locString;
            //Log.d("IsLocStarted", "" + isLocationServiceStarted);
            if (isLocationServiceStarted) {
                //onBind(new Intent(this, LocationService.class));
                locString = getPositionString();
                //unbindService(mConnection);
            } else
            {
                locString = "// ";
            }
            //Log.d("LOCATION", locString);
            // If buffer have full size
            if (stringBuffer == null) {
                stringBuffer = new StringBuffer();
            }
            if (stringBuffer.length() >= BUFFER_SIZE) {
                emptyBuffer();
            }
            if (stringBuffer.length() > 0 )
            {
                stringBuffer.append("\n");
            }
            String fix = "";
            /**
             if(prefixGTS.equals(new String()))
             {
             prefixGTS = "android";
             }*/
            if (prefixGTS.length() != prefixGTS.lastIndexOf(".")) {
                fix = ".";
            }
            long timestamp = System.currentTimeMillis() * 1000;
            int counter = 0;
            String[] splitDescription = null;
            String type = mapSensorNameType.get(getSensorName(currentEvent));
            String description = "NULL";
            if(mapSensorDescription.containsKey(type)) {
                description = mapSensorDescription.get(type);
            }
            if (!description.equals("NULL")) {
                splitDescription = description.replaceAll("\\s", "").split(",");
            }
            for (Float val : currentEvent.values) {
                String currentDesc = "";
                if( null != splitDescription &&
                        splitDescription.length >= counter+1) {
                    currentDesc += ".";
                    currentDesc += splitDescription[counter];
                }
                //Log.d("SensorDesc", description);
                String string = timestamp + locString + prefixGTS.replaceAll("\\s","") + fix + getSensorName(currentEvent).replace(" ", ".")
                        + currentDesc + "{" + "type=" + getSensorType(currentEvent) + ",vendor=" + currentEvent.sensor.getVendor().replace(" ", "-") +
                        ",version=" + currentEvent.sensor.getVersion() + ",source=android";
                //Log.d("Val", string);
                if(counter > 0)
                {
                    stringBuffer.append("\n");
                }
                stringBuffer.append(string);
                stringBuffer.append(",valueIndex=" + counter + "} ");
                stringBuffer.append(val);
                counter++;
            }
        }
        // If app stopped
        if (shouldContinue == false) {
            emptyBuffer();
            stringBuffer = null;
            for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
                mSensorManager.unregisterListener(this);
            }
            stopSelf();
            return;
        }
    }

    /**
     * Method used for testing purposes
     * @param event
     * @return
     */
    protected String getSensorName(SensorEvent event){
        return event.sensor.getName();
    }

    /**
     * Method used for testing purposes
     * @param event
     * @return
     */
    protected int getSensorType(SensorEvent event){
        return event.sensor.getType();
    }

    /**
     * Method used to detect last known position of a user
     * @return string containing /lat:long/ if last position is null contain //
     */
    protected String getPositionString() {
        String locString = "// ";
        Location mLastLocation = LocationService.getLastLocation(isListenGPS,isListenNetWork);
        if (mLastLocation != null) {
            locString = "/" + mLastLocation.getLatitude() + ":" + mLastLocation.getLongitude() + "/ ";
            //Log.d("LOCATIONB", mLastLocation.toString());
        }
        return locString;
    }

    /**
     * empty current buffer
     */
    public void emptyBuffer()
    {
        if (stringBuffer != null) {
            //Log.d("SensorService", stringBuffer.toString());
            final StringBuffer buffer = new StringBuffer(stringBuffer);
            if (CollectService.isPostActive) {
                FileService.writeToFile(buffer.toString(), this);
            } else {
                if(CollectService.ws.isClosed()) {
                    FileService.writeToFile(buffer.toString(), this);
                } else {
                    List<File> allFiles = FileService.getAllFiles("fill", this, true);
                    for (File file:allFiles) {
                        String data = FileService.readMetricFile(file);
                        CollectService.ws.writeData(data);
                    }
                    CollectService.ws.writeData(buffer.toString());
                }
            }
            //FlushWithSocket.writeValues(buffer.toString());
            //Log.d("Buffer", buffer.toString());

            //CollectService.webSocket.writeMessage(buffer.toString());
            stringBuffer = new StringBuffer();
        }
    }

    /**
     * Required method, when accuracy of sensor changed
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Method used to register a listener on sensors, only the one corresponding to the user wishes are registered
     * @param param List of captors name to register
     */
    @Deprecated
    private void registerListeners(List<CharSequence> param) {
        //Log.v("SensorService", "Registering sensors listeners");
        for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            if(param.contains(sensor.getName())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, 1000*60);
                    mSensorManager.flush(this);
                }
                else
                {
                    mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                //Log.d("SensorService", sensor.getName().toString());
                FileService.setContext(this);
                //FileService.writeLogFile("CollectSensor: " + sensor.getName().toString());
            }
        }
    }

    /**
     * Method registering listener on a sensor
     * @param sensor sensor name
     */
    private void registerListeners(String sensor) {
        if(sensorMap.containsKey(sensor))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSensorManager.registerListener(this, sensorMap.get(sensor), SensorManager.SENSOR_DELAY_NORMAL, 1000*60);
                mSensorManager.flush(this);
            }
            else
            {
                mSensorManager.registerListener(this, sensorMap.get(sensor), SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    /**
     * New action start, used to start an intent service which listen to a sensor
     * @param context application context
     * @param sensorName sensor name
     * @param prefGts user choice of GTS prefix
     */
    public void startActionStart(Context context, String sensorName, String prefGts) {
        Intent intent = new Intent(context, SensorService.class);
        intent.setAction(ACTION_STARTB);
        intent.putExtra(EXTRA_SENSORS, sensorName);
        intent.putExtra(EXTRA_PREFIX, prefGts);
        context.startService(intent);
    }

    /**
     * Function used to initialise HashMap containing user choices of value name for
     * a kind of sensor
     * @param sharedPreferences
     */
    private void initialiseHashMap(SharedPreferences sharedPreferences) {
        mapSensorDescription = new HashMap<>();
        Map<String, ?> keySet = sharedPreferences.getAll();
        List<String> lString = SensorsFragment.preferencesList;
        //Log.d("ALLPREF", lString.toString());
        for (String pref:keySet.keySet()) {
            if (!lString.contains(pref)) {
                String value = sharedPreferences.getString(pref, "NULL");
                if(!value.equals("NULL")) {
                    Pair<String, String> pair = SensorPreference.parseValue(value);
                    String type = "android.sensor." + pair.first.replaceAll("\\s+", "_").toLowerCase();
                    mapSensorDescription.put(type,pair.second);
                }

            }
        }
    }

    /**
     * Function used to initialize the map relating a sensor name to a type
     */
    private void createMapSensorType() {
        mapSensorNameType = new HashMap<>();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> list = sm.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor :list) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                /**
                 * For new version android >= 20
                 */
                mapSensorNameType.put(sensor.getName(),sensor.getStringType());
            } else
            {
                /**
                 * For android version <= 20
                 */
                String sensorType = "android.sensor.";
                int type = sensor.getType();
                switch (type) {
                    case Sensor.TYPE_ACCELEROMETER:
                        sensorType += "accelerometer";
                        break;
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        sensorType += "ambient_temperature";
                        break;
                    case Sensor.TYPE_GAME_ROTATION_VECTOR:
                        sensorType += "game_rotation_vector";
                        break;
                    case Sensor.TYPE_GRAVITY:
                        sensorType += "gravity";
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        sensorType += "gyroscope";
                        break;
                    case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                        sensorType += "gyroscope_uncalibrated";
                        break;
                    case Sensor.TYPE_LIGHT:
                        sensorType += "light";
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        sensorType += "linear_acceleration";
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        sensorType += "magnetic_field";
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                        sensorType += "magnetic_field_uncalibrated";
                        break;
                    case Sensor.TYPE_ORIENTATION:
                        sensorType += "orientation";
                        break;
                    case Sensor.TYPE_PRESSURE:
                        sensorType += "pressure";
                        break;
                    case Sensor.TYPE_PROXIMITY:
                        sensorType += "proximity";
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                        sensorType += "relative_humidity";
                        break;
                    case Sensor.TYPE_ROTATION_VECTOR:
                        sensorType += "rotation_vector";
                        break;
                    case Sensor.TYPE_SIGNIFICANT_MOTION:
                        sensorType += "significant_motion";
                        break;
                    case Sensor.TYPE_TEMPERATURE:
                        sensorType += "temperature";
                        break;
                    default: sensorType = "NULL";
                        break;
                }
                mapSensorNameType.put(sensor.getName(),sensorType);
            }
        }
        //Log.d("MAP TYPEs",mapSensorNameType.toString());
    }
}
