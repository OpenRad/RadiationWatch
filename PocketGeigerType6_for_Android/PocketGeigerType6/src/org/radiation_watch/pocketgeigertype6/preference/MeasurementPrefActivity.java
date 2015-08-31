package org.radiation_watch.pocketgeigertype6.preference;

import org.radiation_watch.pocketgeigertype6.R;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class MeasurementPrefActivity extends PreferenceActivity {

	static final String PREF_KEY_MEASUREMENTTIME_LIST = "measurementtime_list";

	public MeasurementPrefActivity() {
		// TODO
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, new MeasurementFragment());
        ft.commit();
    }
    
	public static class MeasurementFragment extends PreferenceFragment{
		
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_measurement);
			
			ListPreference baudratePref = (ListPreference) findPreference(PREF_KEY_MEASUREMENTTIME_LIST);
			baudratePref.setSummary(baudratePref.getEntry());
			
		}
		
		private OnSharedPreferenceChangeListener onPreferenceChangeListenter = new OnSharedPreferenceChangeListener() {
	        @Override
	        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	            if (key.equals(PREF_KEY_MEASUREMENTTIME_LIST)) {
	    			ListPreference baudratePref = (ListPreference) findPreference(PREF_KEY_MEASUREMENTTIME_LIST);
	    			baudratePref.setSummary(baudratePref.getEntry());
	            }
	        }
	    };
		
	    @Override
	    public void onResume() {
	        super.onResume();
	        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	        sharedPreferences.registerOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
	    }
	     
	    @Override
	    public void onPause() {
	        super.onPause();
	        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
	    }
		
	}
}
