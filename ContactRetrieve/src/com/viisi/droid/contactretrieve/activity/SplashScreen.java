package com.viisi.droid.contactretrieve.activity;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.viisi.droid.contactretrieve.R;

public class SplashScreen extends Activity {

	private boolean isButoonPressed;
	private static int SPLASH_TIME_OUT = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				finish();
				if (!isButoonPressed) {
					Intent i = new Intent(SplashScreen.this, ContactRetrieveActivity.class);
					startActivity(i);
				}
			}
		}, SPLASH_TIME_OUT);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		openMainActivity();
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		openMainActivity();
		return super.onKeyDown(keyCode, event);
	}

	private void openMainActivity() {
		if (!isButoonPressed) {
			isButoonPressed = true;
			finish();

			Intent i = new Intent(SplashScreen.this, ContactRetrieveActivity.class);
			startActivity(i);
		}
	}

}
