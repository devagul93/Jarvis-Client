package info.androidhive.speechtotext;

import android.net.Uri;

public class Contact {

	public Contact(String name, String number, int image){
		CONTACT_NAME = name;
		CONTACT_NUMBER = number;
		Imageres = image;
		isSelected = false;
	}
	int Imageres;
	public boolean isSelected;
	public long CONTACT_ID;
	public String CONTACT_NUMBER;
	public String CONTACT_LOOKUP_KEY;
	public String CONTACT_NAME;
	public Uri CONTACT_URI;
	public String OBJECT_ID;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return CONTACT_NAME;
	}
}