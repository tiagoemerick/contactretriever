package com.viisi.droid.contactretrieve.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.viisi.droid.contactretrieve.R;
import com.viisi.droid.contactretrieve.entity.Contact;
import com.viisi.droid.contactretrieve.entity.Password;
import com.viisi.droid.contactretrieve.sqlite.ContactRetrieveDS;
import com.viisi.droid.contactretrieve.sqlite.PasswordDAO;
import com.viisi.droid.contactretrieve.util.Constants;
import com.viisi.droid.contactretrieve.util.PDUUtil;
import com.viisi.droid.contactretrieve.util.SendSMSBugCorrection;
import com.viisi.droid.contactretrieve.util.StringUtil;

public class SMSManagerService extends Service {

	protected static final String URI_SMS_SENT = "content://sms/sent";
	protected static final String URI_SMS_FAILED = "content://sms/failed";
	protected static final String URI_SMS_INBOX = "content://sms/inbox";
	protected static final String URI_SMS_QUEUED = "content://sms/queued";
	protected static final String URI_SMS_DRAFT = "content://sms/draft";
	protected static final String URI_SMS_OUTBOX = "content://sms/outbox";
	protected static final String URI_SMS_UNDELIVERED = "content://sms/undelivered";
	protected static final String URI_SMS_ALL = "content://sms/all";
	protected static final String URI_SMS_CONVERSATIONS = "content://sms/conversations";

	private PendingIntent sentPI;
	private PendingIntent deliveredPI;

	private String statusMessageURI = URI_SMS_SENT;

