package edu.berkeley.cs160.jonathanmanalus.prog3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.getawaycam.R;

public class MainActivity extends Activity {
	
	Location location;
	String message;
	
	public void setMessage(String x) {
		TextView text = (TextView) findViewById(R.id.message);
		text.setText(x);
		message = x;
	}
	
	/*To take photos intent */
	public void toTakePictures(View view) {
		Intent intent = new Intent(this, flickrCamera.class);
		intent.putExtra("currentLocation", location);
		startActivity(intent);
	}
	
	/*Photo Album */
	public void toViewPictures(View view) {
		Intent intent = new Intent(this, ViewPicturesActivity.class);
		intent.putExtra("currentLocation", location);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button camera = (Button) findViewById(R.id.toTakePictures);
		Button photoAlbum = (Button) findViewById(R.id.toViewPictures);
		
		/*Loading message while we GeoCam finds your current location */
		setMessage("Please wait while GeoCam finds your current location..."); 
		
		/* So the user doesn't click on buttons while waiting, I've made them the same co */
		camera.setClickable(false);
		camera.setBackgroundColor(Color.WHITE);
		camera.setTextColor(Color.WHITE);
		
		/*Hides Album Button */
		photoAlbum.setClickable(false);
		photoAlbum.setBackgroundColor(Color.WHITE);
		photoAlbum.setTextColor(Color.WHITE);
		
		/* Starts LocationManager http://developer.android.com/reference/android/location/LocationManager.html */
		LocationManager mylocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		String locationProvider = LocationManager.GPS_PROVIDER;
		LocationListener locationListener = new MyLocationListener();
		mylocationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
	}

	

	/*Source: http://stackoverflow.com/questions/11863696/this-class-that-should-return-my-location-and-actually-returns-0-0-0-0
	 *Finds my location  */
	public class MyLocationListener implements LocationListener {
		
		public MyLocationListener() {
		}

		@Override
		public void onLocationChanged(Location loc) {
			setLocation(loc);
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}
		@Override
		public void onProviderEnabled(String arg0) {
		}
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}
	}
	/*Checks if we can find your location help from http://stackoverflow.com/questions/2250597/find-current-location-latitude-and-longitude */
	public void setLocation(Location loc) {
		Button toTake = (Button) findViewById(R.id.toTakePictures);
		Button toView = (Button) findViewById(R.id.toViewPictures);
		location = loc;
		/*if location found returns a confirmation. */
		if (loc != null) {
			
			setMessage("Awesome GeoCam has found your location!");
			
			/*Shows buttons since location is found */
			toTake.setClickable(true);
			toTake.setBackgroundColor(Color.BLACK);
			toView.setClickable(true);
			toView.setBackgroundColor(Color.BLACK);
			
			/* Returns error message with a suggestion if app can't find location */
		} else {
			setMessage("Sorry,getAwayCam can't find your. Please check if GPS is on in your settings.");
		}
	}
	

}
