package org.radiation_watch.pocketgeigertype6.preference;

import org.radiation_watch.pocketgeigertype6.R;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class CalibrationPrefActivity extends PreferenceActivity {

	static final String PREF_KEY_CALIBRATION_COEFFICIENT = "calibration_coefficient";
	static final String PREF_KEY_CALIBRATION_OFFSET = "calibration_offset";

	public CalibrationPrefActivity() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, new CalibrationFragment());
        ft.commit();
    }
    
	public static class CalibrationFragment extends PreferenceFragment{
		
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_calibration);
			
			EditTextPreference coefficientPref = (EditTextPreference) findPreference(PREF_KEY_CALIBRATION_COEFFICIENT);
			if (coefficientPref.getText() == null) {
				coefficientPref.setSummary("53.032");
			}else if ("".equals(coefficientPref.getText())) {
				coefficientPref.setSummary("53.032");
			}else{
				coefficientPref.setSummary(coefficientPref.getText());
			}
			EditTextPreference offsetPref = (EditTextPreference) findPreference(PREF_KEY_CALIBRATION_OFFSET);
			if (offsetPref.getText() == null) {
				offsetPref.setSummary("0.0");
			}else if ("".equals(offsetPref.getText())) {
				offsetPref.setSummary("0.0");
			}else{
				offsetPref.setSummary(offsetPref.getText());

			}
		}
		
		private OnSharedPreferenceChangeListener onPreferenceChangeListenter = new OnSharedPreferenceChangeListener() {
	        @Override
	        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	            
	        	if (key.equals(PREF_KEY_CALIBRATION_COEFFICIENT)) {
	    			EditTextPreference coefficientPref = (EditTextPreference) findPreference(PREF_KEY_CALIBRATION_COEFFICIENT);
	    			coefficientPref.setSummary(coefficientPref.getText());

	            } else if (key.equals(PREF_KEY_CALIBRATION_OFFSET)) {
	    			EditTextPreference offsetPref = (EditTextPreference) findPreference(PREF_KEY_CALIBRATION_OFFSET);
	    			offsetPref.setSummary(offsetPref.getText());
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
