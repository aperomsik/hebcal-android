package net.peromsik.hebcal;

import android.preference.EditTextPreference;
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
    private EditTextPreference candles;
    private EditTextPreference havdallah;
	
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

        candles = (EditTextPreference) findPreference("candlesOffset");
        updateCandlesTitle();
        havdallah = (EditTextPreference) findPreference("havdallahMinutes");
        updateHavdallahTitle();
    }

    private void updateHavdallahTitle() {
        String cur_val = havdallah.getText();
        String title = getString(R.string.title_havdallah);
        if (cur_val != null)
          title += ": " + cur_val;
        havdallah.setTitle(title);
    }

    private void updateCandlesTitle() {
        String cur_val = candles.getText();
        String title = getString(R.string.title_candles);
        if (cur_val != null)
            title += ": " + cur_val;
        candles.setTitle(title);
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
        if (key.contentEquals("candlesOffset")) {
            String cur_val = candles.getText();
            int minutes = 0;
            try {
                minutes = Integer.parseInt(cur_val);
            } catch (NumberFormatException e) {

            }
            if (minutes < 18) {
                SharedPreferences.Editor e = prefs.edit();
                e.remove(key);
                e.commit();
                candles.setText("18");
            }
            updateCandlesTitle();
        }
        if (key.contentEquals("havdallahMinutes")) {
            String cur_val = havdallah.getText();
            int minutes = 0;
            try {
                minutes = Integer.parseInt(cur_val);
            } catch (NumberFormatException e) {

            }
            if (minutes < 42) {
                SharedPreferences.Editor e = prefs.edit();
                e.remove(key);
                e.commit();
                havdallah.setText("42");
            }
            updateHavdallahTitle();
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
