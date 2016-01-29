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

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by ahebert on 1/8/16.
 * Activity to set sensors settings page
 * See SensorsFragment to get more details
 */
public class SetSensorsActivity extends Activity {
    @Override
    /**
     * Method executing on Creation of this Menu (When there is a click on Settings item inside Menu)
     */
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SensorsFragment()).commit();
    }
}
