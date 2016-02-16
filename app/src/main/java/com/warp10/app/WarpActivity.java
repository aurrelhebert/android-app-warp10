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
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO Button test Connection to plat-form and remove from Start.

/**
 * Main android activity
 */
public class WarpActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_URL = "isActive";
    private Menu menu;
    @Override
    /**
     * Method called to initialize all the components of the main activity
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getApplicationContext(), com.warp10.app.R.xml.preferences, true);
        PreferenceManager.setDefaultValues(getApplicationContext(), com.warp10.app.R.xml.sensors, true);
        setContentView(com.warp10.app.R.layout.activity_warp);
        Toolbar toolbar = (Toolbar) findViewById(com.warp10.app.R.id.toolbar);
        setSupportActionBar(toolbar);
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LinearLayout lLayout = (LinearLayout)findViewById(com.warp10.app.R.id.mainLayout);
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        List<String> sensorNames = getSensorsCheckedBefore(sharedPrefs);
        Log.d("ONCREATE", sensorNames.toString());
        for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(sensorNames.contains(sensor.getName()));
            checkBox.setText(sensor.getName());
            lLayout.addView(checkBox);
        }
        CheckBox checkBox;
        /*
        TextView positionText = new TextView(this);
        positionText.setText("Choose a position provider, that will add the last user known location to all yours sensors GTS");
        lLayout.addView(positionText);
        */
        // To add record, add
        // "GPS", "NETWORK"
        List<String> namesList = Arrays.asList("GPS GTS", "NETWORK GTS");
        for (String name : namesList) {
            if(name.equals("GPS GTS"))
            {
                TextView gtsText = new TextView(this);
                gtsText.setText("To create a GTS recording user movement");
                lLayout.addView(gtsText);
            }
            checkBox = new CheckBox(this);
            checkBox.setText(name);
            checkBox.setChecked(sensorNames.contains(name));
            lLayout.addView(checkBox);
        }
        /*
        for (int i = 0; i < lLayout.getChildCount(); i++)
        {
            if (lLayout.getChildAt(i) instanceof CheckBox)
            {
                Log.d("TAG", (((CheckBox) lLayout.getChildAt(i)).getText()).toString());
            }
        }
        */
        showUserSettings();

        SharedPreferences sp = this.getSharedPreferences(ProfileFragment.NAME_SHARED_FILE_PROFILE,
                MODE_PRIVATE);
        Log.d("Profile ?", sp.getAll().toString());
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Method call to initialize and refreshed the text with the last user preference recorded
     */
    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        StringBuilder builder = new StringBuilder("  Current url were data will be pushed is : ");

        builder.append("\n" + sharedPrefs.getString("url", "NULL"));

        builder.append("\n  To change this value, and set up your authentication token, go in the application setting");

        builder.append("\n  Choose a prefix for yours GTS, which is added to the sensors name to construct the GTS name and finally check which sensors will record data that are available on your phone : ");

        TextView settingsTextView = (TextView) findViewById(com.warp10.app.R.id.textView);

        TextView settingsPrefix = (TextView) findViewById(com.warp10.app.R.id.prefixGTS);

        settingsTextView.setText(builder.toString());

        settingsPrefix.setText(sharedPrefs.getString("prefix", "NULL"));

        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        RadioButton radioButton = (RadioButton) findViewById(com.warp10.app.R.id.isCollectActive);
        if(sharedPrefs.getBoolean("isActive", false)) {
            radioButton.setChecked(true);
        }
    }

    /**
     * When the user press return on the preference page
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
  /*
   * always re-load Preference setting.
   */
        showUserSettings();
    }

    /**
     * Initialize the Menu
     * @param menu main menu of the application
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_warp, menu);
        getMenuInflater().inflate(com.warp10.app.R.menu.menu_warp, menu);
        this.menu = menu;
        return true;
    }

    /**
     * Action to apply when an item of the menu is clicked
     * @param item menu item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Launch start action of the collect Service
     * @param view current view
     */
    public void startCollect(View view) {
        //collectService = new SensorService();
        ArrayList<CharSequence> sensorList = getAllCheckedSensors();
        Log.d("ListSensors", sensorList.toString());
        EditText editText = (EditText) findViewById(com.warp10.app.R.id.prefixGTS);
        final String prefixGTS = editText.getText().toString();

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        final String warpUrl = sharedPrefs.getString("url", "NULL");
        final String token = sharedPrefs.getString("token", "NULL");
        SharedPreferences.Editor ed = sharedPrefs.edit();
        String names = sensorList.toString();
        ed.putString("checkedGTS",names);
        ed.putString("prefix", prefixGTS);
        ed.apply();

        //Save the current Profile
        saveProfile();

        if(warpUrl.equals("NULL") || token.equals("NULL")) {
            createDialogSingleMessage("Consider changing url and token in this app setting");
        }
        //String token = "hn.iXMFqrZSmQzg8y5Tv2lXsKf.N5ifkitno6L3tfVfGs_MABi_aobQfB7.Qx8z5e1UO_1enRJa0tuSoIp8Pq0QVG4tulOeSAYqwERw5FhEePZXTOnwFAVZZtpYTbeB8UjlBr7qjM5uJcM6WK_Kv7iTvQQChNiSMvQ5SNXvMfRN";
        else if( warpUrl!=null && token != null)
        {
            //if (FlushService.sendAtestPost(new StringBuffer(), warpUrl, token)) {
            //SensorService.startActionStart(getApplicationContext(), sensorList, prefixGTS, warpUrl, token, flushTime);
            if(sensorList.isEmpty())
            {
                createDialogSingleMessage("No sensor register, service will not start");
            } else {
                if(prefixGTS.equals("NULL"))
                {
                    createDialogSingleMessage("Prefix of GTS to collect equals to NULL, collect is running");
                }
                //CollectService collectService = new CollectService();
                startService(new Intent(getApplicationContext(), CollectService.class));
                this.createDialogSingleMessage("Collect is now running. " +
                        "As long as radioButton on top of the app is checked the application is running.");
                //collectService.startCollectService(getApplicationContext(), sensorList, prefixGTS);
            }
                //RadioButton radioButton = (RadioButton) findViewById(R.id.isCollectActive);
                //radioButton.setChecked(true);
            //}
            //else
            //{
            //    this.createDialogSingleMessage("Wrong association url/token or no connection available to internet");
            //}
        } else {
            this.createDialogSingleMessage("Url or token is null");
        }
    }

    /**
     * Method to register listener on preferences changed, here update radio button collect
     * in order to indicate the user if collect is currently running or not
     * @param sharedPreferences
     * @param key key of the preferences changed
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_URL)) {
            //Log.d("PrefChange", "Valid");
            RadioButton radioButton = (RadioButton) findViewById(com.warp10.app.R.id.isCollectActive);
            radioButton.setChecked(sharedPreferences.getBoolean(KEY_URL, false));
        }
    }

    /**
     * Get list of all sensors available on the telephone
     * @return An array list containing the name of those sensors
     */
    public ArrayList<CharSequence> getAllCheckedSensors()
    {
        ArrayList<CharSequence> sensorList = new ArrayList<CharSequence>();
        LinearLayout lLayout = (LinearLayout)findViewById(com.warp10.app.R.id.mainLayout);
        for (int i = 0; i < lLayout.getChildCount(); i++)
        {
            if (lLayout.getChildAt(i) instanceof CheckBox)
            {
                CheckBox checkBox = (CheckBox) lLayout.getChildAt(i);
                if (checkBox.isChecked())
                {
                    sensorList.add(checkBox.getText());
                }
            }
        }
        return sensorList;
    }

    /**
     * Launch the action stop of the collect service
     * @param view current view
     */
    public void stopCollect(View view) {

        /*
        if(SensorService.shouldContinue) {
            SensorService.startActionStop(getApplicationContext());
        }

        if(LocationService.isRunning) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            stopService(intent);
        }*/

        Intent intent = new Intent(getApplicationContext(),CollectService.class);
        stopService(intent);
        this.createDialogSingleMessage("Stop collect. When the radioButton on top of the app will be " +
                "unchecked then the collect will be correctly stopped.");
        //SensorService.startActionStop(getApplicationContext());
        //RadioButton radioButton = (RadioButton) findViewById(R.id.isCollectActive);
        //radioButton.setChecked(false);
    }

    /**
     * Create a dialog box between user and the application printing a line on screen
     * @param screenText line to print on screen
     */
    private void createDialogSingleMessage(String screenText)
    {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(screenText)
                .setTitle(com.warp10.app.R.string.app_name);

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.cancel();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Method use to get a list containing all the sensors checked on screen
     * @param sharedPrefs
     * @return
     */
    public static List<String> getSensorsCheckedBefore(SharedPreferences sharedPrefs) {
        ArrayList<String> sensorsName = new ArrayList<>();
        /*
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
                */
        String allName = sharedPrefs.getString("checkedGTS", "[]");
        if(allName.equals("[]")) {
            return sensorsName;
        }
        StringBuilder sb = new StringBuilder(allName);
        sb.deleteCharAt(0);
        sb.deleteCharAt(sb.lastIndexOf("]"));
        String[] tokens = sb.toString().split(", ");
        for(String sensor : tokens) {
            sensorsName.add(sensor);
        }
        return  sensorsName;
    }

    /**
     * Method used when item settings is pressed
     * Generate a new setting page containing main settings for the application
     * @param item
     */
    public void menuOnSettingsClick(MenuItem item) {
        Intent settingsIntent = new Intent(WarpActivity.this, SetPreferenceActivity.class);
        WarpActivity.this.startActivityForResult(settingsIntent, 0);
    }

    /**
     * Function used when user click on flushed all files
     * Use the method flushAllFiles with value stop to true
     * Collect have to be stopped
     * @param item
     */
    public void menuOnFlushClick(MenuItem item) {
        Context context = getApplicationContext();
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        Boolean isActive = sharedPrefs.getBoolean("isActive", false);
        if(isActive)
        {
            createDialogSingleMessage("Can not flush all the files when collect is Active, flush are already done at regular time");
            return;
        }
        String url = sharedPrefs.getString("url", "NULL");
        String token = sharedPrefs.getString("token","NULL");
        //FlushService.sendNotification(this, url, token);
        FlushService.flushAllFiles("fill", context, url, token, true);
    }

    /**
     * Function used when user click on flushed all files
     * Use the method flushAllFiles with value stop to true
     * Collect have to be stopped
     * @param item
     */
    public void menuOnFlushDuringCollectClick(MenuItem item) {
        Context context = getApplicationContext();
        FileService.MIN_VALID_DATE = System.currentTimeMillis();
        Log.d("Val min data","" + FileService.MIN_VALID_DATE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmFlushData.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000,
                pendingIntent);
    }


    /**
     * Function used when user press delete all files
     * Delete all the files (metrics && tmp) created by the application using method of fileService
     * Collect have to be stopped
     * @param item
     */
    public void menuOnDeleteClick(MenuItem item) {
        Context context = getApplicationContext();
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        Boolean isActive = sharedPrefs.getBoolean("isActive", false);
        if(isActive)
        {
            createDialogSingleMessage("Can not delete files when collect is Active");
            return;
        }
        List<File> files = FileService.getAllFiles("fill", context, true);
        for (File file : files) {
            file.delete();
        }
    }

    /**
     * Method used when item read log pressed
     * Read the log file generated by the application
     * @param item
     */
    public void menuOnReadLogClick(MenuItem item) {
        Context context = getApplicationContext();
        File file = new File(context.getExternalFilesDir(
                null), "warp.log");
        if(file.exists()) {
            String logText = FileService.readFile(file);
            createDialogSingleMessage(logText);
        } else {
            createDialogSingleMessage("Log File is empty");
        }
    }

    /**
     * Method used when item clean log pressed
     * Clean log file generated by the application
     * @param item
     */
    public void menuOnCleanLogClick(MenuItem item) {
        Context context = getApplicationContext();
        File file = new File(context.getExternalFilesDir(
                null), "warp.log");
        file.delete();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        // Here you can perform updates to the CAB due to
        // an invalidate() request
        return true;
    }

    /**
     * Method used when item sensors description pressed
     * Generate a new setting page containing sensor description menu
     * @param item
     */
    public void menuOnSensorsDesClick(MenuItem item) {
        Intent settingsIntent = new Intent(WarpActivity.this, SetSensorsActivity.class);
        WarpActivity.this.startActivityForResult(settingsIntent, 0);
    }

    /**
     * Method used when select all button is pressed
     * Checked all the sensors available
     * @param view
     */
    public void selectAll(View view) {
        LinearLayout lLayout = (LinearLayout)findViewById(com.warp10.app.R.id.mainLayout);
        for (int i = 0; i < lLayout.getChildCount(); i++) {
            if (lLayout.getChildAt(i) instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) lLayout.getChildAt(i);
                checkBox.setChecked(true);
            }
        }
    }

    /**
     * Method used when select all button is pressed
     * Unchecked all the sensors available
     * @param view
     */
    public void deselectAll(View view) {
        LinearLayout lLayout = (LinearLayout)findViewById(com.warp10.app.R.id.mainLayout);
        for (int i = 0; i < lLayout.getChildCount(); i++) {
            if (lLayout.getChildAt(i) instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) lLayout.getChildAt(i);
                checkBox.setChecked(false);
            }
        }
    }

    public void saveCurrentProfile(View view) {
        saveProfile();
        //Intent intent = new Intent(this,FirstActivity.class);
        //startActivity(intent);
    }

    private void saveProfile() {
        SharedPreferences sp = this.getSharedPreferences(ProfileFragment.NAME_SHARED_FILE_PROFILE, MODE_PRIVATE);
        String key = sp.getString("currentKey", "NULL");
        //Log.d("Key", key);
        if(!key.equals("NULL")) {
            String name = LoadProfile.getName(sp.getString(key, "NULL"));
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            String url = sharedPrefs.getString("url", "NULL");
            String token = sharedPrefs.getString("token", "NULL");
            String socketUrl = sharedPrefs.getString("urlWS", "NULL");
            TextView settingsPrefix = (TextView) findViewById(com.warp10.app.R.id.prefixGTS);
            String prefix = settingsPrefix.getText().toString();
            ArrayList<CharSequence> sensorList = getAllCheckedSensors();
            String allCheckedGTS = sensorList.toString();

            String myValue = name.replaceAll(";","_") + ";" +
                    url + ";" + token + ";"
                    + prefix.replaceAll(";", "_") + ";" +
                    allCheckedGTS.replaceAll(";", "_") + ";" + socketUrl;
            //Log.d("Value", myValue);
            sp.edit().putString(key,myValue).apply();
        }
    }

    /**
     * Method answer click on create New Profile - Launch new Profile Activity
     * @param item
     */
    public void addAProfile(MenuItem item) {
        Intent intent = new Intent(this, NewProfile.class);
        startActivity(intent);
    }

    /**
     * Method to answer click on Load an existing Profile - Launch the Profile Menu
     * @param item
     */
    public void loadAProfile(MenuItem item) {
        Intent intent = new Intent(this,SetLoadProfile.class);
        startActivity(intent);
    }
}
