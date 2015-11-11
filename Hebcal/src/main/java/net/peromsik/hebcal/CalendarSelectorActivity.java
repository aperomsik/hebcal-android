/**
 * 
 */
package net.peromsik.hebcal;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class CalendarSelectorActivity extends PreferenceActivity {
	

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (CalEventLoader.cal_info == null)
	    //    CalEventLoader.cal_info = CalEventLoader.getCalendars();
		if (CalEventLoader.cal_info == null)
			return;
		
		PreferenceScreen cal_screen = getPreferenceManager().createPreferenceScreen(this.getApplicationContext());
        int i_cal;
		for (i_cal = 0; i_cal < CalEventLoader.cal_info.length; i_cal ++) {
			CheckBoxPreference p = new CheckBoxPreference(this);
			p.setKey("using"+CalEventLoader.cal_info[i_cal].cal_id);
			//p.setTitle("using"+CalEventLoader.cal_info[i_cal].cal_id);
			p.setTitle(CalEventLoader.cal_info[i_cal].displayName);
			cal_screen.addPreference(p);
		}
        setPreferenceScreen(cal_screen);
    }
	
}