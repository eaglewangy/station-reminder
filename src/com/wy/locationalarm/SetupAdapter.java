package com.wy.locationalarm;

import java.util.List;

import com.wy.locationalarm.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SetupAdapter extends ArrayAdapter<SetupItem>
{
	int resource;

	 public SetupAdapter(Context context, int resource, List<SetupItem> items) 
	 {
	    super(context, resource, items);
	    this.resource = resource;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) 
	  {
	    LinearLayout listView;

	    SetupItem item = getItem(position);

	    String title = item.GetTitle();
	    String value = item.GetValue();

	    if (convertView == null) 
	    {
	    	listView = new LinearLayout(getContext());
	    	String inflater = Context.LAYOUT_INFLATER_SERVICE;
	    	LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
	    	vi.inflate(resource, listView, true);
	    } 
	    else 
	    {
	    	listView = (LinearLayout) convertView;
	    }

	    TextView titleView = (TextView)listView.findViewById(R.id.firstLine);
	    TextView valueView = (TextView)listView.findViewById(R.id.secondLine);
	      
	    titleView.setText(title);
	    valueView.setText(value);

	    return listView;
	  }
}
