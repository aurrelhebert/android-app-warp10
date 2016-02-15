package com.warp10.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by ahebert on 2/12/16.
 */
public class SetLoadProfile extends Activity {
    @Override
    /**
     * Method executing on Creation of this Menu (When there is a click on Settings item inside Menu)
     */
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new ProfileFragment()).commit();
    }
}
