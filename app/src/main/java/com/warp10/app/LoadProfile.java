package com.warp10.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ahebert on 2/12/16.
 */
public class LoadProfile extends DialogPreference {

    /**
     * Private profileDescription with a name, url, token, prefix and all checked GTS
     */
    private class Profile {
        String name;
        String url;
        String token;
        String prefix;
        String allCheckedGts;

        public Profile(String[] tab) {
            Log.d("tab", tab.toString());
            this.name = tab[0];
            this.url = tab[1];
            this.token = tab[2];
            this.prefix = tab[3];
            this.allCheckedGts = tab[4];
        }

        public String getAllCheckedGts() {
            return allCheckedGts;
        }

        public void setAllCheckedGts(String allCheckedGts) {
            this.allCheckedGts = allCheckedGts;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * Value stored in shared preference : name [value]
     */
    String myValue = "";

    /**
     * Sensor description associated to the current sensor preference
     */
    Profile profile;

    /**
     * Value by default
     */
    String mDefault = "null [null]";

    /**
     * attribute set of the current preference
     */
    AttributeSet attr;

    /**
     * Constructor - load the layout current profile
     * @param context
     * @param attrs
     */
    public LoadProfile(Context context, AttributeSet attrs)

    {
        super(context, attrs);
        attr = attrs;
        setDialogLayoutResource(R.layout.current_profile);
        setDialogIcon(null);
    }

    /**
     * Constructor - load the layout current profile and set key of current preference
     * @param context
     * @param attrs
     */
    public LoadProfile(Context context, AttributeSet attrs, String key) {
        super(context, attrs);
        attr = attrs;
        setDialogLayoutResource(R.layout.current_profile);
        setDialogIcon(null);
        this.setKey(key);
        //setPersistent(true);
    }

    /**
     * Constructor - load the layout current profile, set key and value
     * @param context
     * @param attrs
     */
    public LoadProfile(Context context, AttributeSet attrs, String value, String key) {
        super(context, attrs);
        this.setTitle(getName(value));
        this.myValue = value;
        attr = attrs;
        setDialogLayoutResource(R.layout.current_profile);
        setDialogIcon(null);
        this.setKey(key);
    }

    /**
     * function used to extract name of string
     */
    protected static String getName(String value){
        return value.split(";")[0];
    }


    /**
     * Function used to prepare the dialog
     * Delete positive and negative buttons of the view
     * Set the title of the dialog with current preference title
     * @param builder view builder
     */
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
        builder.setTitle(getTitle());
    }

    /**
     * When clicked open a dialog containing the layout current profile
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

        profile = getValue();

        /**
         * Set title with title preference
         */
        EditText edName = (EditText) view.findViewById(R.id.currentProfileName);
        edName.setText(profile.getName());

        /**
         * Set value with value preference
         */
        EditText edUrl = (EditText) view.findViewById(R.id.currentProfileUrl);
        edUrl.setText(profile.getUrl());

        /**
         * Set title with title preference
         */
        EditText edTok = (EditText) view.findViewById(R.id.currentProfileToken);
        edTok.setText(profile.getToken());

        /**
         * Set value with value preference
         */
        EditText edPref = (EditText) view.findViewById(R.id.currentProfilePrefix);
        edPref.setText(profile.getPrefix());

        /**
         * Update current title and value of sensor preference
         */
        Button okButton = (Button) view.findViewById(R.id.profileOk);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) myView.findViewById(R.id.currentProfileName);

                EditText url = (EditText) myView.findViewById(R.id.currentProfileUrl);
                EditText token = (EditText) myView.findViewById(R.id.currentProfileToken);
                EditText prefix = (EditText) myView.findViewById(R.id.currentProfilePrefix);
                myValue = name.getText().toString().replaceAll(";","_") + ";" + url.getText().toString() + ";" +
                        token.getText().toString() + ";" + prefix.getText().toString().replaceAll(";","_") + ";" +
                        profile.getAllCheckedGts();
                saveValue(myValue, name.getText().toString().replaceAll(";", "_"));

                SharedPreferences sharedPreferences = PreferenceManager.
                        getDefaultSharedPreferences(getContext());
                sharedPreferences.edit().putString("url",url.getText().toString()).commit();
                sharedPreferences.edit().putString("token",token.getText().toString()).commit();
                sharedPreferences.edit().putString("prefix",prefix.getText().toString().
                        replaceAll(";", "_")).commit();
                sharedPreferences.edit().putString("checkedGTS",profile.getAllCheckedGts()).commit();
                getDialog().dismiss();
                Intent intent = new Intent(getContext(), WarpActivity.class);
                getContext().startActivity(intent);
            }
        });

        /**
         * Do nothing
         */
        Button cancelButton = (Button) view.findViewById(R.id.profileCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        /**
         * Delete current preference from preference screen and shared preference
         */
        Button deleteButton = (Button) view.findViewById(R.id.profileDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Add a dialog Are you sure ?
                getPreferenceManager().setSharedPreferencesName(ProfileFragment.NAME_SHARED_FILE_PROFILE);
                Preference preference = getPreferenceManager().findPreference(getKey());
                PreferenceScreen preferenceScreen = (PreferenceScreen) getPreferenceManager().findPreference("profilePrefScreen");
                preferenceScreen.removePreference(preference);
                getSharedPreferences().edit().remove(getKey()).commit();
                getDialog().dismiss();
            }
        });
    }

    /**
     * Get value of current preference in sharedPreference ("title [value]")
     * @return
     */
    protected Profile getValue() {
        //PreferenceManager.setDefaultValues(getContext(), getKey(),  Context.MODE_PRIVATE, R.xml.sensors, false);
        getPreferenceManager().setSharedPreferencesName(ProfileFragment.NAME_SHARED_FILE_PROFILE);
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        myValue = sp.getString(getKey(), mDefault);
        String[] tabValues = myValue.split(";");
        Profile profile = new Profile(tabValues);
        return profile;
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
        if(attr == null) {
            getPreferenceManager().setSharedPreferencesName(ProfileFragment.NAME_SHARED_FILE_PROFILE);
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            sharedPreferences.edit().putString(getKey(), value).commit();
            sharedPreferences.edit().putString(getKey(), value).apply();
            setTitle(title);
            //editor.apply();
            return;
        }

        getPreferenceManager().setSharedPreferencesName(ProfileFragment.NAME_SHARED_FILE_PROFILE);
        //SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        SensorPreference pref = (SensorPreference) getPreferenceManager().findPreference(getKey());
        pref.setTitle(title);
        //getPreferenceManager().setSharedPreferencesName(getKey());
        getEditor().putString(getKey(), value).commit();
        getEditor().putString(getKey(), value).apply();
        //this.setTitle(title);
        myValue = value;
        Log.d("SAVE", value);
    }

    /**
     * Function used to saveState, when an unwanted events happened
     */
    private static class SavedState extends Preference.BaseSavedState {
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
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        if(null!=(((SavedState) state).value)) {
            profile = new Profile((((SavedState) state).value).split(";"));
        } else {
            profile = null;
        }
    }

    /**
     * If there is no default value in shared Pref add current default value
     * @param restore
     * @param defaultValue
     */
    protected void onSetInitialValue(boolean restore, Object defaultValue)
    {
        myValue = (restore ? getPersistedString(mDefault) : (String) defaultValue);
        getPreferenceManager().setSharedPreferencesName(ProfileFragment.NAME_SHARED_FILE_PROFILE);
        if(!getSharedPreferences().contains(getKey())){
            getSharedPreferences().edit().putString(getKey(),myValue).commit();
        }
    }
}
