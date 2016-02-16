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

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ahebert on 1/8/16.
 * PreferenceType SensorPreference correspond to a sensor Description
 */
public class SensorPreference extends DialogPreference {

    /**
     * Private sensorDescription is a title and a value
     */
    private class SensorDescription {
        String title;
        String value;

        public SensorDescription(String title, String value) {
            this.title = title;
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * Value stored in shared preference : title [value]
     */
    String myValue = "";

    /**
     * Sensor description associated to the current sensor preference
     */
    SensorDescription sensorDescription;

    /**
     * Value by default
     */
    String mDefault = "null [null]";

    /**
     * attribute set of the current preference
     */
    AttributeSet attr;

    /**
     * Constructor - load the layout sensor
     * @param context
     * @param attrs
     */
    public SensorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        attr = attrs;
        setDialogLayoutResource(com.warp10.app.R.layout.sensor_layout);
        setDialogIcon(null);
    }

    /**
     * Constructor - load the layout sensor ans set key of current preference
     * @param context
     * @param attrs
     */
    public SensorPreference(Context context, AttributeSet attrs, String key) {
        super(context, attrs);
        attr = attrs;
        setDialogLayoutResource(com.warp10.app.R.layout.sensor_layout);
        setDialogIcon(null);
        this.setKey(key);
        //setPersistent(true);
    }

    /**
     * Constructor - load the layout sensor, set key and value
     * @param context
     * @param attrs
     */
    public SensorPreference(Context context, AttributeSet attrs, String value, String key) {
        super(context, attrs);
        this.setTitle(parseValue(value).first);
        this.myValue = value;
        attr = attrs;
        setDialogLayoutResource(com.warp10.app.R.layout.sensor_layout);
        setDialogIcon(null);
        this.setKey(key);
    }

    /**
     * Function used to prepare the dialog
     * Delete positive and negative buttons of the view
     * Set the title of the dialog with current preferene title
     * @param builder view builder
     */
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
        builder.setTitle(getTitle());
    }

    /**
     * When clicked open a dialog containing the layout sensor
     * Set current title and value with preferences title and value
     * Add 3 buttons save, cancel and delete
     * On save, update and register current sensor preference
     * On cancel, do nothing
     * On delete, delete current preference
     * @param view
     */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        final View myView = view;

        sensorDescription = getValue();

        /**
         * Set title with title preference
         */
        EditText ed = (EditText) view.findViewById(com.warp10.app.R.id.currentSensorTitle);
        ed.setText(sensorDescription.getTitle());

        /**
         * Set value with value preference
         */
        EditText ed2 = (EditText) view.findViewById(com.warp10.app.R.id.currentSensorValue);
        ed2.setText(sensorDescription.getValue());

        /**
         * Update current title and value of sensor preference
         */
        Button okButton = (Button) view.findViewById(com.warp10.app.R.id.currentSensorOk);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText title = (EditText) myView.findViewById(com.warp10.app.R.id.currentSensorTitle);
                EditText value = (EditText) myView.findViewById(com.warp10.app.R.id.currentSensorValue);
                myValue = title.getText().toString() + " [" + value.getText().toString() + "]";
                saveValue(myValue, title.getText().toString());
                getDialog().dismiss();
            }
        });

        /**
         * Do nothing
         */
        Button cancelButton = (Button) view.findViewById(com.warp10.app.R.id.currentSensorCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        /**
         * Delete current preference from preference screen and shared preference
         */
        Button deleteButton = (Button) view.findViewById(com.warp10.app.R.id.currentSensorDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Add a dialog Are you sure ?
                Preference preference = getPreferenceManager().findPreference(getKey());
                PreferenceScreen preferenceScreen = (PreferenceScreen) getPreferenceManager().findPreference("sensorPrefScreen");
                preferenceScreen.removePreference(preference);
                getSharedPreferences().edit().remove(getKey()).apply();
                getDialog().dismiss();
            }
        });
    }

    /**
     * Get value of current preference in sharedPreference ("title [value]")
     * @return
     */
    protected SensorDescription getValue() {
        //PreferenceManager.setDefaultValues(getContext(), getKey(),  Context.MODE_PRIVATE, R.xml.sensors, false);
        myValue = getSharedPreferences().getString(getKey(), mDefault);
        Pair<String,String> pair = parseValue(myValue);
        return new SensorDescription(pair.first, pair.second);
    }

    /**
     * Parse the value of the preference
     * @param val value of the preference ("title [value]")
     * @return Pair<Title,Value>
     */
    public static Pair<String,String> parseValue(String val) {
        //Log.d("Parse", val);
        String[] tabString = val.split(" \\[");
        if (tabString.length < 2) {
            return new Pair(tabString[0], "");
        }
        String[] tabString2 = tabString[1].split("\\]");
        return new Pair(tabString[0], tabString2[0]);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        super.onGetDefaultValue(a, index);
        return a.getString(index);
    }

    /**
     * Save the modified value and title in shared preferences
     * @param value
     * @param title
     */
    protected void saveValue(String value, String title) {
        if(null == attr) {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            //sharedPreferences.edit().putString(getKey(), value).commit();
            sharedPreferences.edit().putString(getKey(), value).apply();
            setTitle(title);
            //editor.apply();
            return;
        }
        SensorPreference pref = (SensorPreference) getPreferenceManager().findPreference(getKey());
        pref.setTitle(title);
        //getPreferenceManager().setSharedPreferencesName(getKey());
        //getEditor().putString(getKey(), value).commit();
        getEditor().putString(getKey(), value).apply();
        try {
            pref.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //this.setTitle(title);
        myValue = value;
        Log.d("SAVE", value);
    }

    /**
     * Function used to saveState, when an unwanted events happened
     */
    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        String value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeString(value);
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    /**
     * Method used for saving current state
     * @return
     */
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent,
            // use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        myState.value = myValue;
        return myState;
    }

    /**
     * Method used to restore state
     * @param state
     */
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (null == state || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        Pair<String, String> pair= parseValue(((SavedState) state).value);
        sensorDescription = new SensorDescription(pair.first,pair.second);
    }

    /**
     * If there is no default value in shared Pref add current default value
     * @param restore
     * @param defaultValue
     */
    protected void onSetInitialValue(boolean restore, Object defaultValue)
    {
        myValue = (restore ? getPersistedString(mDefault) : (String) defaultValue);
        if(!getSharedPreferences().contains(getKey())){
            getSharedPreferences().edit().putString(getKey(),myValue).apply();
        }
    }
}