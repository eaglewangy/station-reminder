package com.wy.locationalarm;

public class Constants 
{
	public static String LOG = "Location Alarm";
	
	public final static int SUBWAY = 0;
	public final static int TRAIN = 1;
	public final static int CUSTOM_HOTSPOT = 2;
	
	//  120km/h == 33.33m/s
	public final static int SUBWAY_SPEED = 34;
	
	//The Android's default system path of your application database.
	public static String DB_PATH = "/data/data/com.autodesk.locationalarm/databases/";
	public static String DB_NAME = "locationalarm.db";
	
	public static final String TYPE_TABLE = "TYPE";
	public static final String CITY_TABLE = "CITY";
	public static final String SUBWAY_TABLE = "SUBWAY";
	public static final String HOTSPOT_TABLE = "HOTSPOT";
	public static final String TRAIN_TABLE = "TRAIN";
	
	//column name in table
	public static final String TYPE_COLUMN = "type";
	public static final String LINE_COLUMN = "line";
	public static final String HASSUBWAY_COLUMN = "hassubway";
	public static final String STATION_COLUMN = "station";
	public static final String CITYNAME_COLUMN = "cityname";
	public static final String CITYID_COLUMN = "cityid";
	public static final String ID_COLUMN = "_id";
	
	//column index in table
	public static final int ID_IDX = 0;
	public static final int TYPE_IDX = 1;
	public static final int PROVINCE_IDX = 1;
	public static final int CITYNAME_IDX = 1;
	public static final int LINE_IDX = 2;
	public static final int STATION_IDX = 3;
	public static final int LAT_IDX = 4;
	public static final int LNG_IDX = 5;
	
	//column index in table hotspot
	public static final int HS0 = 0;
	public static final int HS1 = 1;
	public static final int HS2 = 2;
	public static final int HS3 = 3;
	public static final int HS4 = 4;
	public static final int HS5 = 5;
	public static final int HS6 = 6;
	public static final int HS7 = 7;
	public static final int HS8 = 8;
	public static final int HS9 = 9;
	
	public static final String HS_CITY = "city";
	public static final String HS_CHECKED = "checked";
	public static final String HS_LAT = "lat";
	public static final String HS_LNG = "lng";
	public static final String HS_PROVINCE = "province";
	public static final String HS_DISTANCE = "distance";
	public static final String HS_SPEED = "speed";
	
	//setup
	public static final int VIBRATE_IN_LIST = 0;
	public static final int RADIUS_IN_LIST = 1;
	public static final int RINGTONE_IN_LIST = 2;
	
	
	//intent args
	public static final String DISPLAY_BUTTON = "displaybutton";
	public static final String BUNDLE = "bundle";
	
	//for preference
	public static final String USER_PREFERENCE = "USER_PREFERENCE";
	public static final String PREF_VIBRATE = "PREF_VIBRATE";
	public static final String PREF_RADIUS = "PREF_RADIUS";
	public static final String PREF_RINGTONE = "PREF_RINGTONE";
	public static final String PERF_RINGTONE_CHECKEDITEM = "PERF_RINGTONE_CHECKEDITEM";
	public static final String PERF_RINGTONE_URI = "PERF_RINGTONE_URI";
	public static final String PERF_DEFALUT_RINGTONE_URI = "android.resource://com.autodesk.locationalarm/raw/igotalove";
	public static final String PERF_CURRENT_RINGTONE = "PERF_CURRENT_RINGTONE";
	public static final String PREF_SUBWAYCITY = "PREF_SUBWAYCITY";
	
	public static final String PREF_GPS_STATUS = "PREF_GPS_STATUS";
	
	public static final String PREF_DISTANCE_SPEED = "PREF_DISTANCE_SPEED";
	
	public static final String PREF_LAT = "PREF_LAT";
	public static final String PREF_LNG = "PREF_LNG";
	public static final String PREF_ADDRESS = "PREF_ADDRESS";
	
	public static final String PREF_FIRST_START = "PREF_FIRST_START";
	
	public static final String PREF_CURRENT_CITY = "PREF_CURRENT_CITY";
	
	public static final String PREF_SEARCH_FROM = "PREF_SEARCH_FROM";
	public static final String PREF_SEARCH_FROM_SUBWAY = "PREF_SEARCH_FROM_SUBWAY";
	public static final String PREF_SEARCH_FROM_TRAIN = "PREF_SEARCH_FROM_TRAIN";
	public static final String PREF_SEARCH_FROM_BOTH = "PREF_SEARCH_FROM_BOTH";
	
	public static final String PREF_SLEEP_TIME = "PREF_SLEEP_TIME";
	
}
