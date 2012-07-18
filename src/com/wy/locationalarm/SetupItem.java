package com.wy.locationalarm;

public class SetupItem {
	  public String GetTitle() 
	  {
		  return itemTitle;
	  }
	  
	  public String GetValue()
	  {
		  return itemValue;
	  }
	  
	  public void SetTitle(String title)
	  {
		  itemTitle = title;
	  }
	  
	  public void SetValue(String value)
	  {
		  itemValue = value;
	  }

	  public SetupItem(String title, String value) 
	  {
		  this.itemTitle = title;
		  this.itemValue = value;
	  }
	  
	  String itemTitle;
	  String itemValue;
}
