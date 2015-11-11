package net.peromsik.hebcal;

import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class HebcalPrefsActivity extends PreferenceActivity
       implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private ListPreference location;
	private ListPreference dialect;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 	
		prefs.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.prefs);
        
        HebcalNativeLoader nl = new HebcalNativeLoader();
     	String[] hebcal_cities = nl.getCities();
     	String cur_city = prefs.getString("Location", nl.getCurCity());
        
        location = (ListPreference) findPreference("Location");
        location.setEntries(hebcal_cities);
        location.setEntryValues(hebcal_cities);
        if (cur_city != null) {
          location.setValue(cur_city);
          location.setSummary(cur_city);
        }

        dialect = (ListPreference) findPreference("Dialect"); 
        updateDialectSummary();
    }

	public void onDestroy() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 	
		prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    	if (key.contentEquals("Location")) {
    		location.setSummary(location.getValue());
    	}
    	if (key.contentEquals("Dialect")) {
    		updateDialectSummary();
    	}
    }
    
    private void updateDialectSummary() {
        String cur_val = dialect.getValue();
        if (cur_val != null) {
        	CharSequence [] labels = dialect.getEntries();
        	int i = dialect.findIndexOfValue(cur_val);
        	if (i >= 0 && i < 3) {
        	  dialect.setSummary(labels[i]);
        	}
        }
    }   
}
