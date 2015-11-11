
package net.peromsik.hebcal;

import android.content.ContentValues;
import android.text.format.Time;

public class HebcalNativeLoader  {

  static {
    System.loadLibrary("hebcal");
  }
  
  private ContentValues[] events;
  private String[] cities = null;
  private String cur_city = null;

  public native void hebcal_set_date( int mm, int dd, int yy );
  
  public native void hebcal_set_prefs( boolean daf, boolean sunrise, boolean sunset);
  
  public native void hebcal_range_events(int num_days);

  public native void hebcal_get_cities();
  
  public native void hebcal_localize_to_city(String city);
  public native void hebcal_set_dialect(String dialect);

  /**
   * Returns Hebcal string
   */
  public native String hebcal(int mode);
  
  public void setEvent(int i, int n, 
		  int mm, int dd, int yy, int hh, int min, 
		  int daf, String desc) {
	  if (i == 0) {
		  events = new ContentValues[n];
	  }
	  events[i] = new ContentValues();
	  events[i].put("title",desc);
	  Time t = new Time();
	  long t_millis;
	  t.year = yy;
	  t.month = mm - 1;
	  t.monthDay = dd;
	  t.hour = hh;
	  t.minute = min;
	  t_millis = t.normalize(false);
	// seems events and instances are not quite the same. pretend they are instances for now.
	//  events[i].put("dtstart", t_millis);
	//  events[i].put("dtend", t_millis);
		events[i].put("begin", t_millis);
		events[i].put("end", t_millis);
	  events[i].put("allDay", hh == 0 && min == 0);
	  
  }

  public void setCity(int i, int n, String city) {
	  if (i == -1) {
	      cur_city = city;
	      return;
	  }
	  if (i == 0) {
  	      cities = new String[n];
	  }
	  cities[i] = city;	  
  }
  
  public ContentValues[] getEvents() {	  
	  return events;
  }

  public String getCurCity() {
	  if (cities == null)
		  hebcal_get_cities();  
      return cur_city;
  }
  public String[] getCities() {
	  if (cities == null)
		  hebcal_get_cities();  
      return cities;
  }
  
}