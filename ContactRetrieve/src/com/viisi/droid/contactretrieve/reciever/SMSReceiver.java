package com.viisi.droid.contactretrieve.reciever;

import java.util.List;
import java.util.Random;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.viisi.droid.contactretrieve.entity.Password;
import com.viisi.droid.contactretrieve.service.SMSManagerService;
import com.viisi.droid.contactretrieve.sqlite.ContactRetrieveDS;
import com.viisi.droid.contactretrieve.sqlite.PasswordDAO;
import com.viisi.droid.contactretrieve.util.Constants;

public class SMSReceiver extends BroadcastReceiver {

	private ContactRetrieveDS contactRetrieveDS;

	private Long idPasswordUsed;
	private String finalMessage;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		StringBuilder originalMessage = new StringBuilder();
		String originalPhone = null;

		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				if (i == 0) {
					originalPhone = msgs[i].getOriginatingAddress();
				}
				originalMessage.append(msgs[i].getMessageBody().toString());
				if (i != (msgs.length - 1)) {
					originalMessage.append("\n");
				}
			}

			String originalMessageString = originalMessage.toString();
			if (isMessageContactRequest(originalMessageString)) {
				String requestType = null;

				if (isMessagePasswordsRequest(originalMessageString)) {
					if (validateMasterPassword(context, originalMessageString)) {
						requestType = Constants.sendsms.requesttype_password;
						sendSMS(originalPhone, originalMessageString.trim(), requestType, context);
					}
				} else {
					if (validatePasswords(context, originalMessageString)) {
						if (finalMessage != null && !finalMessage.equals("")) {
							originalMessageString = finalMessage;
						}
						requestType = Constants.sendsms.requesttype_contact;
						sendSMS(originalPhone, originalMessageString.trim(), requestType, context);
						incrementRateLauncherCounter(context);
					}
				}
			}
		}
	}

	private boolean validateMasterPassword(Context context, String originalMessageString) {
		String[] msg = originalMessageString.split("\\" + Constants.wildcards.string_password_request);
		String masterPaswMsg = msg[1];
		if (masterPaswMsg != null && !masterPaswMsg.trim().equals("")) {
			SharedPreferences prefs = context.getSharedPreferences(Constants.preferences.preferencefilename, 0);
			String masterPassw = prefs.getString(Constants.preferences.preference_master_password, "");
			if (masterPassw != null && !masterPassw.equals("")) {
				if (masterPaswMsg.equalsIgnoreCase(masterPassw)) {
					int count = getContactRetrieveDSPassword(context).count();
					if (count > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isMessagePasswordsRequest(String originalMessage) {
		if (!TextUtils.isEmpty(originalMessage) && originalMessage.contains(Constants.wildcards.string_password_request)) {
			return true;
		}
		return false;
	}

	private boolean validatePasswords(Context context, String originalMessage) {
		if (originalMessageHasMinimumCharacters(originalMessage)) {
			@SuppressWarnings("unchecked")
			List<Password> passwords = (List<Password>) getContactRetrieveDSPassword(context).findAll();
			if (passwords == null || passwords.size() == 0) {
				if (originalMessage.contains(Constants.wildcards.string_password_separator)) {
					return false;
				} else {
					return true;
				}
			} else {
				if (originalMessage.contains(Constants.wildcards.string_password_separator)) {
					String[] msg = originalMessage.split(Constants.wildcards.string_password_separator);
					String senha = msg[1];
					originalMessage = msg[0].trim();
					finalMessage = msg[0].trim();
					if (!senha.equals("")) {
						for (Password password : passwords) {
							if (password.getPassw() != null) {
								if (password.getPassw().equalsIgnoreCase(senha)) {
									idPasswordUsed = password.getId();
									return true;
								}
							}
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private boolean originalMessageHasMinimumCharacters(String originalMessage) {
		int MINIMUM_CHARACTERS_TO_SEARCH = 3;
		final String STRING_CONTACT_REQUEST = new String(Constants.wildcards.string_contact_request + " ");
		final int STRING_CONTACT_REQUEST_LENGTH = STRING_CONTACT_REQUEST.length();

		String message = originalMessage.subSequence(STRING_CONTACT_REQUEST_LENGTH, originalMessage.length()).toString().trim();
		String[] messageSplit = message.split(Constants.wildcards.string_password_separator);
		String name = messageSplit[0].trim();
		if (name.length() >= MINIMUM_CHARACTERS_TO_SEARCH) {
			return true;
		}
		return false;
	}

	private void incrementRateLauncherCounter(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.apprater.preferencefilename, 0);
		SharedPreferences.Editor editor = prefs.edit();

		long launch_count = prefs.getLong(Constants.apprater.preference_launch_count, 0) + 1;
		editor.putLong(Constants.apprater.preference_launch_count, launch_count);
		editor.commit();
	}

	private boolean isMessageContactRequest(String originalMessage) {
		if (!TextUtils.isEmpty(originalMessage) && originalMessage.startsWith(Constants.wildcards.string_contact_request)) {
			return true;
		}
		return false;
	}

	private void sendSMS(String phoneNumber, String message, String requestType, Context context) {
		ContextWrapper cw = new ContextWrapper(context);
		Context baseContext = cw.getBaseContext();
		
		Random s = new Random(System.currentTimeMillis());
		String START_SENDING = "START_SENDING" + s.nextLong();

		Intent intentSMS = new Intent(START_SENDING, null, baseContext, SMSManagerService.class);
		intentSMS.putExtra(Constants.sendsms.celnumber, phoneNumber);
		intentSMS.putExtra(Constants.sendsms.textmessage, message);
		intentSMS.putExtra(Constants.sendsms.idpasswordused, idPasswordUsed != null ? idPasswordUsed : 0);
		intentSMS.putExtra(Constants.sendsms.requesttype, requestType);

		PendingIntent pendingIntent = PendingIntent.getService(baseContext, 0, intentSMS, PendingIntent.FLAG_ONE_SHOT);
		try {
			pendingIntent.send();
		} catch (CanceledException e) {
		}
//		 cw.startService(intentSMS);
	}

	private ContactRetrieveDS getContactRetrieveDSPassword(Context context) {
		if (contactRetrieveDS == null) {
			contactRetrieveDS = new PasswordDAO(context);
		}
		return contactRetrieveDS instanceof PasswordDAO ? contactRetrieveDS : (contactRetrieveDS = new PasswordDAO(context));
	}

}