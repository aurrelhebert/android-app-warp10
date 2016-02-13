package com.warp10.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ahebert on 2/12/16.
 */
public class NewProfile extends AppCompatActivity {

    protected static int count = 10000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_profile);

        LoadProfile profile;
        SharedPreferences sharedPreferences = getSharedPreferences(ProfileFragment.NAME_SHARED_FILE_PROFILE,MODE_PRIVATE);
        while (sharedPreferences.contains("" + count)){
            count++;
        }
        final int countValue = count;
        count++;

        // View

        /**
         * Set null on name
         */
        EditText edName = (EditText) findViewById(R.id.currentProfileName);
        edName.setText("NULL");

        /**
         * Set null on Url
         */
        EditText edUrl = (EditText) findViewById(R.id.currentProfileUrl);
        edUrl.setText("NULL");

        /**
         * Set null on Token
         */
        EditText edTok = (EditText) findViewById(R.id.currentProfileToken);
        edTok.setText("NULL");

        /**
         * Set null on value
         */
        EditText edPref = (EditText) findViewById(R.id.currentProfilePrefix);
        edPref.setText("NULL");

        Button okButton = (Button) findViewById(R.id.profileOk);

        /**
         * Save new title and value to create a new sensor preference
         */
        final Context ctx = this;
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.currentProfileName);

                EditText url = (EditText) findViewById(R.id.currentProfileUrl);
                EditText token = (EditText) findViewById(R.id.currentProfileToken);
                EditText prefix = (EditText) findViewById(R.id.currentProfilePrefix);
                String myValue = name.getText().toString().replaceAll(";","_") + ";" + url.getText().toString() + ";" +
                        token.getText().toString() + ";" + prefix.getText().toString().replaceAll(";","_") + ";" +
                        "[]";

                LoadProfile profile = new LoadProfile(ctx, null,"" + countValue);
                profile.setKey("" + countValue);

                SharedPreferences sp = getSharedPreferences(ProfileFragment.NAME_SHARED_FILE_PROFILE,
                        MODE_PRIVATE);
                //Log.d("" + countValue, myValue);
                sp.edit().putString("" + countValue, myValue).commit();
                //profile.getPreferenceManager().setSharedPreferencesName("profile");
                //profile.saveValue(myValue, name.getText().toString().replaceAll(";", "_"));
                //getPreferenceManager().setSharedPreferencesName("profile");
                //Pre
                //        findPreference("profilePrefScreen");
                //preferenceScreen.addPreference(profile);
                //Log.d("" + countValue, sp.getString("" + countValue, "NULL"));
                Intent intent = new Intent(ctx, WarpActivity.class);
                startActivity(intent);
            }
        });

        /**
         * End the dialog
         */
        Button cancelButton = (Button) findViewById(R.id.profileCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, FirstActivity.class);
                startActivity(intent);
            }
        });

        /**
         * Do not put on screen the delete button wich exist in the layout sensor
         */
        Button deleteButton = (Button) findViewById(R.id.profileDelete);
        ViewGroup layout = (ViewGroup) deleteButton.getParent();
        if(null!=layout) //for safety only  as you are doing onClick
            layout.removeView(deleteButton);

    }
}
