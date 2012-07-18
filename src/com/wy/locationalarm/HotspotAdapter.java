package com.wy.locationalarm;

import java.util.List;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class HotspotAdapter extends ArrayAdapter<HotspotItem> {

	private final List<HotspotItem> items;
	private final Activity context;

	public HotspotAdapter(Activity context, List<HotspotItem> items) {
		super(context, R.layout.hotspot, items);
		this.context = context;
		this.items = items;
	}

	static class ViewHolder {
		public ImageView imageView;
		protected TextView text;
		protected TextView distanceAndSpeed;
		protected CheckBox checkbox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.hotspot, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView) view.findViewById(R.id.hsimage);
			viewHolder.text = (TextView) view.findViewById(R.id.hstext);
			viewHolder.distanceAndSpeed = (TextView) view.findViewById(R.id.distance);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.hscheckbox);
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							HotspotItem element = (HotspotItem) viewHolder.checkbox.getTag();
							element.SetSelected(buttonView.isChecked());
						}
					});
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(items.get(position));
		}
		else 
		{
			view = convertView;
			((ViewHolder) view.getTag()).checkbox.setTag(items.get(position));
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.text.setText(Html.fromHtml(items.get(position).GetContent()));
		
		String distanceAndSpeed = items.get(position).GetDistanceAndSpeed();
		if (distanceAndSpeed.length() == 0)
			holder.distanceAndSpeed.setVisibility(View.GONE);
		else{
			holder.distanceAndSpeed.setVisibility(View.VISIBLE);
			holder.distanceAndSpeed.setText(Html.fromHtml(items.get(position).GetDistanceAndSpeed()));
		}
		
		holder.checkbox.setChecked(items.get(position).IsSelected());
		int type = items.get(position).GetType();
		if (type == Constants.TRAIN) {
			holder.imageView.setImageResource(R.drawable.tab_train_grey);
		} 
		else if (type == Constants.SUBWAY)
		{
			holder.imageView.setImageResource(R.drawable.tab_subway_grey);
		}
		return view;
	}
}
