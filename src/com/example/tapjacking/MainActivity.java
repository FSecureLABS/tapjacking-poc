package com.example.tapjacking;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // get the root view and activate touch filtering to prevent tap jacking
    View v = findViewById(android.R.id.content);
    v.setFilterTouchesWhenObscured(true);
}

	private static int SEEK_MAX = 100;

	private Intent service;

	// UI elements
	SeekBar left;
	SeekBar top;
	EditText repRate;

	// seekbar change listener
	OnSeekBarChangeListener positionListner;

	// phone display dimensions
	int width, height;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get the dimensions of the tapjacking image
		BitmapDrawable bd = (BitmapDrawable) this.getResources().getDrawable(
				R.drawable.ic_terms_message);
		int h = bd.getBitmap().getHeight();
		int w = bd.getBitmap().getWidth();

		Display display = getWindowManager().getDefaultDisplay();

		// get the display dimensions, these are used to determine how far a
		// given offset should move the image. as the offset uses the top left
		// corner as the point from which it is calculated the width and height
		// of the poc image are subtracted from the height and width of the
		// screen
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point size = new Point();
			display.getSize(size);
			width = size.x - w;
			height = size.y - h;
		} else {
			// getSize was introduced in API 13, so all previous builds need the
			// deprecated getWidth and getHeight functions
			width = display.getWidth() - w;
			height = display.getHeight() - h;

		}

		// instantiate UI elements
		left = (SeekBar) findViewById(R.id.seekLeftOffset);
		top = (SeekBar) findViewById(R.id.seekTopOffset);
		repRate = (EditText) findViewById(R.id.edtRepetionRate);

		// define the actions to be performed when a user interacts with the
		// seekbar
		positionListner = new OnSeekBarChangeListener() {
			Toast temp = new Toast(getApplicationContext());
			ImageView img = new ImageView(getApplicationContext());

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// pop up the tapjacking image so that the user can see where it
				// is
				img.setImageResource(R.drawable.ic_terms_message);
				temp.setView(img);
				temp.setDuration(Toast.LENGTH_SHORT);
				temp.setGravity(Gravity.TOP | Gravity.LEFT,
						(int) (left.getProgress() * width / (SEEK_MAX)),
						(int) (top.getProgress() * height / (SEEK_MAX)));
				temp.show();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// when they start moving the image, cancel the old display
				temp.cancel();
				stopService(service);

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// do nothing, nobody cares
			}
		};

		// set the seekbar listener to both the left and top bars
		left.setOnSeekBarChangeListener(positionListner);
		top.setOnSeekBarChangeListener(positionListner);

		// start a new intent for the ToastService
		service = new Intent(this, ToastService.class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Start the Tapjacking Service
	 * 
	 * @param view
	 */

	@SuppressLint("NewApi")
	public void startTJService(View view) {
		// try and get the repetition period specified by the user, if the value
		// entered is not a valid number, log the error
		int rep = 15000;
		try {
			rep = Integer.parseInt(repRate.getText().toString()) * 1000;
		} catch (Exception e) {
			Log.w("_com_example_tapjacking",
					"Failed to convert repetion value to a number, using default value");
			rep=15000;
		}
		service.putExtra("_com_example_tapjacking_topOffset",
				(int) (top.getProgress() * height / (SEEK_MAX)));
		service.putExtra("_com_example_tapjacking_leftOffset",
				(int) (left.getProgress() * width / (SEEK_MAX)));
		service.putExtra("_com_example_tapjacking_repetitionTime", rep);
		this.startService(service);
	}

	/**
	 * Stop the Tapjacking Service
	 * 
	 * @param view
	 */
	public void stopTJService(View view) {
		this.stopService(service);
	}

}
