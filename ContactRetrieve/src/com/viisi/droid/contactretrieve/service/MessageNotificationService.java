package com.viisi.droid.contactretrieve.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.viisi.droid.contactretrieve.R;
import com.viisi.droid.contactretrieve.activity.ContactRetrieveActivity;

public class MessageNotificationService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();

		int notifID = extras.getInt("NotifID");
		String statusMessageURI = extras.getString("statusMessageURI");

		Intent i = new Intent(getBaseContext(), ContactRetrieveActivity.class);
		i.putExtra("NotifID", notifID);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent detailsIntent = PendingIntent.getActivity(this, 0, i, 0);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		StringBuilder tickerText = new StringBuilder();
		StringBuilder message = new StringBuilder();
		if (TextUtils.isEmpty(statusMessageURI) || statusMessageURI.compareTo(SMSManagerService.URI_SMS_FAILED) != 0) {
			tickerText.append(getResources().getString(R.string.app_name));
			tickerText.append(" - ");
			tickerText.append(getResources().getString(R.string.sms_send));

			message.append(getResources().getString(R.string.message_success_notification));
		} else {
			tickerText.append(getResources().getString(R.string.app_name));
			tickerText.append(" - ");
			tickerText.append(getResources().getString(R.string.sms_not_send));

			message.append(getResources().getString(R.string.message_error_notification));
		}
		String tickerTextString = tickerText.toString();

		Notification notif = new Notification(R.drawable.notification, tickerTextString, System.currentTimeMillis());
		notif.flags = Notification.FLAG_AUTO_CANCEL;

		notif.setLatestEventInfo(this, tickerTextString, message.toString(), detailsIntent);
		nm.notify(notifID, notif);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}