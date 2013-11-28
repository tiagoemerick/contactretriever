package com.viisi.droid.contactretrieve.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

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

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
					dialog.dismiss();
					break;
				case DialogInterface.BUTTON_NEUTRAL:
					dialog.dismiss();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					if (editor != null) {
						editor.putBoolean(Constants.apprater.preference_dontshowagain, true);
						editor.commit();
					}
					dialog.dismiss();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setIcon(R.drawable.ic_rate);
		builder.setTitle(mContext.getString(R.string.label_rate_title));
		builder.setMessage(mContext.getString(R.string.label_rate_message_1) + " " + APP_TITLE + " " + mContext.getString(R.string.label_rate_message_2));
		builder.setPositiveButton(R.string.label_rate_rate, dialogClickListener);
		builder.setNeutralButton(R.string.label_rate_remind_later, dialogClickListener);
		builder.setNegativeButton(R.string.label_rate_no_thanks, dialogClickListener);

		builder.show();
	}
}