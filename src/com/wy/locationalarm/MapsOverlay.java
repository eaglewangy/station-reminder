package com.wy.locationalarm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.wy.locationalarm.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MapsOverlay extends ItemizedOverlay<OverlayItem>{

	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private Context context;
	
	private final Paint dottedStrokePaint;
	private final Paint fillPaint;
	private final Paint strokePaint;
	private final int strokeWidth = 5;
	private float currCircleRadius = 50;
	private float miniCircleRadius = 3;
	
	private DBAdapter dbAdapter;
	
	
	public MapsOverlay(MapsActivity context, Drawable maker) {
		super(boundCenterBottom(maker));
	
		this.context = context;
		//items.add(new OverlayItem(getPoint(40.748963847316034, -73.96807193756104), "UN", "United Nations"));
		
		this.fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.fillPaint.setARGB(64, 255, 119, 107);
        
        this.strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.strokePaint.setARGB(255, 255, 119, 107);
        this.strokePaint.setStyle(Style.STROKE);
        this.strokePaint.setStrokeWidth(strokeWidth);
        
		this.dottedStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.dottedStrokePaint.setARGB(255, 148, 154, 170);
        this.dottedStrokePaint.setStyle(Style.STROKE);
        this.dottedStrokePaint.setStrokeWidth(strokeWidth);
        this.dottedStrokePaint.setPathEffect(new DashPathEffect(new float[] {20.0f, 7.5f}, 0.0f));
        
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return items.get(i);
	}
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		OverlayItem item = getFocus();
        if (item != null)
        {
            Projection projection = mapView.getProjection();
            GeoPoint geoPoint = item.getPoint();
            Point point = projection.toPixels(geoPoint, null);
            drawCircle(canvas, this.dottedStrokePaint, point, currCircleRadius);
            drawCircle(canvas, this.fillPaint, point, currCircleRadius);
        	drawCircle(canvas, this.strokePaint, point, currCircleRadius);
        	drawCircle(canvas, this.strokePaint, point, miniCircleRadius);
        }
        
        super.draw(canvas, mapView, shadow);
	}
	
	private void drawCircle(Canvas canvas, Paint paint, Point center, float radius)
    {
        RectF circleRect = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        canvas.drawOval(circleRect, paint);
    }
	
	@Override
	public boolean onTap(int i) {
		OverlayItem item = items.get(i);
		GeoPoint currentPoint = item.getPoint();
		final double lat = currentPoint.getLatitudeE6()/1E6;
		final double lng = currentPoint.getLongitudeE6()/1E6;
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		String content = item.getSnippet();
		char lastChar = content.charAt(content.length() - 1);
		//content = content.replaceAll("...", "");
		//MapsActivity in MainActivity
		if (lastChar == '_')
		{
			dialog.setMessage(content.replace("_", ""));
			dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int position) {
					dialog.cancel();
				}
			});
		}
		//apsActivity in ConfigureActivity
		else{
			dialog.setTitle(item.getTitle());
			int pos = content.indexOf("\n");
			final String station = content.substring(0, pos);
			dialog.setMessage(content);
			dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int position) {
					dialog.cancel();
				}
			});
			dialog.setPositiveButton(context.getString(R.string.makesure), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int position) {
					dialog.cancel();
					AddHotspot(station, lat, lng);
				}
			});
		}
		
		dialog.show();
		return true;
	}
	
	private void AddHotspot(String station, double lat, double lng)
	{
		Log.v(Constants.LOG, "map custom station: " + station);
		dbAdapter = new DBAdapter(context);
        dbAdapter.open();
		ContentValues hotspotValue = new ContentValues();
		hotspotValue.put(Constants.TYPE_COLUMN, Constants.CUSTOM_HOTSPOT);
		hotspotValue.putNull(Constants.HS_CITY);
		hotspotValue.putNull(Constants.LINE_COLUMN);
		hotspotValue.put(Constants.STATION_COLUMN, station);
		hotspotValue.put(Constants.HS_CHECKED, "1");
		hotspotValue.put(Constants.HS_LAT, String.valueOf(lat));
		hotspotValue.put(Constants.HS_LNG, String.valueOf(lng));
		dbAdapter.AddSubwayHotspot(hotspotValue);
		
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);
		((Activity) context).finish();
	}
	
	@Override
	public int size() {
		return items.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
		items.add(overlay);
	    populate();
	}
}
