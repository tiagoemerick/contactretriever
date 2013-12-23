package com.viisi.droid.contactretrieve.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.viisi.droid.contactretrieve.R;

public class AlertNotification {
	private static String APP_TITLE;

	public static void showRateDialog(final Context mContext) {
		if (APP_TITLE == null) {
			APP_TITLE = mContext.getString(R.string.app_name);
		}

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://android-developers.blogspot.com.ar/2013/10/getting-your-sms-apps-ready-for-kitkat.html")));
					dialog.dismiss();
					break;
				case DialogInterface.BUTTON_NEUTRAL:
					dialog.dismiss();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setIcon(R.drawable.ic_warning);
		builder.setTitle(mContext.getString(R.string.label_alert_notification_title));
		builder.setMessage(mContext.getString(R.string.label_alert_notification));
		builder.setNeutralButton(R.string.label_gotit, dialogClickListener);
		builder.setPositiveButton(R.string.label_readmore, dialogClickListener);

		builder.show();
	}
}