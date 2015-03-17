package com.samsonan.android.percussionstudio.components;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.samsonan.android.percussionstudio.R;

public class SettingsActivity extends Activity {

    public static String KEY_PREF_UI_MODE = "pref_ui_mode";
    public static String KEY_PREF_AUTO_ADD_BAR = "pref_auto_add_bar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment  {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }


    }
}


