package com.wy.locationalarm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter
{
	private static final String DATABASE_TABLE = "subway";

	public static final String LOCATION_ID = "_id";
	public static final String CITY = "city";
	public static final String LINE = "line";
	public static final String STATION = "station";
	public static final String LATITUDE = "lat";
	public static final String LONGITUDE = "lng";
	public static final String RADIUS = "radius";

	public static final int CITY_COLUMN = 1;
	public static final int LINE_COLUMN = 2;
	public static final int STATION_COLUMN = 3;
	public static final int LATITUDE_COLUMN = 4;
	public static final int LONGITUDE_COLUMN = 5;

	private static SQLiteDatabase db = null;
	private static Context context;
	private static LADBOpenHelper dbHelper;

	
	public DBAdapter(Context _context) {
		context = _context;
		dbHelper = new LADBOpenHelper(_context);
	}

	/** Close the database */
	public void close() {
		db.close();
	}

	/** Open the database */
	public void open() throws SQLiteException 
	{  
		try 
		{
			dbHelper.CreateDataBase();
		} 
		catch (IOException e) 
		{
			Log.d(Constants.LOG, "Unable to create database");
		}
		try 
		{
			dbHelper.openDatabase();
			db = dbHelper.GetDB();

		}
		catch(SQLException sqle)
		{
			Log.d(Constants.LOG, "Unable to open database in SubwayDBAdapter");
		}
	}
	
	//////////////////////////////////////////////////////////////////
	/*
	 * new method to UI
	 * for every list item, get all the sub-items to display.
	*/
	public Cursor GetAllItems(String table, String cityName, String line)
	{
		Cursor cursor = null;
		//set type
		if (table.equals(Constants.TYPE_TABLE))
			cursor = db.query(table, new String[] {"_id", "type"}, null, null, null, null, null);
		//set city
		else if (table.equals(Constants.CITY_TABLE))
			cursor = db.query(true, table, new String[] {"_id", "cityname", "hassubway"}, Constants.HASSUBWAY_COLUMN +"='1'", null, null, null, null, null);
		//set line
		else if (table.equals(Constants.SUBWAY_TABLE) && cityName.length() > 0 && line == null)
		{
			int cityId = GetCityId(cityName);
			if (cityId == -1)
				return cursor;
			
			cursor = db.query(table, new String[] {"_id", "cityid", "line", "station", "lat", "lng"}, 
					Constants.CITYID_COLUMN + "='" + String.valueOf(cityId) + "'", null,  
					Constants.LINE_COLUMN, null, Constants.ID_COLUMN, null);
			
			if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
				Log.v(Constants.LOG, " GetAllItems function failed.");
			}
		}
		//set station
		else if (table.equals(Constants.SUBWAY_TABLE) && cityName.length() > 0 && line.length() > 0)
		{
			int cityId = GetCityId(cityName);
			if (cityId == -1)
				return cursor;
			
			cursor = db.query(table, new String[] {"_id", "cityid", "line", "station", "lat", "lng"}, 
					Constants.CITYID_COLUMN + "='" + String.valueOf(cityId) + "' and " +
					Constants.LINE_COLUMN + "='" + line + "'", null, null, null, null);
		}
		
		return cursor;
	}
	public int GetCityId(String cityName)
	{
		//query CITY table
		int cityId = -1;
		Cursor cursor = db.query(Constants.CITY_TABLE, new String[] {"_id", "cityname", "hassubway"}, 
				Constants.CITYNAME_COLUMN + "=" + "'" + cityName + "'", null, null, null, null, null);
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			Log.v(Constants.LOG, " GetCityId: " + cityName + " failed.");
		}
		
		cityId = cursor.getInt(Constants.ID_IDX);
		cursor.close();
		
		return cityId;
	}
	
	public String GetCityName(int cityId)
	{
		String name = null;
		Cursor cursor = db.query(Constants.CITY_TABLE, new String[] {"_id", "cityname", "hassubway"}, 
				Constants.ID_COLUMN + "=" + "'" + String.valueOf(cityId) + "'", null, null, null, null, null);
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No city item found for row");
		}
		
		name = cursor.getString(Constants.CITYNAME_IDX);
		cursor.close();
		
		return name;
	}
	
	public Cursor GetSingleItem(String station)
	{
		Cursor cursor = db.query(Constants.SUBWAY_TABLE, new String[] {"_id", "cityid", "line", "station", "lat", "lng"},
				Constants.STATION_COLUMN + "='" + station + "'", null, null, null, null, null);
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			Log.v(Constants.LOG, " GetSingleItem: " + station + " failed.");
		}

		return cursor; 
	}
	
	public String GetLine(long id)
	{
		
		String line = null;
		Cursor cursor = db.query(Constants.SUBWAY_TABLE, new String[] {"_id", "cityid", "line", "station", "lat", "lng"},
				Constants.ID_COLUMN + "='" + String.valueOf(id) + "'", null, null, null, null, null);
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			Log.v(Constants.LOG, " GetLine: " + String.valueOf(id) + " failed.");
		}
		
		line = cursor.getString(Constants.LINE_IDX);
		return line; 
	}
	
	//below is for hotspot
	public Cursor GetAllHotspots()
	{
		Cursor cursor = null;
		cursor = db.query(Constants.HOTSPOT_TABLE, new String[] {"_id", "type", "province", 
				"city", "line", "station", "checked", "lat", "lng"}, 
				null, null, null, null, null);
		return cursor;
	}
	
	public Cursor GetHotspot(long _id)
	{
		Cursor cursor = null;
		cursor = db.query(Constants.HOTSPOT_TABLE, new String[] {"_id", "type", "province", 
				"city", "line", "station", "checked", "lat", "lng"}, 
				Constants.ID_COLUMN + "=" + String.valueOf(_id), null, null, null, null);
		return cursor;
	}
	
	public long AddSubwayHotspot(ContentValues value)
	{
		return db.insert(Constants.HOTSPOT_TABLE, null, value);
	}
	
	public void UpdateHotspot(String checked, long id)
	{
		String sql = "update "+ Constants.HOTSPOT_TABLE +" set checked='" + checked +"' where " + 
					Constants.ID_COLUMN + "='" + String.valueOf(id) + "'";
		db.execSQL(sql);
		//return db.update(Constants.HOTSPOT_TABLE, value, whereClause, null);
	}
	
	public int DeleteHotspot(long id)
	{
		return db.delete(Constants.HOTSPOT_TABLE, Constants.ID_COLUMN + "='" + String.valueOf(id) + "'", null);
	}
	
	//below if for train
	public Cursor GetCities(String province)
	{
		Log.v(Constants.LOG, " GetCities: " + province);
		Cursor cursor = null;
		cursor = db.query(Constants.TRAIN_TABLE, new String[] {"_id", "province", "station", "lat", "lng"}, 
				Constants.HS_PROVINCE + "='" + province + "'", null, null, null, null);
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			Log.v(Constants.LOG, " GetCities: " + province + " failed.");
		}
		
		return cursor;
	}
	
	public long AddTrainHotspot(ContentValues value)
	{
		return db.insert(Constants.HOTSPOT_TABLE, null, value);
	}
	
	public Cursor GetCity(String station)
	{
		Log.v(Constants.LOG, " GetCity: " + station);
		Cursor cursor = db.query(Constants.TRAIN_TABLE, new String[] {"_id", "province", "station", "lat", "lng"},
				Constants.STATION_COLUMN + "='" + station + "'", null, null, null, null, null);
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			Log.v(Constants.LOG, " GetCity: " + station + " failed.");
		}

		return cursor; 
	}
	
	//used by MapsActivity
	public long AddCustomHotspot(ContentValues value)
	{
		return db.insert(Constants.HOTSPOT_TABLE, null, value);
	}
	
	//used by SearchActivity
	public Cursor SubwayStations(String station)
	{
		Cursor cursor = null;
		
		cursor = db.query(Constants.SUBWAY_TABLE, new String[] {"_id", "cityid", "line", "station", "lat", "lng"}, 
				Constants.STATION_COLUMN + "='" + station + "'", null, null, null, null);
		if (cursor.getCount() > 0)
			return cursor;
		
		//fuzzy search
		cursor = db.query(Constants.SUBWAY_TABLE, new String[] {"_id", "cityid", "line", "station", "lat", "lng"}, 
					Constants.STATION_COLUMN + " like '%" + station + "%'", null, null, null, null);
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			Log.v(Constants.LOG, " SubwayStations: " + station + " failed.");
		}
		
		return cursor;
	}
	
	public Cursor TrainStations(String station)
	{
		Cursor cursor = null;
		
		cursor = db.query(Constants.TRAIN_TABLE, new String[] {"_id", "province", "station", "lat", "lng"}, 
				Constants.STATION_COLUMN + "='" + station + "'", null, null, null, null);
		if (cursor.getCount() > 0) {
			return cursor;
		}
		//fuzzy search
		cursor = db.query(Constants.TRAIN_TABLE, new String[] {"_id", "province", "station", "lat", "lng"}, 
					Constants.STATION_COLUMN + " like '%" + station + "%'", null, null, null, null);
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			Log.v(Constants.LOG, " TrainStations: " + station + " failed.");
		}
		
		return cursor;
	}
	//////////////////////////////////////////////////////////////////

	/** Remove a station on its index */
	public boolean remove(long _rowIndex) {
		return db.delete(DATABASE_TABLE, LOCATION_ID + "=" + _rowIndex, null) > 0;
	}

	/*
	  public boolean update(long _rowIndex, String _task) {
	    ContentValues newValue = new ContentValues();
	    newValue.put(KEY_TASK, _task);
	    return db.update(DATABASE_TABLE, newValue, KEY_ID + "=" + _rowIndex, null) > 0;
	  }
	 */

	/** Return a Cursor to all the station items */
	public Cursor getAllStationsCursor() {
		return db.query(DATABASE_TABLE, new String[] { LOCATION_ID, CITY, LINE, STATION, LATITUDE, LONGITUDE}, null, null, null, null, null);
	}

	/** Return a Cursor to a specific row */
	public Cursor setCursorStationItem(long _rowIndex) throws SQLException {
		Cursor result = db.query(true, DATABASE_TABLE, new String[] {LOCATION_ID, CITY, LINE, STATION, LATITUDE, LONGITUDE},
				LOCATION_ID + "=" + _rowIndex, null, null, null, null, null);

		if ((result.getCount() == 0) || !result.moveToFirst()) {
			throw new SQLException("No stations found for row: " + _rowIndex);
		}
		return result;
	}

	/** Return a station Item based on its row index */
	public Cursor getStationItem(String staionName) throws SQLException {
		Cursor cursor = db.query(true, DATABASE_TABLE, new String[] {LOCATION_ID, CITY, LINE, STATION, LATITUDE, LONGITUDE},
				STATION + "='" + staionName + "'", null, null, null, null, null);
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No to do item found for row: " + staionName);
		}

		return cursor;  
	}

	/** Static Helper class for creating, upgrading, and opening
	 * the database.
	 */
	private static class LADBOpenHelper extends SQLiteOpenHelper 
	{
		private SQLiteDatabase mDataBase; 
		private final Context context;

		public LADBOpenHelper(Context context) 
		{

			super(context, Constants.DB_NAME, null, 1);
			this.context = context;
		}	

		/**
		 * Creates a empty database on the system and rewrites it with your own database.
		 * */
		public void CreateDataBase() throws IOException
		{
			boolean dbExist = CheckDataBase();

			if(dbExist){
				//do nothing - database already exist
			}else{
				//By calling this method and empty database will be created into the default system path
				//of your application so we are be able to overwrite that database with our database.
				SQLiteDatabase db = this.getReadableDatabase();
				if (db.isOpen())
					db.close();
				
				try {
					CopyDataBase();

				} catch (IOException e)
				{
					Log.d(Constants.LOG, "Copy database error: " + e.getMessage());
				}
			}
		}

		/**
		 * Check if the database already exist to avoid re-copying the file each time you open the application.
		 * @return true if it exists, false if it doesn't
		 */
		private boolean CheckDataBase()
		{
			SQLiteDatabase db = null;

			try{
				String dbPath = Constants.DB_PATH + Constants.DB_NAME;
				db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
			}catch(SQLiteException e)
			{
				//database does't exist yet.
				Log.d(Constants.LOG, "open database failed: " + e.getMessage());
			}

			if(db != null)
			{
				db.close();
			}

			return db != null ? true : false;
		}

		/**
		 * Copies your database from your local assets-folder to the just created empty database in the
		 * system folder, from where it can be accessed and handled.
		 * This is done by transfering bytestream.
		 */
		private void CopyDataBase() throws IOException
		{

			//Open your local db as the input stream
			InputStream is = context.getAssets().open(Constants.DB_NAME);

			// Path to the just created empty db
			String outDBFile= Constants.DB_PATH + Constants.DB_NAME;

			//Open the empty db as the output stream
			OutputStream os = new FileOutputStream(outDBFile);

			//transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer))>0){
				os.write(buffer, 0, length);
			}

			//Close the streams
			os.flush();
			os.close();
			is.close();

		}
		public void openDatabase() throws SQLException
		{
			//Open the database
			//String daPath = DB_PATH + DB_NAME;
			//mDataBase = SQLiteDatabase.openDatabase(daPath, null, SQLiteDatabase.OPEN_READONLY);
			mDataBase = getReadableDatabase();
			if (mDataBase == null)
			{
				Log.d(Constants.LOG, "getReadableDatabase() failed: ");
			}
		}
		
		public SQLiteDatabase GetDB()
		{
			return mDataBase;
		}

		@Override
		public synchronized void close() 
		{
			if(mDataBase != null)
				mDataBase.close();
			super.close();

		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{

		}
	}
}
