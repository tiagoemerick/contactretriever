package com.viisi.droid.contactretrieve.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viisi.droid.contactretrieve.R;
import com.viisi.droid.contactretrieve.util.Constants;

// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
public class AppRater {
	private static String APP_TITLE;
	private final static String APP_PNAME = "com.viisi.droid.contactretrieve";

	private final static int DAYS_UNTIL_PROMPT = 0;
	private final static int LAUNCHES_UNTIL_PROMPT = 1;

	public static void app_launched(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.apprater.preferencefilename, 0);
		if (prefs.getBoolean(Constants.apprater.preference_dontshowagain, false)) {
			return;
		}

		APP_TITLE = mContext.getString(R.string.app_name);

		SharedPreferences.Editor editor = prefs.edit();

		// Incrementa apenas quando o app é acionado para mandar SMS em
		// SMSReciver
		long launch_count = prefs.getLong(Constants.apprater.preference_launch_count, 0);
		// editor.putLong("launch_count", launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong(Constants.apprater.preference_date_firstlaunch, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(Constants.apprater.preference_date_firstlaunch, date_firstLaunch);
		}

		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateDialog(mContext, editor);
			}
		}
		editor.commit();
	}

	public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
		if (APP_TITLE == null) {
			APP_TITLE = mContext.getString(R.string.app_name);
		}

		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle(mContext.getString(R.string.label_rate_title) + " " + APP_TITLE);

		RelativeLayout rlIconLauncher = new RelativeLayout(mContext);

		Button iconLauncher = new Button(mContext);
		iconLauncher.setId(5);
		iconLauncher.setBackgroundResource(R.drawable.ic_launcher);
		iconLauncher.setClickable(false);

		RelativeLayout.LayoutParams lpIconLauncher = new RelativeLayout.LayoutParams(56, 52);
		lpIconLauncher.addRule(RelativeLayout.CENTER_HORIZONTAL);

		rlIconLauncher.addView(iconLauncher, lpIconLauncher);

		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);

		ll.addView(rlIconLauncher);

		TextView tv = new TextView(mContext);
		tv.setText(mContext.getString(R.string.label_rate_message_1) + " " + APP_TITLE + mContext.getString(R.string.label_rate_message_2));
		tv.setWidth(240);
		tv.setPadding(4, 0, 4, 10);
		ll.addView(tv);

		Button bRate = new Button(mContext);
		bRate.setText(mContext.getString(R.string.label_rate_title) + " " + APP_TITLE);
		bRate.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
				// mContext.startActivity(new Intent(Intent.ACTION_VIEW,
				// Uri.parse("https://play.google.com/store/apps/details?id=" +
				// APP_PNAME)));
				dialog.dismiss();
			}
		});
		ll.addView(bRate);

		Button bRemind = new Button(mContext);
		bRemind.setText(mContext.getString(R.string.label_rate_remind_later));
		bRemind.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ll.addView(bRemind);

		Button bNoThx = new Button(mContext);
		bNoThx.setText(mContext.getString(R.string.label_rate_no_thanks));
		bNoThx.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean(Constants.apprater.preference_dontshowagain, true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		ll.addView(bNoThx);

		// RelativeLayout rlStars = createStars(mContext);
		// ll.addView(rlStars);

		dialog.setContentView(ll);
		dialog.show();
	}

	@SuppressWarnings("unused")
	private static RelativeLayout createStars(final Context mContext) {
		RelativeLayout rlStars = new RelativeLayout(mContext);

		int widthStar = 50;
		int heightStar = 46;

		Button bStart1 = new Button(mContext);
		bStart1.setId(50);
		bStart1.setBackgroundResource(R.drawable.bluestar);
		bStart1.setClickable(false);

		RelativeLayout.LayoutParams lpStar1 = new RelativeLayout.LayoutParams(widthStar, heightStar);
		lpStar1.leftMargin = 80;
		rlStars.addView(bStart1, lpStar1);

		Button bStar2 = new Button(mContext);
		bStar2.setId(100);
		bStar2.setBackgroundResource(R.drawable.bluestar);
		bStar2.setClickable(false);

		RelativeLayout.LayoutParams lpStar2 = new RelativeLayout.LayoutParams(widthStar, heightStar);
		lpStar2.addRule(RelativeLayout.RIGHT_OF, bStart1.getId());
		rlStars.addView(bStar2, lpStar2);

		Button bStar3 = new Button(mContext);
		bStar3.setId(150);
		bStar3.setBackgroundResource(R.drawable.bluestar);
		bStar3.setClickable(false);

		RelativeLayout.LayoutParams lpStar3 = new RelativeLayout.LayoutParams(widthStar, heightStar);
		lpStar3.addRule(RelativeLayout.RIGHT_OF, bStar2.getId());
		rlStars.addView(bStar3, lpStar3);

		Button bStar4 = new Button(mContext);
		bStar4.setId(200);
		bStar4.setBackgroundResource(R.drawable.bluestar);
		bStar4.setClickable(false);

		RelativeLayout.LayoutParams lpStar4 = new RelativeLayout.LayoutParams(widthStar, heightStar);
		lpStar4.addRule(RelativeLayout.RIGHT_OF, bStar3.getId());
		rlStars.addView(bStar4, lpStar4);

		Button bStar5 = new Button(mContext);
		bStar5.setId(250);
		bStar5.setBackgroundResource(R.drawable.bluestar);
		bStar5.setClickable(false);

		RelativeLayout.LayoutParams lpStar5 = new RelativeLayout.LayoutParams(widthStar, heightStar);
		lpStar5.addRule(RelativeLayout.RIGHT_OF, bStar4.getId());
		rlStars.addView(bStar5, lpStar5);

		return rlStars;
	}
}