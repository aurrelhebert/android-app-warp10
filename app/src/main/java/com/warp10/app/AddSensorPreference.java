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
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ahebert on 1/8/16.
 * PreferenceType to add a new preference
 */
public class AddSensorPreference extends DialogPreference {

    /**
     * Key counter
     */
    public static volatile int count = 1001;
    /**
     * SensorPreference attribute --> The resulting preference
     */
    SensorPreference sensorPreference;

    /**
     * The page context
     */
    Context ctx;

    /**
     * Constructor - store the context and load the layout sensor
     * @param context
     * @param attrs
     */
    public AddSensorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(com.warp10.app.R.layout.sensor_layout);
        this.setDialogTitle("Add a sensor description");
        setDialogIcon(null);
        ctx = context;
    }

    /**
     * When clicked open a dialog containing the layout sensor
     * Put null on currentTitle and value
     * Add 2 buttons save and cancel
     * On save, update and register current sensor preference
     * On cancel, do nothing
     * @param view
     */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);

        /**
         * Set null on title
         */
        EditText ed = (EditText) view.findViewById(com.warp10.app.R.id.currentSensorTitle);
        ed.setText("NULL");

        /**
         * Set null on value
         */
        EditText ed2 = (EditText) view.findViewById(com.warp10.app.R.id.currentSensorValue);
        ed2.setText("NULL");

        final View myView = view;
        Button okButton = (Button) view.findViewById(com.warp10.app.R.id.currentSensorOk);

        /**
         * Save new title and value to create a new sensor preference
         */
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText title = (EditText) myView.findViewById(com.warp10.app.R.id.currentSensorTitle);
                EditText value = (EditText) myView.findViewById(com.warp10.app.R.id.currentSensorValue);
                String myValue = title.getText().toString() + " [" + value.getText().toString() + "]";
                sensorPreference.saveValue(myValue, title.getText().toString());
                PreferenceScreen preferenceScreen = (PreferenceScreen) getPreferenceManager().findPreference("sensorPrefScreen");
                preferenceScreen.addPreference(sensorPreference);
                getDialog().dismiss();
            }
        });

        /**
         * End the dialog
         */
        Button cancelButton = (Button) view.findViewById(com.warp10.app.R.id.currentSensorCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        /**
         * Do not put on screen the delete button wich exist in the layout sensor
         */
        Button deleteButton = (Button) view.findViewById(com.warp10.app.R.id.currentSensorDelete);
        ViewGroup layout = (ViewGroup) deleteButton.getParent();
        if(null!=layout) //for safety only  as you are doing onClick
            layout.removeView(deleteButton);
    }

    /**
     * Function used to prepare the dialog
     * Delete positive and negative buttons of the view
     * Create a new sensor preference, with a key that doesn't exist yet
     * @param builder view builder
     */
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        while (sharedPreferences.contains("" + count)){
            count++;
        }
        sensorPreference = new SensorPreference(ctx, null,"" + count);
        count++;
    }
}
