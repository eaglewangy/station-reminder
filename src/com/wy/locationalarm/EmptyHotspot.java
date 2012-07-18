package com.wy.locationalarm;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EmptyHotspot extends Activity{
	
	private Button addNewBtn;
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emptyhotspot_layout);
		
		addNewBtn = (Button)findViewById(R.id.addnewbtn);
		addNewBtn.setOnClickListener(addListener);
		//addNewBtn.setVisibility(View.GONE);
	}
	
	private OnClickListener addListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(), ConfigureActivity.class);
			startActivity(intent);
		}
	};
}