	private ContactRetrieveDS contactRetrieveDS;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Bundle intentExtras = intent.getExtras();
			if (intentExtras != null) {
				String phoneNumber = intentExtras.getString(Constants.sendsms.celnumber);
				String message = intentExtras.getString(Constants.sendsms.textmessage);
				Long idPasswordUsed = intentExtras.getLong(Constants.sendsms.idpasswordused);
				String requestType = intentExtras.getString(Constants.sendsms.requesttype);
				
				if (isContentValid(phoneNumber, message)) {
					if (requestType != null && !requestType.equals("")) {
						String finalMessage = message;
						
						if (requestType.equalsIgnoreCase(Constants.sendsms.requesttype_contact)) {
							String nameToSearch = getNameToSearh(message);
							List<Contact> contactsInfo = getContactsInfo(nameToSearch);
							
							// TODO: remover
//							List<Contact> contactsInfo = teste(nameToSearch);
							
							String contactsMessage = getContactsMessage(contactsInfo, phoneNumber);
							finalMessage = contactsMessage;
							
							deletePasswordUsed(idPasswordUsed);
						} else if (requestType.equalsIgnoreCase(Constants.sendsms.requesttype_password)) {
							String messagePasswords = getMessagePasswords(message);
							finalMessage = messagePasswords;
						}
						sendSMS(phoneNumber, finalMessage);
					}
				}
			}
		}
		return super.onStartCommand(intent, PendingIntent.FLAG_ONE_SHOT, startId);
	}

	private List<Contact> teste(String nameToSearch) {
		Contact c = new Contact();
		c.setName(nameToSearch);
		
		List<Contact> cc = new ArrayList<Contact>();
		cc.add(c);
		
		return cc;
	}

	private String getMessagePasswords(String message) {
		StringBuilder formatedMessage = new StringBuilder(getApplicationContext().getString(R.string.sett_hint_passw) + "(s): ");
		int i = 0;

		@SuppressWarnings("unchecked")
		List<Password> allPasswords = (List<Password>) getContactRetrieveDSPassword().findAll();
		for (Password password : allPasswords) {
			formatedMessage.append(password.getPassw());
			if (i != (allPasswords.size() - 1)) {
				formatedMessage.append(", ");
			}
			i++;
		}
		String finalTextMessage = getMessageConvertedInPDU(formatedMessage.toString());

		return finalTextMessage.trim();
	}

	private void deletePasswordUsed(Long idPasswordUsed) {
		if (idPasswordUsed.compareTo(0l) != 0) {
			Password password = new Password();
			password.setId(idPasswordUsed);
			getContactRetrieveDSPassword().delete(password);
		}
	}

	/**
	 * Retorna a msg formatada para ser enviada
	 * 
	 * @param contactsInfo
	 * @param phoneNumber
	 * @return Ex: 1- Tiago: 91760438/33586574, 2- Iago: 99999999
	 */
	private String getContactsMessage(List<Contact> contactsInfo, String phoneNumber) {
		StringBuilder formatedMessage = new StringBuilder(getResources().getString(R.string.contact_not_found));
		String finalTextMessage = formatedMessage.toString();

		if (contactsInfo.size() > 0) {
			formatedMessage = new StringBuilder();
			int i = 0;
			for (Contact contact : contactsInfo) {
				formatedMessage.append((i + 1) + "- " + contact.getName() + ": ");
				int j = 0;
				for (String phone : contact.getPhones()) {
					formatedMessage.append(phone);
					if (j != (contact.getPhones().size() - 1)) {
						formatedMessage.append("/");
					}
					j++;
				}
				if (i != (contactsInfo.size() - 1)) {
					formatedMessage.append(", ");
				}
				i++;
			}
			finalTextMessage = getMessageConvertedInPDU(formatedMessage.toString());
		}
		return finalTextMessage.trim();
	}

	private String getMessageConvertedInPDU(String formatedMessage) {
		StringBuilder finalTextMessage = new StringBuilder();

		int numerOfPDUToConvert = (formatedMessage.length() / 12) + 1;
		String[] messagesToConvertInPDUMode = new String[numerOfPDUToConvert];

		StringBuilder messagesToConvertInPDUModeAux = new StringBuilder();
		int numberPositionPDU = 0;
		for (char ch : formatedMessage.toString().toCharArray()) {
			if (messagesToConvertInPDUModeAux.length() < 12) {
				messagesToConvertInPDUModeAux.append(ch);
				messagesToConvertInPDUMode[numberPositionPDU] = messagesToConvertInPDUModeAux.toString();
			} else {
				messagesToConvertInPDUModeAux = new StringBuilder();
				numberPositionPDU++;
				messagesToConvertInPDUMode[numberPositionPDU] = messagesToConvertInPDUModeAux.append(ch).toString();
			}
		}

		final String beginningPDUText = "07911326040000F0040B911346610089F60000208062917314080C";
		for (int k = 0; k < messagesToConvertInPDUMode.length; k++) {
			if (messagesToConvertInPDUMode[k] != null) {
				if (messagesToConvertInPDUMode[k].length() < 12) {
					int count = 12 - messagesToConvertInPDUMode[k].length();
					for (int j = 0; j < count; j++) {
						messagesToConvertInPDUMode[k] += " ";
					}
				}
				SmsMessage createFromPduTemp = SmsMessage.createFromPdu(PDUUtil.hexStringToByteArray(beginningPDUText + PDUUtil.getMessageInPDUFormat(messagesToConvertInPDUMode[k])));
				finalTextMessage.append(createFromPduTemp.getMessageBody());
			}
		}
		return finalTextMessage.toString();
	}

	private String getNameToSearh(String message) {
		final String STRING_CONTACT_REQUEST = new String(Constants.wildcards.string_contact_request + " ");
		final int STRING_CONTACT_REQUEST_LENGTH = STRING_CONTACT_REQUEST.length();

		return message.subSequence(STRING_CONTACT_REQUEST_LENGTH, message.length()).toString().trim();
	}

	private void saveOutboxSMS(String phoneNumber, String message) {
		ContentValues values = new ContentValues();
		values.put("address", phoneNumber);
		values.put("body", message);
		getContentResolver().insert(Uri.parse(statusMessageURI), values);
	}

	private boolean isContentValid(String phoneNumber, String message) {
		final String STRING_CONTACT_REQUEST = new String(Constants.wildcards.string_contact_request + " ");
		final int STRING_CONTACT_REQUEST_LENGTH = STRING_CONTACT_REQUEST.length();

		if (message.length() <= STRING_CONTACT_REQUEST_LENGTH) {
			return false;
		}
		return true;
	}

	private void addNotification() {
		Intent i = new Intent(getBaseContext(), MessageNotificationService.class);
		i.putExtra("NotifID", 1);
		i.putExtra("statusMessageURI", statusMessageURI);

		sentPI = PendingIntent.getService(getBaseContext(), 0, i, PendingIntent.FLAG_ONE_SHOT);
		try {
			sentPI.send();
		} catch (CanceledException e) {
		}
	}

	private void sendSMS(String phoneNumber, String message) {
		sentPI = registroMensagemEnviada(phoneNumber, message);
		deliveredPI = registroMensagemEntregue();

		SmsManager smsManager = SmsManager.getDefault();
		// doesnt work. there is a bug on android core. Its sending sms twice
		// smsManager.sendTextMessage(phoneNumber, null, message.trim(), sentPI, deliveredPI);

		SendSMSBugCorrection.send(phoneNumber, message.trim(), sentPI, deliveredPI, smsManager);
	}

	/**
	 * Cria um evento que sera acionado quando o serviço de mensagem tiver
	 * <b>enviado</b> o SMS
	 * 
	 * @param message
	 *            to save outbox
	 * @param phoneNumber
	 * 
	 * @return PendingIntent
	 */
	private PendingIntent registroMensagemEnviada(String phoneNumber, String message) {
		Random s = new Random(System.currentTimeMillis());
		StringBuilder SENT = new StringBuilder("SMS_SENT");
		SENT.append(s.nextLong());

		String SENT_STRING = SENT.toString();
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT_STRING), PendingIntent.FLAG_ONE_SHOT);

		final String phoneNumberFinal = phoneNumber;
		final String messageFinal = message;

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					statusMessageURI = URI_SMS_SENT;
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					statusMessageURI = URI_SMS_FAILED;
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					statusMessageURI = URI_SMS_FAILED;
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					statusMessageURI = URI_SMS_FAILED;
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					statusMessageURI = URI_SMS_FAILED;
					break;
				}
				addNotification();
				saveOutboxSMS(phoneNumberFinal, messageFinal);
			}
		}, new IntentFilter(SENT_STRING));

		return sentPI;
	}

	/**
	 * Cria um evento que sera acionado quando o serviço de mensagem tiver
	 * <b>entregue</b> o SMS
	 * 
	 * @return PendingIntent
	 */
	private PendingIntent registroMensagemEntregue() {
		Random s = new Random(System.currentTimeMillis());
		StringBuilder DELIVERED = new StringBuilder("SMS_DELIVERED");
		DELIVERED.append(s.nextLong());

		String DELIVERED_STRING = DELIVERED.toString();

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED_STRING), PendingIntent.FLAG_ONE_SHOT);

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					break;
				case Activity.RESULT_CANCELED:
					break;
				}
			}
		}, new IntentFilter(DELIVERED_STRING));

		return deliveredPI;
	}

	private List<Contact> getContactsInfo(String nameToSearch) {
		List<Contact> contacts = new ArrayList<Contact>();

		String patternWithoutVogal = "[ÀÁáàÉÈéèÍíÓóÒòÚúAaEeIiOoUu]";
		String nameToSearchWithoutVogal = nameToSearch.replaceAll(patternWithoutVogal, "%");

		// String columLower = " LOWER(TRIM(REPLACE(" +
		// ContactsContract.Contacts.DISPLAY_NAME +
		// ",'ÀÁáàÉÈéèÍíÓóÒòÚú','AAaaEEeeIiOoOoUu')))";
		String columLower = ContactsContract.Contacts.DISPLAY_NAME;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE";

		String where = columLower + " LIKE " + "'%" + nameToSearchWithoutVogal + "%' ";
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, where, null, sortOrder);

		while (cursor.moveToNext()) {
			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1")) {
				hasPhone = "true";

				String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

				// >= android-api-10
				// String normalizedDB = Normalizer.normalize(name, Normalizer.Form.NFD);
				String normalizedDB = StringUtil.substituteAccents(name);
				String nameNormalizedWithouAccents = normalizedDB.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").trim().toLowerCase();

				// String normalizedNameToSearch = Normalizer.normalize(nameToSearch, Normalizer.Form.NFD);
				String normalizedNameToSearch = StringUtil.substituteAccents(nameToSearch);
				String nameToSearchNormalizedWithouAccents = normalizedNameToSearch.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").trim().toLowerCase();

				if (nameNormalizedWithouAccents.contains(nameToSearchNormalizedWithouAccents)) {
					Contact contact = new Contact();

					String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
					contact.setId(Long.valueOf(contactId));

					contact.setName(name);

					if (Boolean.parseBoolean(hasPhone)) {
						Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
						while (phones.moveToNext()) {
							StringBuilder formatedPhoneNumber = new StringBuilder();
							formatedPhoneNumber.append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
							contact.getPhones().add(formatedPhoneNumber.toString());
						}
						phones.close();
					}
					contacts.add(contact);
				}
			}
		}
		cursor.close();
		return contacts;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private ContactRetrieveDS getContactRetrieveDSPassword() {
		if (contactRetrieveDS == null) {
			contactRetrieveDS = new PasswordDAO(getApplicationContext());
		}
		return contactRetrieveDS instanceof PasswordDAO ? contactRetrieveDS : (contactRetrieveDS = new PasswordDAO(getApplicationContext()));
	}

}