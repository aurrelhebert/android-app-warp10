package com.warp10.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by ahebert on 2/11/16.
 */
public class FirstActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view
        setContentView(R.layout.first_layout);

//        // Generate options for the bitmap generation
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = false;
//
//        // Get the window display
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//
//        // Add the window height to the option height, and same for width
//        options.outHeight = size.y;
//        options.outWidth = size.x;

        // Put option, context and image view as final to use them in thread
        //final BitmapFactory.Options optionsThread = options;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        // Get the window display
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Add the window height to the option height, and same for width
        options.outHeight = size.y;
        options.outWidth = size.x;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ussenterprise, options);
        ImageView mImageView = (ImageView) findViewById(R.id.imageView2);
        mImageView.setImageBitmap(bitmap);

        //Log.d("create", "C " + bitmap.describeContents());
        // Thread to generate the font image
//        new Thread(new Runnable() {
//            public void run() {
//                final
//                imgView.post(new Runnable() {
//                    public void run() {
//                        imgView.setImageBitmap(currentBitmap);
//                    }
//                });
//            }
//        }).start();

        Toolbar toolbar = (Toolbar) findViewById(com.warp10.app.R.id.toolbarFirst);
        setSupportActionBar(toolbar);

//        Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//        Canvas c = new Canvas(b);
//        c.drawText("Test",0,0, new TextPaint());
//        ImageButton imgButton = (ImageButton) findViewById(R.id.imageButton);
//        c.save();
//        imgButton.setImageBitmap(b);
    }

    public void loadProfile(View view) {
        SharedPreferences sp = this.getSharedPreferences(ProfileFragment.NAME_SHARED_FILE_PROFILE, Activity.MODE_PRIVATE);
//        if(sp.getAll().isEmpty()) {
//            createDialogSingleMessage("Please create a profile before going to the load page");
//            return;
//        }
        Intent intent = new Intent(this,SetLoadProfile.class);
        startActivity(intent);
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

    public void newProfile(View view) {
        Intent intent = new Intent(this, NewProfile.class);
        startActivity(intent);
        SharedPreferences sp = this.getSharedPreferences("profile", MODE_PRIVATE);
        Log.d("Profile ?", sp.getAll().toString());
    }
}
