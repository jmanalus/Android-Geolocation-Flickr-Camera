package edu.berkeley.cs160.jonathanmanalus.prog3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.getawaycam.R;

public class flickrCamera extends Activity {

	/*To display photos for the imageView, and I got help from http://stackoverflow.com/questions/19323100/how-to-display-image-in-imageview */
	public static final int MEDIA_TYPE_IMAGE = 1;
	/*Request code returned by the camera source help: http://mobile.tutsplus.com/tutorials/android/android-sdk-quick-tip-launching-the-camera/ */
	private static final int CAMERA_PIC_REQUEST = 1313;
	Location currentLocation;
	
	/*My apps api key from Flickr */
	private  String api_key = "a8258e916bb20fc1d407f2b18622bc66";
	
	/* Source: http://developer.android.com/reference/android/os/Environment.html
	 * and 
	 * http://stackoverflow.com/questions/18434877/why-would-getexternalstoragepublicdirectory-and-getexternalfilesdir-are-not-supp 
	 * http://stackoverflow.com/questions/8564396/trying-to-save-picture-to-a-particular-pictures-folder-that-has-not-been-created
	 */
	final String dirPics = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/photos/"; 
	final Uri mapUri = Uri.parse(dirPics + "picMap");
	final Uri numUri = Uri.parse(dirPics + "picNum");
	
	/* returns current time that has passed */
	Random random = new Random(System.currentTimeMillis());
	
	/*Stores photos into a hash. I got help with better understanding it from here http://stackoverflow.com/questions/11945531/listview-adapter-with-hashmap and
	 * http://developer.android.com/reference/java/util/HashMap.html
	 */
	public static HashMap<Integer, Location> map = new HashMap<Integer, Location>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take);
		/* Help from http://stackoverflow.com/questions/13983728/android-using-getintent-only-within-oncreate */
		Intent i = getIntent();
		currentLocation = (Location) i.getExtras().get("currentLocation");
		takePhoto();
	}
	/* I got help from several posts on stackoverflow and the google android api docs to be able to put the flickr API together to gather photos from flickr
	 * http://stackoverflow.com/questions/16326279/android-code-works-fine-on-gingerbread-but-breaks-on-nexus-7-w-4-2-2
	 * http://stackoverflow.com/questions/4854517/when-using-the-camera-app-in-android-how-can-i-return-the-whole-image-instead-o
	 */
	public void takePhoto() {
	    File newdir = new File(dirPics); 
	    newdir.mkdirs();
        String file = dirPics+".jpg";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {}
        Uri outputFileUri = Uri.fromFile(newfile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    	double lat = currentLocation.getLatitude();
    	double lon = currentLocation.getLongitude();
		String target = "http://api.flickr.com/services/rest/?method=flickr.photos.search&lat=" + lat + "&lon=" + lon + "&api_key=" + api_key + "&format=json&per_page=10";
		
		picturesFromFlickr task = new picturesFromFlickr();
		task.execute(target);
	}

	public void takePhoto(View v) {
		takePhoto();
	}
	
	
	
	/* Source section slides 9 from GSI Eric and from this post http://stackoverflow.com/questions/4223472/how-to-display-image-from-internet-in-android */
	private class picturesFromFlickr extends AsyncTask<String, Integer, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... arg0) {
			StringBuilder builder = new StringBuilder();
			HttpURLConnection connection = null;
			try {
				URL url = new URL(arg0[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoOutput(false);
				connection.setDoInput(true);
				connection.setRequestMethod("GET");
				InputStream in = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = reader.readLine();
				builder.append(line);
				Log.i("error", "before JSON return1");
				String result = builder.toString();
				result = result.substring(result.indexOf('(') + 1, result.lastIndexOf(')'));
				JSONObject json = new JSONObject(result);
				Log.i("error", "before JSON return: " + json.toString());
				return json;
			} catch (Exception e) {
				Log.i("error", "exception: " + e.getMessage());
				return null;
			}
		}
		
		protected void onPostExecute(JSONObject result) {
			updateView(result);
		}
	}
	/*Help link http://stackoverflow.com/questions/11439522/json-and-network-operation-from-an-asynctask 
	 * http://forum.processing.org/one/topic/interactive-flickr-photo-visualization-json.html
	 * */
	public void updateView(JSONObject json) {
		try {
			JSONObject page = json.getJSONObject("photos");
			JSONArray photos = page.getJSONArray("photo");
			if (photos.length() > 0) {
				JSONObject photo = photos.getJSONObject(random.nextInt(photos.length()));
				String id = photo.getString("id");
				String secret = photo.getString("secret");
				String serverId = photo.getString("server");
				String farmId = photo.getString("farm");
				String url = "http://farm" + farmId + ".staticflickr.com/" + serverId + "/" + id + "_" + secret + ".jpg";
				FindBitmap task = new FindBitmap();
				task.execute(url);
		} 
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/*Bitmap http://stackoverflow.com/questions/12001793/android-imageview-setimagebitmap-vs-setimagedrawable */
	public void setImageBitmap(Bitmap bm) {
		ImageView image = (ImageView) findViewById(R.id.picture);
		image.setImageBitmap(bm);
	}
	/* PARAM the IMG URL 
	 * This post helped http://stackoverflow.com/questions/16538714/stop-asynctask-doinbackground-method and Eric slides helped me with AsyncTask.
	 * To help with bitmap and image processing http://stackoverflow.com/questions/3802820/bitmapfactory-decodestream-always-returns-null-and-skia-decoder-shows-decode-ret
	 * */

	public class FindBitmap extends AsyncTask<String, Integer, Bitmap> {
	
		@Override
		protected Bitmap doInBackground(String... params) {
			try {
		        URL src = new URL(params[0]);
		        HttpURLConnection connection = (HttpURLConnection) src.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        return myBitmap;
		    } catch (IOException e) {
		        e.printStackTrace();
		        return null;
		    }
		}
		
		@Override
		protected void onPostExecute(Bitmap bm) {
			setImageBitmap(bm);
		}
	}
	

	/* back to the home screen */
	public void backHome(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
