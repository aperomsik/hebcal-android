/**
 * 
 */
package net.peromsik.hebcal;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;

class CalEventLoader implements SharedPreferences.OnSharedPreferenceChangeListener {
	Hebcal hebcal;

	/**
	 * @param hebcal
	 */
	void setContext(Hebcal hebcal) {
		this.hebcal = hebcal;
	}

	private int apivers = Integer.parseInt(android.os.Build.VERSION.SDK);
	ContentResolver contentResolver;
	public boolean registered = false;
	
	int num_cals;
    class CalendarInfo {
    	public int cal_id;
    	public int color;
    	public String displayName;
    	public boolean in_use;
    }

	static CalendarInfo [] cal_info = null;
	
	// public CalEventLoader() {
	//  super();
	//}
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)  {
		
		if (key.matches("using.*")) {
			cal_info = null;
		}
	}
	
     public void cleanup() {
    	 if (registered) {
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context)this.hebcal); 	
	        prefs.unregisterOnSharedPreferenceChangeListener(this);
    	 }
     }
	
	public int getCalColor (int cal_id) {
		int i_cal;
		
	    if (cal_info == null)
	        cal_info = getCalendars();
		if (cal_info == null)
			return 0;
		
		for (i_cal = 0 ; i_cal < cal_info.length; i_cal ++) {
			int this_id = cal_info[i_cal].cal_id;
			if (this_id == cal_id)
			{
				int color = cal_info[i_cal].color;
				return color;
			}	
		}
		return 0;
	}
	public boolean using_calendar(int cal_id) {
	int i_cal;
		
	    if (cal_info == null)
	        cal_info = getCalendars();
		if (cal_info == null)
			return false;
		
		for (i_cal = 0 ; i_cal < cal_info.length; i_cal ++) {
			int this_id = cal_info[i_cal].cal_id;
			if (this_id == cal_id)
			{
				return cal_info[i_cal].in_use;
			}	
		}
		return false;   		
	}
	
	private Uri getCalendarContentUri(String table) {
		// with clue from astrid
	    if(apivers >= android.os.Build.VERSION_CODES.FROYO) // Froyo moved it
	        return Uri.parse("content://com.android.calendar/" + table);
	    else
	        return Uri.parse("content://calendar/" + table);
	}
	
	public void viewEvent(Context context, String eventId) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(getCalendarContentUri("events/" + eventId));  
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		        | Intent.FLAG_ACTIVITY_SINGLE_TOP
		        | Intent.FLAG_ACTIVITY_CLEAR_TOP
		        | Intent.FLAG_ACTIVITY_NO_HISTORY
		        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		context.startActivity(intent);
	}
	
    public CalendarInfo [] getCalendars () {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context)this.hebcal); 	
    	if (!registered)
    	{
    		prefs.registerOnSharedPreferenceChangeListener(this);
    		registered = true;
    	}
    	Context c = this.hebcal;
    	contentResolver = c.getContentResolver();
    	Uri my_uri = getCalendarContentUri("calendars");
    	String displayName = "displayName";
    	String color = "color";
    	if (apivers >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) { 
    		displayName = "calendar_displayName";
    		color = "calendar_color";
    	}
    	Cursor calsCursor = contentResolver.query(my_uri, new String[] {"_id", displayName, color }, 
    			null, null, displayName);
    	int i = 0;
    	CalendarInfo [] info_arr = new CalendarInfo[calsCursor.getCount()];;
    	while(calsCursor.moveToNext()) {
		
			ContentValues vals = new ContentValues();
			CalendarInfo info = new CalendarInfo();
			DatabaseUtils.cursorRowToContentValues(calsCursor, vals);
			info.cal_id = vals.getAsInteger("_id");
			Integer try_color = vals.getAsInteger(color);
			if (try_color != null)
			   info.color = try_color;
			info.displayName = vals.getAsString(displayName);
			info.in_use = prefs.getBoolean("using" + info.cal_id, true);
			info_arr[i] = info;
			i++;
		}
		
		return info_arr;
    }
	
	public ContentValues [] getCalendarEventsInRange(Context context, 
			long startmillis, long endmillis) {
		// clue from http://svn.jimblackler.net/jimblackler/trunk/workspace/AndroidReadCalendarExample/src/net/jimblackler/readcalendar/Example.java
		Uri.Builder uribuilder = getCalendarContentUri("instances/when").buildUpon();
		ContentUris.appendId(uribuilder, startmillis);
		ContentUris.appendId(uribuilder, endmillis);
		contentResolver = context.getContentResolver();
	
		Uri my_uri = uribuilder.build();
		Cursor eventCursor = contentResolver.query(my_uri,
				new String[] { "title", "begin", "end", "allDay", "calendar_id", "event_id"}, null,
				null, "startDay ASC, startMinute ASC"); 
		if (eventCursor == null)
			return new ContentValues[0];
		ContentValues [] vals_arr = new ContentValues[eventCursor.getCount()];
		int i = 0;
		while(eventCursor.moveToNext()) {
		
			ContentValues vals = new ContentValues();
			DatabaseUtils.cursorRowToContentValues(eventCursor, vals);
			int cal_id = vals.getAsInteger("calendar_id");
			if (!using_calendar(cal_id))
				continue;
			vals_arr[i] = vals;
			i++;
		}
		
		return vals_arr;
	}
}