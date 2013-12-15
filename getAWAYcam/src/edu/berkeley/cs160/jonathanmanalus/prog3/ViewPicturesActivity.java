package edu.berkeley.cs160.jonathanmanalus.prog3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.getawaycam.R;

public class ViewPicturesActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);	
	}

	/* back to the home screen */
	public void backHome(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
