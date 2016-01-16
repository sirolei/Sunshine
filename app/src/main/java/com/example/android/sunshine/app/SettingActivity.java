package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by sansi on 2016/1/14.
 */
public class SettingActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (savedInstanceState == null){
            getFragmentManager().beginTransaction().add(R.id.container, new PlaceHolderFragment()).commit();
        }
    }


    public static class PlaceHolderFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        public PlaceHolderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.setting);
            Resources res = getResources();
            SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location_key = res.getString(R.string.pref_location_key);
            String location_default = spf.getString(location_key, res.getString(R.string.pref_location_defalut));
            String unit_key = res.getString(R.string.pref_pick_units_key);
            String unit_default = spf.getString(unit_key, res.getString(R.string.pref_pick_units_default));
            bindSettingPreference(findPreference(location_key), findPreference(unit_key));
            onPreferenceChange(findPreference(location_key), location_default);
            onPreferenceChange(findPreference(unit_key), unit_default);
        }

        private void bindSettingPreference(Preference... preference){
            for(Preference pref : preference){
                pref.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if  (preference instanceof ListPreference){
                ListPreference pref = (ListPreference) preference;
                int valueIndex = pref.findIndexOfValue(newValue.toString());
                if (valueIndex >= 0){
                    pref.setValue(newValue.toString());
                    pref.setSummary(pref.getEntries()[valueIndex].toString());
                }
            }

            if (preference instanceof EditTextPreference){
                EditTextPreference editPref = (EditTextPreference) preference;
                editPref.setSummary(newValue.toString());
            }
            return true;
        }
    }
}
