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

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service used to always get available user loaction
 */
public class LocationService extends Service {

    /**
     * Binder to establish a connection with an other activity
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * String Buffer
     */
    protected StringBuffer stringBuffer;

    // MAX Buffer size
    private final static int BUFFER_SIZE = 1000;

    /**
     * Action Start
     */
    private static final String ACTION_START = "com.cityzendata.warpapp.action.START";

    /**
     * Location listener (GPS and/or Network) and manager
     */
    protected LocationListener locationListener = null;
    protected LocationManager locationManager = null;

    protected static volatile boolean isRunning = false;

    protected Intent intent = null;

    /**
     * Action start parameter, service can listen to GPS or Network
     */
    private static final String EXTRA_LISTGPS = "com.cityzendata.warpapp.extra.LISTGPS";
    private static final String EXTRA_LISTNET = "com.cityzendata.warpapp.extra.LISTNET";
    private static final String EXTRA_RECGPS = "com.cityzendata.warpapp.extra.RECGPS";
    private static final String EXTRA_RECNET = "com.cityzendata.warpapp.extra.RECNET";
    private static final String EXTRA_PREFIX = "com.cityzendata.warpapp.extra.PREFIX";

    /**
     * Basic Constructor
     */
    public LocationService() {
    }

    /**
     * Start location Service
     * @param context
     * @param sensorNameList
     * @param prefixGts
     */
    public void startActionStart(Context context, ArrayList<CharSequence> sensorNameList, String prefixGts) {
        handleStartCommand(context,sensorNameList.contains("GPS"), sensorNameList.contains("NETWORK"),
                sensorNameList.contains("GPS GTS"),sensorNameList.contains("NETWORK GTS"),prefixGts);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Command to launch the location service
     * @param context application context
     * @param isListenGPS iif user want to listen to GPS
     * @param isListenNetwork iif user want to listen to Network location
     */
    protected void handleStartCommand(Context context, boolean isListenGPS, boolean isListenNetwork,
                                   boolean recordGPS, boolean recordNetwork, String prefix) {
        Intent intentServiceGeo = new Intent(context, LocationService.class);
        intentServiceGeo.setAction(ACTION_START);
        intentServiceGeo.putExtra(EXTRA_LISTGPS, isListenGPS);
        intentServiceGeo.putExtra(EXTRA_LISTNET, isListenNetwork);
        intentServiceGeo.putExtra(EXTRA_RECGPS, recordGPS);
        intentServiceGeo.putExtra(EXTRA_RECNET, recordNetwork);
        intentServiceGeo.putExtra(EXTRA_PREFIX, prefix);
        context.startService(intentServiceGeo);
    }

    /**
     * Start service
     * @param intent intent containing action start and parameters
     * @param flags
     * @param startId
     * @return START_STICKY, meaning service won't be interrupted
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        intent.setAction(ACTION_START);
        handleCommand(intent);
        // We want this service to run only in response to a start action
        // so return start redeliver intent.
        return START_REDELIVER_INTENT;
    }

    /**
     * Execution of action start
     * @param intent
     */
    protected void handleCommand(Intent intent) {
        if (null != intent) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                final Boolean isListenGPS = intent.getBooleanExtra(EXTRA_LISTGPS, false);
                final Boolean isListenNetWork = intent.getBooleanExtra(EXTRA_LISTNET, false);
                final Boolean recGPS = intent.getBooleanExtra(EXTRA_RECGPS, false);
                final Boolean recNetWork = intent.getBooleanExtra(EXTRA_RECNET, false);
                final String prefix = intent.getStringExtra(EXTRA_PREFIX);
                //Log.d("ALL", isListenGPS + " " + isListenNetWork + " " + recGPS + " " + recNetWork + prefix);
                handleActionStart(isListenGPS, isListenNetWork, this, recGPS, recNetWork, prefix);
            }
        }
    }

    /**
     * Handler of action Start
     * @param isListenGPS if asked register GPS
     * @param isListenNetWork if asked register Network location
     */
    private void handleActionStart(boolean isListenGPS, boolean isListenNetWork, final Context context,
                                   boolean recGPS, boolean recNetwork, final String prefixGTS) {
        // Define a listener that responds to location updates

        if(isRunning) {
            this.onDestroy();
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final boolean record = recGPS || recNetwork;

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (record) {
                    //Log.d("GPSLOCA",location.toString());
                    long timestamp = System.currentTimeMillis() * 1000;
                    // If buffer have full size
                    if (null == stringBuffer) {
                        stringBuffer = new StringBuffer();
                    }
                    if (stringBuffer.length() >= BUFFER_SIZE) {
                        emptyBuffer();
                    }
                    if(stringBuffer.length() > 0 )
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
                    String string = timestamp + "/" + location.getLatitude() + ":" + location.getLongitude() + "/ " + prefixGTS + fix + location.getProvider() + "{"
                            + "source=android" + "} true";
                    stringBuffer.append(string);
                    //Log.d("Location Handler", stringBuffer.toString());
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                //emptyBuffer();
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                emptyBuffer();
            }

            /**
             * empty current buffer
             */
            public void emptyBuffer() {
                if (null != stringBuffer) {
                    //Log.d("LocationService", stringBuffer.toString());
                    final StringBuffer buffer = new StringBuffer(stringBuffer);
                    if(CollectService.isPostActive) {
                        FileService.writeToFile(buffer.toString(), context);
                    } else {
                        if(CollectService.ws.isClosed()) {
                            FileService.writeToFile(buffer.toString(), context);
                        } else {
                            List<File> allFiles = FileService.getAllFiles("fill", context, true);
                            for (File file:allFiles) {
                                String data = FileService.readMetricFile(file);
                                if(CollectService.ws.writeData(data)) {
                                    file.delete();
                                }
                            }
                            if(!CollectService.ws.writeData(buffer.toString())) {
                                FileService.writeToFile(buffer.toString(), context);
                            }
                        }
                    }
                    stringBuffer = new StringBuffer();
                }
            }
        };


        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (isListenGPS || recGPS) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            setLocManager();
        }
        if (isListenNetWork || recNetwork) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
            setLocManager();
        }
        //Log.d("LOCATIONB", locationManager.getAllProviders().toString());
        //Log.d("LOCATIONB", locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).toString());
        isRunning = true;

    }

    /**
     * Static location manger to use getLastLocation as static method
     */
    protected static LocationManager locManager = null;

    /**
     * Static Context to use getLastLocation as static method
     */
    protected static Context ctx = null;

    public void setLocManager() {
        locManager = locationManager;
        ctx = this;
    }

    /**
     * Function used to get the last location of user
     * @param isListenGPS if check GPS provider if available
     * @param isListenNetWork if check Network provider
     * @return Last known location
     */
    public static Location getLastLocation(boolean isListenGPS, boolean isListenNetWork) {
        Location mLastLocation = null;
        if (null != locManager) {
            if (isListenGPS) {
                if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return null;
                    }
                    mLastLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                else if (isListenNetWork) {
                    mLastLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            else if(isListenNetWork) {
                mLastLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        return mLastLocation;
    }

    /**
     * End of service, unregistered GPS or Network providers
     */
    public void onDestroy () {
        if(isRunning) {
            if (null != locationManager) {
                locationListener.onProviderDisabled(LocationManager.NETWORK_PROVIDER);
                locationManager.removeUpdates(locationListener);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationListener.onProviderDisabled(LocationManager.GPS_PROVIDER);
                locationManager = null;
                //Log.d("LOCATION", "Detroyed");
            }
            //stopSelf();
            //Log.d("LOC", "STOP service location");
            isRunning = false;
        }
    }
}
