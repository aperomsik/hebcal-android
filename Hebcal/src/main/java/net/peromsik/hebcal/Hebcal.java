package net.peromsik.hebcal;


import java.util.Calendar;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;


import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.PopupMenu;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.Toast;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.text.method.ScrollingMovementMethod;
import android.content.ContentValues;
import android.text.format.Time;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
//import android.graphics.Typeface;
import android.text.format.DateUtils;


public class Hebcal extends ActionBarActivity {
    static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;

    private Boolean as_table = true;

    private TextView mHebcalText;
    private TableLayout mHebcalTable;
    private Button mDateButton;
    private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

    CalEventLoader cel;
    HebcalEventLoader hevl = new HebcalEventLoader();
    
	private String hebcalTextString = "";
	
    private int mode = 0;
    private int num_days = 1;
    String mPrevDate = null;
        
    private class HebcalEventLoader 
        implements SharedPreferences.OnSharedPreferenceChangeListener {
	     HebcalNativeLoader nl;
	     public Boolean need_to_apply_prefs = true;
	     Boolean registered = false;
	     String[] hebcal_cities;

	     
	     public HebcalEventLoader () {
	     	nl = new  HebcalNativeLoader();
	     }
	     
	     public void cleanup() {
	    	 if (registered) {
		        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context)Hebcal.this); 	
		        prefs.unregisterOnSharedPreferenceChangeListener(this);
	    	 }
	     }
	     
	    void applyPrefs () {
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context)Hebcal.this); 	
            
	    	if (!registered)
	    	{
	    		prefs.registerOnSharedPreferenceChangeListener(this);
	    		registered = true;
	    	}
	    	
	    	Boolean daf_sw = prefs.getBoolean("showDaf", true);
            Boolean sunrise_sw = prefs.getBoolean("showSunrise", true);
            Boolean sunset_sw = prefs.getBoolean("showSunset", true);
            String dialect = prefs.getString("Dialect", "a");
            String native_cur_city = nl.getCurCity();
            String pref_cur_city = prefs.getString("Location", native_cur_city);
            
            nl.hebcal_set_prefs(daf_sw, sunrise_sw, sunset_sw);
            if (pref_cur_city != null)
               nl.hebcal_localize_to_city(pref_cur_city);
        	nl.hebcal_set_dialect(dialect);

            need_to_apply_prefs = false;
	    }
	    
	     
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        	// eventually apply just the changed one, but for now...
            need_to_apply_prefs = true;
            // best if we can avoid calling this until activity is visible again.
            // Hebcal.this.updateDisplay();
            
            //if (key.contentEquals("Location")) {
            //	nl.hebcal_localize_to_city(prefs.getString("Location", ""));
            //}
            	
        }
        
        public ContentValues [] getEvents ()
        {
           nl.hebcal_set_date(mMonth+1, mDay, mYear);
           if (need_to_apply_prefs)
        	   applyPrefs();
           nl.hebcal_range_events(1 + 6*mode);
           return nl.getEvents();
        }
    }
    
    public class HebcalModeOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         // Toast.makeText(parent.getContext()), parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
        	mode = pos;
        	updateDisplay();
        }

        public void onNothingSelected(AdapterView<?> parent) {
          // Do nothing.
        }
    }
    
    private void gotoToday () {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay();
    }
    
    private void onRangeModeChanged(int pos, boolean display) {
    	Resources res = getResources();
    	int [] modeValues = res.getIntArray(R.array.ModeValues);
    	String [] modeStrings = res.getStringArray(R.array.ModeStrings);
    	mode = pos;
    	num_days = modeValues[mode];
		ActionBar ab = getSupportActionBar();
		if (ab != null)
		  	ab.setSubtitle(modeStrings[pos]);
    	if (display)
          updateDisplay();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	
        cel = new CalEventLoader();
        cel.setContext(this);
       
        mHebcalText = (TextView) findViewById(R.id.text);
        mHebcalTable = (TableLayout) findViewById(R.id.Table);
        mDateButton = (Button) findViewById(R.id.pickDate);

        mHebcalText.setMovementMethod(new ScrollingMovementMethod());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context)Hebcal.this); 	
    	mode = prefs.getInt("range_mode", 0);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		Toolbar t = (Toolbar) findViewById(R.id.toolbar);
		if (t != null) {
			setSupportActionBar(t);
			ActionBar ab = getSupportActionBar();
			t.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDrawerLayout.openDrawer(GravityCompat.START);
				}
			});
			ab.setDisplayHomeAsUpEnabled(true);

			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {

				public void onDrawerClosed(View view) {
					supportInvalidateOptionsMenu();
					//drawerOpened = false;
				}

				public void onDrawerOpened(View drawerView) {
					supportInvalidateOptionsMenu();
					//drawerOpened = true;
				}
			};
			mDrawerToggle.setDrawerIndicatorEnabled(true);
			mDrawerLayout.setDrawerListener(mDrawerToggle);
		}

		onRangeModeChanged(mode, false);

		NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				Intent intent;
				mDrawerLayout.closeDrawers();

				switch (menuItem.getItemId()) {
					case R.id.CalButton:
						intent = new Intent(Hebcal.this, CalendarSelectorActivity.class);
						startActivity(intent);
						break;

					case R.id.PrefsButton:
						intent = new Intent(Hebcal.this, HebcalPrefsActivity.class);
						startActivity(intent);
						break;

					case R.id.Mode0:
						mode = 0;
						onRangeModeChanged(mode, false);
						break;

					case R.id.Mode1:
						mode = 1;
						onRangeModeChanged(mode, false);
						break;

					case R.id.Mode2:
						mode = 2;
						onRangeModeChanged(mode, false);
						break;

					case R.id.Mode3:
						mode = 3;
						onRangeModeChanged(mode, false);
						break;
				}
				return true;
			}
		});
        
        gotoToday();
        
        mDateButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });   
        mDateButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				gotoToday();
				Toast.makeText(getApplicationContext(), "Back to the present.", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

    public void onDestroy() {
    	hevl.cleanup();
    	cel.cleanup();
    	super.onDestroy();
    }
    
    public void onResume() {
    	if (hevl != null && hevl.need_to_apply_prefs)
    		updateDisplay();
    	super.onResume();
    }
    
    public void onStop() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context)Hebcal.this); 	
    	SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("range_mode", mode);
        editor.commit();
        super.onStop();
    }

    private void addAppt() {
            	// help from http://stackoverflow.com/questions/2911731/intent-that-opens-new-calendar-event-activity
            	Time start = new Time();
                start.monthDay = mDay;
                start.month = mMonth;
                start.year = mYear;
                long startmillis = start.normalize(false);
                long endmillis = startmillis + DateUtils.HOUR_IN_MILLIS;
            	Intent intent = new Intent(Intent.ACTION_EDIT);
            	intent.setType("vnd.android.cursor.item/event");
            	// intent.putExtra("title", "Some title");
            	// intent.putExtra("description", "Some description");
            	intent.putExtra("beginTime", startmillis);
            	intent.putExtra("endTime", endmillis);
            	startActivity(intent);
    }
    
    public boolean onCreateOptionsMenu(Menu m){
    	 MenuInflater inflater = getMenuInflater();
    	 inflater.inflate(R.menu.mainmenu, m);

    	return super.onCreateOptionsMenu(m);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.AddApptButton:
            addAppt();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                            mDateSetListener,
                            mYear, mMonth, mDay);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }    

    private String eventString (ContentValues event) {
    	String text = new String();
    	Boolean allDay = event.getAsBoolean("allDay");
    	long time_millis = event.getAsLong("begin");
    	long end_millis = event.getAsLong("end");
    	Time t = new Time();
    	t.set(time_millis);
    	if (allDay) {
    		t.set(time_millis - t.gmtoff);
    	}
    	if (mode != 0) {
    		text += t.format("%a %d-%b");
    		//text += ": ";
    	}
    		
    	if (allDay == false && end_millis - time_millis == DateUtils.DAY_IN_MILLIS) {
    		allDay = true;
    		// should recast it to midnight gmt, and should do this when reading from calendar, 
    		// and should resort.
    	}
   	
    	
    	if (! allDay) {
      	  text += t.format("%I:%M %p");
      	  text = text.substring(0, text.length() - 1) + " - ";
      	} else if (mode != 0) {
      		text += " - ";
      	}
    	text += event.getAsString("title");
    	text += "\n";
    	return text;
    }

    private boolean isDark(int color) {
    	// http://en.wikipedia.org/wiki/Luminance_(relative)
    	double luminance = 0.2126 * Color.red(color)+ 0.7152 * Color.green(color) + 0.0722 * Color.blue(color);

    	return (luminance < 128);
    }
    
    
    private void addEventTableRow (ContentValues event, int color, boolean fromCal) {
    	
    	TableRow row = new TableRow(getApplicationContext());
    	TextView tview;
    	int textColor = isDark(color) ? Color.LTGRAY : Color.DKGRAY;
    	
    	String text = new String();
    	
    	Boolean allDay = event.getAsBoolean("allDay");
    	long time_millis = event.getAsLong("begin");
    	long end_millis = event.getAsLong("end");

    	Time t = new Time();
    	t.set(time_millis);
    	
    	
    	if (allDay == false && end_millis - time_millis == DateUtils.DAY_IN_MILLIS) {
    		allDay = true;
    		// should recast it to midnight gmt, and should do this when reading from calendar, 
    		// and should resort.
    		t.set(time_millis - 1000 * t.gmtoff);
    	}    	
    	if (mode != 0) {
    		text = t.format("%a %d-%b ");
        	tview = new TextView(getApplicationContext());        	
        	tview.setText(text);
        	if (text.equals(mPrevDate)) {
        		// make this configurable?
        		tview.setTextColor(color);
        	} else {
        		TextView border = new TextView(getApplicationContext());
        		border.setHeight(5);
        		tview.setTextColor(textColor);
        		//border.setBackgroundColor(color);
        		mHebcalTable.addView(border);
        	}
        	mPrevDate = text;
    		row.addView(tview);
    	}
    	
    	if (allDay) {
          text = "";
    	} else {
      	  text = t.format("%I:%M%p");
      	  text = text.substring(0, text.length() - 1) + " ";
    	}
    	
      	tview = new TextView(getApplicationContext());        	
      	tview.setText(text);
		tview.setTextColor(textColor);
      	row.addView(tview);
      	 
    	text = event.getAsString("title");
    	tview = new TextView(getApplicationContext());        	
    	tview.setText(text);
		tview.setTextColor(textColor);
    	if (color != 0)
    	{
    	   row.setBackgroundColor(color);
    	   //tview.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    	}
    	if (fromCal) {
    		tview.setTag(event.getAsString("event_id"));
    		tview.setOnClickListener(new View.OnClickListener() {

    			public void onClick(View v) {
    				String event_id = (String) v.getTag();
    				cel.viewEvent(Hebcal.this, event_id);
//    				ViewGroup.LayoutParams lp = v.getLayoutParams();
//           	
//    				v.setLayoutParams(new TableRow.LayoutParams(
//    						TableRow.LayoutParams.WRAP_CONTENT,
//    						TableRow.LayoutParams.WRAP_CONTENT, ((TableRow.LayoutParams)lp).weight == 1 ? 0 : 1));
//     
//    				v.requestLayout();
            }
        });   
    	}
    	row.addView(tview);
    	mHebcalTable.addView(row);
    }
    
    private void addEventToDisplay (ContentValues event, int color, boolean fromCal) {
    	if (as_table)
    	{
         	addEventTableRow(event, color, fromCal); 
         	// addEventTableRow(event);
         	// addEventTableRow(event);

         	// addEventTableRow(event);

    	}
    	else
    		hebcalTextString += eventString(event);
    	
    }
    
    private void updateDisplay() {
    	//nl.hebcal_set_date(mMonth+1, mDay, mYear);
    	//String hebcalText = nl.hebcal(mode); 
        //mHebcalText.setText(hebcalText);
        
    	Time start = new Time();
        start.monthDay = mDay;
        start.month = mMonth;
        start.year = mYear;
        long startmillis = start.normalize(false);
        
        long endmillis = startmillis + (num_days) * DateUtils.DAY_IN_MILLIS;
        
    	ContentValues[] cal_events = cel.getCalendarEventsInRange(this, startmillis, endmillis);
        ContentValues[] hc_events = hevl.getEvents();
    	int num_cal, num_hc, i_cal, i_hc;     
        
    	num_cal = cal_events.length;
    	num_hc = (hc_events == null) ? 0 : hc_events.length;
    	long next_hc;
    	long next_cal;
    	long future = 3000 * DateUtils.YEAR_IN_MILLIS;
    	
        hebcalTextString = "";
        mHebcalTable.removeAllViews();
   	        
    	for (i_cal = 0, i_hc = 0; i_cal < num_cal || i_hc < num_hc; ) {
    		if (i_cal == num_cal || cal_events[i_cal] == null)
    			next_cal = future;
    		else
    			next_cal = cal_events[i_cal].getAsLong("begin");
    		if (i_hc == num_hc)
    			next_hc = future;
    		else
    			next_hc = hc_events[i_hc].getAsLong("begin");
    		
    		if (next_hc < next_cal) {
    			addEventToDisplay(hc_events[i_hc], 0xff000040, false);
    			i_hc ++;
    		} else {
    			if (cal_events[i_cal] != null) {    		
    	    	  int cal_id = cal_events[i_cal].getAsInteger("calendar_id");
    	    	  int color = cel.getCalColor(cal_id);
    			  addEventToDisplay(cal_events[i_cal], color, true);
    			}
    			i_cal ++;
    		}
    	}
    	
    
        mHebcalText.setText(hebcalTextString);
        mHebcalTable.requestLayout();
       
        
        mDateButton.setText(start.format("%a %d-%b-%Y"));
            //new StringBuilder()
                    // Month is 0 based so add 1
                    //.append(mMonth + 1).append("-")
            //		.append(mDay).append("-")
             //       .append(DateUtils.getMonthString(mMonth, DateUtils.LENGTH_MEDIUM)).append("-")
            //        .append(mYear).append(" ")
        	//);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

}
