package com.wy.locationalarm;

public class HotspotItem {
	
	private String content;
	private int _id;
	private int type;
	private String province;
	private String city;
	private String line;
	private String station;
	private int checked;
	private boolean selected;
	private double lat;
	private double lng;
	private String distanceAndSpeed;
	
	private DBAdapter dbAdpter;

	public HotspotItem(int _id, int type, String province, String city, 
			String line, String station, int checked, double lat, 
			double lng, DBAdapter dbAdpter) {
		
		this.dbAdpter = dbAdpter;
		this._id = _id;
		this.type = type;
		this.province = province;
		this.city = city;
		this.line = line;
		this.station = station;
		if (type == Constants.SUBWAY)
			this.content = "<font size=\"100\">" +
							city + " " + line + "</font><br>"
							+ station;
		else if (type == Constants.TRAIN)
			this.content = province + " " + station;
		else if (type == Constants.CUSTOM_HOTSPOT)
			this.content = station;
		if (checked == 1)
			selected = true;
		else if (checked == 0)
			selected = false;
		
		this.lat = lat;
		this.lng = lng;
		
		distanceAndSpeed = "";
	}

	public String GetContent() {
		return this.content;
	}
	
	public String GetDistanceAndSpeed(){
		return distanceAndSpeed;
	}
	
	public void SetDistanceAndSpeed(String distanceAndSpeed){
		this.distanceAndSpeed = distanceAndSpeed;
	}

	public void SetContent(String content) {
		this.content = content;
	}

	public boolean IsSelected() {
		return selected;
	}
	
	public String GetProvince(){
		if (type == Constants.TRAIN)
			return province;
		else if (type == Constants.SUBWAY)
			return null;
		else
			return null;
	}
	
	public String GetCity(){
		return city;
	}
	
	public String GetLine(){
		if (type == Constants.SUBWAY)
			return line;
		else
			return null;
	}
	
	public String GetStation(){
		return station;
	}

	public void SetSelected(boolean selected) {
		String checked = null;
		if (selected)
			checked = "1";
		else
			checked = "0";
		
		dbAdpter.UpdateHotspot(checked, this._id);
		this.selected = selected;
	}
	
	public double GetLat(){
		return lat;
	}
	
	public double GetLng(){
		return lng;
	}
	
	public int GetDbId()
	{
		return this._id;
	}
	
	public void SetDbId(int _id)
	{
		this._id = _id;
	}
	
	public int GetType()
	{
		return this.type;
	}
}
