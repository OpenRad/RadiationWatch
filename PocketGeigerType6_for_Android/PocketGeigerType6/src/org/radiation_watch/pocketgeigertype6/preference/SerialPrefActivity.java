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

public class SerialPrefActivity extends PreferenceActivity {

	static final String PREF_KEY_BAUDRATE_LIST = "baudrate_list";
	static final String PREF_KEY_DATABITS_LIST = "databits_list";
	static final String PREF_KEY_PARITY_LIST = "parity_list";
	static final String PREF_KEY_STOPBITS_LIST = "stopbits_list";
	static final String PREF_KEY_FLOWCONTROL_LIST = "flowcontrol_list";
	
	public SerialPrefActivity() {
		// TODO
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, new SerialFragment());
        ft.commit();
    }
    
	public static class SerialFragment extends PreferenceFragment{
		
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_serial);
			
			ListPreference baudratePref = (ListPreference) findPreference(PREF_KEY_BAUDRATE_LIST);
			baudratePref.setSummary(baudratePref.getEntry());
			
			ListPreference databitsPref = (ListPreference) findPreference(PREF_KEY_DATABITS_LIST);
			databitsPref.setSummary(databitsPref.getEntry());
			
			ListPreference parityPref = (ListPreference) findPreference(PREF_KEY_PARITY_LIST);
			parityPref.setSummary(parityPref.getEntry());
			
			ListPreference stopbitsPref = (ListPreference) findPreference(PREF_KEY_STOPBITS_LIST);
			stopbitsPref.setSummary(stopbitsPref.getEntry());
			
			ListPreference flowcontrolPref = (ListPreference) findPreference(PREF_KEY_FLOWCONTROL_LIST);
			flowcontrolPref.setSummary(flowcontrolPref.getEntry());
		}
		
		private OnSharedPreferenceChangeListener onPreferenceChangeListenter = new OnSharedPreferenceChangeListener() {
	        @Override
	        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	            if (key.equals(PREF_KEY_BAUDRATE_LIST)) {
	    			ListPreference baudratePref = (ListPreference) findPreference(PREF_KEY_BAUDRATE_LIST);
	    			baudratePref.setSummary(baudratePref.getEntry());

	            } else if (key.equals(PREF_KEY_DATABITS_LIST)) {
	    			ListPreference databitsPref = (ListPreference) findPreference(PREF_KEY_DATABITS_LIST);
	    			databitsPref.setSummary(databitsPref.getEntry());

	            } else if (key.equals(PREF_KEY_PARITY_LIST)) {
	    			ListPreference parityPref = (ListPreference) findPreference(PREF_KEY_PARITY_LIST);
	    			parityPref.setSummary(parityPref.getEntry());

	            } else if (key.equals(PREF_KEY_STOPBITS_LIST)) {
	    			ListPreference stopbitsPref = (ListPreference) findPreference(PREF_KEY_STOPBITS_LIST);
	    			stopbitsPref.setSummary(stopbitsPref.getEntry());

	            } else if (key.equals(PREF_KEY_FLOWCONTROL_LIST)) {
	    			ListPreference flowcontrolPref = (ListPreference) findPreference(PREF_KEY_FLOWCONTROL_LIST);
	    			flowcontrolPref.setSummary(flowcontrolPref.getEntry());
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

