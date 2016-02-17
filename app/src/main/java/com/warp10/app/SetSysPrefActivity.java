package com.warp10.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by ahebert on 2/17/16.
 */
public class SetSysPrefActivity extends Activity {

    @Override
    /**
     * Method creating the Menu
     */
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SystemSettingsFragment()).commit();
    }
}
