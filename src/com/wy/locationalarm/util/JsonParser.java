package com.wy.locationalarm.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.wy.locationalarm.Constants;

import android.util.Log;

/*
	{ 
	   "result":{"time":"2011-08-28 17:29:45","error":"161"},
	   "content":{
	       "point":{"x":"12947422.359722","y":"4846434.0501397"},
	       "radius":"130",
	       "addr":{"detail":"Beijing shi,shang di jiu jie"}
	   }
	}
*/

public class JsonParser {
	
	public String lat, lng, addr;
	
	private JSONObject jsonObject;
	
	public boolean parse(String jsonStr){
		try {
			if (jsonStr == null || jsonStr.length() == 0)
				return false;
			//Log.e("LAAAAAAAAAAA", jsonStr);
			jsonObject = new JSONObject(jsonStr);
			JSONObject resultObject = jsonObject.getJSONObject("result");
			String errorValue = resultObject.getString("error");
			if (!errorValue.equals("161")){
				Log.e(Constants.LOG, "Parse Json error.");
				return false;
			}
			
			JSONObject contentObject = jsonObject.getJSONObject("content");
			JSONObject pointObject = contentObject.getJSONObject("point");
			String lngValue = pointObject.getString("x");
			String latValue = pointObject.getString("y");
			
			JSONObject addrObject = contentObject.getJSONObject("addr");
			String addrValue = addrObject.getString("detail");
			
			lat = latValue;
			lng = lngValue;
			addr = addrValue;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
