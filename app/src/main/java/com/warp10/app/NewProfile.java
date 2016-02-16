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
 * Activity page to create a new Profile on Warp10 app
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
         * Set initial name
         */
        EditText edName = (EditText) findViewById(R.id.currentProfileName);
        edName.setText("name");

        /**
         * Set initial Url
         */
        EditText edUrl = (EditText) findViewById(R.id.currentProfileUrl);
        edUrl.setText("Warp10Url");

        /**
         * Set initial UrlSocket
         */
        EditText edSocket = (EditText) findViewById(R.id.currentProfileUrlSocket);
        edSocket.setText("none");

        /**
         * Set initial Token
         */
        EditText edTok = (EditText) findViewById(R.id.currentProfileToken);
        edTok.setText("token");

        /**
         * Set initial prefix
         */
        EditText edPref = (EditText) findViewById(R.id.currentProfilePrefix);
        edPref.setText("android");

        Button okButton = (Button) findViewById(R.id.profileOk);

        /**
         * Save new profile preference and its associated value
         */
        final Context ctx = this;
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.currentProfileName);

                EditText url = (EditText) findViewById(R.id.currentProfileUrl);
                EditText token = (EditText) findViewById(R.id.currentProfileToken);
                EditText prefix = (EditText) findViewById(R.id.currentProfilePrefix);
                EditText socketUrl = (EditText) findViewById(R.id.currentProfileUrlSocket);
                String myValue = name.getText().toString().replaceAll(";","_") + ";" + url.getText().toString() + ";" +
                        token.getText().toString() + ";" + prefix.getText().toString().replaceAll(";","_") + ";" +
                        "[]" + ";" + socketUrl.getText().toString();

                LoadProfile profile = new LoadProfile(ctx, null,"" + countValue);
                profile.setKey("" + countValue);

                SharedPreferences sp = getSharedPreferences(ProfileFragment.NAME_SHARED_FILE_PROFILE,
                        MODE_PRIVATE);
                //Log.d("" + countValue, myValue);
                sp.edit().putString("" + countValue, myValue).apply();

                SharedPreferences sharedPreferences = PreferenceManager.
                        getDefaultSharedPreferences(getApplicationContext());
                sharedPreferences.edit().putString("url",url.getText().toString()).apply();
                sharedPreferences.edit().putString("token",token.getText().toString()).apply();
                sharedPreferences.edit().putString("prefix",prefix.getText().toString().
                        replaceAll(";", "_")).apply();
                sharedPreferences.edit().putString("checkedGTS","[]").apply();
                sharedPreferences.edit().putString("urlWS", socketUrl.getText().toString()).apply();
                sharedPreferences.edit().putString("profileName",name.getText().toString().replaceAll(";","_")).apply();

                sp.edit().putString("currentKey","" + countValue).apply();
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
         * Do not put on screen the delete button which exist in the layout sensor
         */
        Button deleteButton = (Button) findViewById(R.id.profileDelete);
        ViewGroup layout = (ViewGroup) deleteButton.getParent();
        if(null!=layout) //for safety only  as you are doing onClick
            layout.removeView(deleteButton);

    }
}
