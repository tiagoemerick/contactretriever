package com.viisi.droid.contactretrieve.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManagerHelper;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.viisi.droid.contactretrieve.R;
import com.viisi.droid.contactretrieve.entity.Contact;
import com.viisi.droid.contactretrieve.entity.Password;
import com.viisi.droid.contactretrieve.entity.TrustedNumber;
import com.viisi.droid.contactretrieve.sqlite.ContactRetrieveDS;
import com.viisi.droid.contactretrieve.sqlite.PasswordDAO;
import com.viisi.droid.contactretrieve.sqlite.TrustedNumberDAO;
import com.viisi.droid.contactretrieve.util.Constants;

public class PreferencesActivity extends Activity {

	private static final class HandlerExtension extends Handler {
		private final ProgressDialog dialog;

		private HandlerExtension(ProgressDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		public void handleMessage(Message msg) {
			dialog.dismiss();
		}
	}

	private static final int PICK_CONTACT = 1;
	private Contact contact;

	// Panels
	private TextView prefPasswords;
	private TextView prefManage;

	private EditText textPassw;
	private EditText textTrustedNumber;
	private Button addPassw;
	private TextView textMailLabel;
	private EditText textNewMail;
	private Button changeMail;
	private TextView textNewMasterPassw;
	private Button changeMasterPassw;
	private Button delAll;
	private Button delAllTrutedNumbers;
	private Button addTrustedNumber;
	private Button findContact;

	private boolean needToSearch;
	private List<String> passws;
	private ArrayAdapter<String> adapter;
	private Map<String, Long> passwMap;

	private boolean needToSearchNumbers;
	private List<String> numbers;
	private ArrayAdapter<String> adapterNumbers;
	private Map<String, Long> numbersMap;

	private ContactRetrieveDS contactRetrieveDS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int layout = R.layout.preferences_layout;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			layout = extras.getInt(Constants.layout.string_layout_preferences);
		}
		setContentView(layout);
		createComponents(layout);
	}

	private void showActualMail() {
		SharedPreferences prefs = PreferenceManagerHelper.wrap(getApplicationContext(), Constants.preferences.preferencefilename, 0);
		String actualMail = prefs.getString(Constants.preferences.preference_mail, "");

		StringBuilder text = new StringBuilder();
		text.append(getApplicationContext().getString(R.string.sett_text_change_mail));
		text.append(": ");
		text.append(actualMail);

		textMailLabel.setText(text.toString());
	}

	private OnClickListener goViewPasswords = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), PreferencesActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.password_preferences_layout);
			startActivity(i);
		}
	};

	private OnClickListener goViewManage = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), PreferencesActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.manage_preferences_layout);
			startActivity(i);
		}
	};

	private OnClickListener delPasswordListener = new OnClickListener() {
		public void onClick(View v) {
			getContactRetrieveDSPassword().deleteAll();
			if (adapter != null) {
				adapter.clear();
			}
			loadPasswordsDB();
		}
	};

	private OnClickListener delAllTrutedNumbersListenerListener = new OnClickListener() {
		public void onClick(View v) {
			getContactRetrieveDSTrustedNumber().deleteAll();
			if (adapterNumbers != null) {
				adapterNumbers.clear();
			}
			loadTrustedNumbersDB();
		}
	};

	private OnClickListener findContactListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		}
	};

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		// super.onActivityResult(reqCode, resultCode, data);

		if (reqCode == PICK_CONTACT) {
			if (resultCode == Activity.RESULT_OK) {
				getContactInfo(data);

				// Mais de um telefone no contato
				if (contact.getPhones().size() > 1) {
					final CharSequence[] items = new CharSequence[contact.getPhones().size()];

					int i = 0;
					for (String phone : contact.getPhones()) {
						items[i] = phone;
						i++;
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(contact.getName());
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							String numberP = new String(items[item].toString());
							textTrustedNumber.setText(numberP.replaceAll("\\D", ""));
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				} else if (contact.getPhones().size() == 1) {
					String numberP = new String(contact.getPhones().get(0));
					textTrustedNumber.setText(numberP.replaceAll("\\D", ""));
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void getContactInfo(Intent intent) {
		contact = new Contact();

		Cursor cursor = managedQuery(intent.getData(), null, null, null, null);
		stopManagingCursor(cursor);
		while (cursor.moveToNext()) {
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			contact.setId(Long.valueOf(contactId));

			String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
			contact.setName(name);

			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (hasPhone.equalsIgnoreCase("1")) {
				hasPhone = "true";
			} else {
				hasPhone = "false";
			}

			if (Boolean.parseBoolean(hasPhone)) {
				Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
				while (phones.moveToNext()) {
					StringBuilder formatedPhoneNumber = new StringBuilder();
					formatedPhoneNumber.append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

					final String data_type = "data2";
					int type = phones.getInt(phones.getColumnIndex(data_type));

					switch (type) {
					case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
						final String data_label_name = "data3";
						formatedPhoneNumber.append(" - ");
						formatedPhoneNumber.append(phones.getString(phones.getColumnIndex(data_label_name)));
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
						formatedPhoneNumber.append(" - Fax");
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
						formatedPhoneNumber.append(" - Fax");
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
						formatedPhoneNumber.append(" - ");
						formatedPhoneNumber.append(getResources().getString(R.string.type_phone_home));
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
						formatedPhoneNumber.append(" - ");
						formatedPhoneNumber.append(getResources().getString(R.string.type_phone_mobile));
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
						formatedPhoneNumber.append(" - ");
						formatedPhoneNumber.append(getResources().getString(R.string.type_phone_work));
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
						formatedPhoneNumber.append(" - ");
						formatedPhoneNumber.append(getResources().getString(R.string.type_phone_mobile_work));
						break;
					}
					contact.getPhones().add(formatedPhoneNumber.toString());
				}
				phones.close();
			}
		}
		// close for that: stopManagingCursor(cursor);
		cursor.close();
	}

	private OnClickListener addTrustedNumberListener = new OnClickListener() {
		public void onClick(View v) {
			final ProgressDialog dialog = createProgressDialog();

			final Handler handler = new HandlerExtension(dialog);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					insertTrustedNumberDB(textTrustedNumber);
					textTrustedNumber.setText("");
					loadTrustedNumbersDB();

					Message msg = new Message();
					handler.sendMessage(msg);
				}
			});
		}

		private ProgressDialog createProgressDialog() {
			final ProgressDialog dialog = ProgressDialog.show(PreferencesActivity.this, null, getApplicationContext().getString(R.string.label_loading_message), false, false);
			return dialog;
		}

		private void insertTrustedNumberDB(EditText textPassw) {
			String valueToInsert = textPassw.getText().toString();
			if (valueToInsert != null && !valueToInsert.trim().equalsIgnoreCase("")) {
				getContactRetrieveDSTrustedNumber().insert(valueToInsert);
				needToSearchNumbers = true;
			} else {
				Toast.makeText(getBaseContext(), R.string.sett_toast_invalid_passw, Toast.LENGTH_LONG).show();
			}
		}
	};

	private OnClickListener changeMailListener = new OnClickListener() {
		public void onClick(View v) {
			if (textNewMail != null && textNewMail.getText() != null && !textNewMail.getText().toString().trim().equals("")) {
				SharedPreferences prefs = PreferenceManagerHelper.wrap(getApplicationContext(), Constants.preferences.preferencefilename, 0);
				SharedPreferences.Editor editor = prefs.edit();

				editor.putString(Constants.preferences.preference_mail, textNewMail.getText().toString());
				editor.commit();

				StringBuilder text = new StringBuilder();
				text.append(getApplicationContext().getString(R.string.sett_text_change_mail));
				text.append(": ");
				text.append(textNewMail.getText().toString());

				textMailLabel.setText(text.toString());
				textNewMail.setText("");

				Toast.makeText(getBaseContext(), R.string.sett_change_mail_ok, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener changeMasterPasswListener = new OnClickListener() {
		public void onClick(View v) {
			if (textNewMasterPassw != null && textNewMasterPassw.getText() != null && !textNewMasterPassw.getText().toString().trim().equals("")) {
				SharedPreferences prefs = PreferenceManagerHelper.wrap(getApplicationContext(), Constants.preferences.preferencefilename, 0);
				SharedPreferences.Editor editor = prefs.edit();

				editor.putString(Constants.preferences.preference_master_password, textNewMasterPassw.getText().toString());
				editor.commit();

				textNewMasterPassw.setText("");

				Toast.makeText(getBaseContext(), R.string.sett_change_mail_ok, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener addPasswordListener = new OnClickListener() {
		public void onClick(View v) {
			final ProgressDialog dialog = createProgressDialog();

			final Handler handler = new HandlerExtension(dialog);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					insertPasswordDB(textPassw);
					textPassw.setText("");
					loadPasswordsDB();

					Message msg = new Message();
					handler.sendMessage(msg);
				}
			});
		}

		private ProgressDialog createProgressDialog() {
			final ProgressDialog dialog = ProgressDialog.show(PreferencesActivity.this, null, getApplicationContext().getString(R.string.label_loading_message), false, false);
			return dialog;
		}

		private void insertPasswordDB(EditText textPassw) {
			String valueToInsert = textPassw.getText().toString();
			if (valueToInsert != null && !valueToInsert.trim().equalsIgnoreCase("")) {
				getContactRetrieveDSPassword().insert(valueToInsert);
				needToSearch = true;
			} else {
				Toast.makeText(getBaseContext(), R.string.sett_toast_invalid_passw, Toast.LENGTH_LONG).show();
			}
		}
	};

	public static void updateListViewHeight(ListView myListView) {
		ListAdapter myListAdapter = myListView.getAdapter();
		if (myListAdapter == null) {
			return;
		}

		int totalHeight = 0;
		int adapterCount = myListAdapter.getCount();
		for (int size = 0; size < adapterCount; size++) {
			View listItem = myListAdapter.getView(size, null, myListView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = myListView.getLayoutParams();
		int newHeight = totalHeight + (myListView.getDividerHeight() * (adapterCount - 1));
		final int DP_255 = 377;
		if (newHeight < DP_255) {
			params.height = newHeight;
		} else {
			params.height = DP_255;
		}
		myListView.setLayoutParams(params);
	}

	@SuppressWarnings("unchecked")
	private void loadPasswordsDB() {
		ListView listView = (ListView) findViewById(R.id.mylist);

		if (passws == null || passws.size() == 0 || needToSearch) {
			List<Password> passwords = (List<Password>) getContactRetrieveDSPassword().findAll();
			passwMap = new HashMap<String, Long>(passwords.size());

			passws = new ArrayList<String>();
			for (Password password : passwords) {
				passws.add(password.getPassw());
				passwMap.put(password.getPassw(), password.getId());
			}
			needToSearch = true;
		}

		if (needToSearch) {
			adapter = new ArrayAdapter<String>(PreferencesActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, passws);
			needToSearch = false;
		}

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Integer pos = position;

				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							String itemPassw = adapter.getItem(pos);

							Password password = new Password();
							password.setId(passwMap.get(itemPassw));

							getContactRetrieveDSPassword().delete(password);

							adapter.remove(itemPassw);
							loadPasswordsDB();
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							dialog.cancel();
						}
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
				builder.setMessage(getApplicationContext().getString(R.string.sett_message_passw_delete)).setPositiveButton(getApplicationContext().getString(R.string.label_yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.label_no), dialogClickListener).show();
			}
		});
		updateListViewHeight(listView);
	}

	@SuppressWarnings("unchecked")
	private void loadTrustedNumbersDB() {
		ListView listView = (ListView) findViewById(R.id.listTrustedNumbers);

		if (numbers == null || numbers.size() == 0 || needToSearchNumbers) {
			List<TrustedNumber> tNumbers = (List<TrustedNumber>) getContactRetrieveDSTrustedNumber().findAll();
			numbersMap = new HashMap<String, Long>(tNumbers.size());

			numbers = new ArrayList<String>();
			for (TrustedNumber number : tNumbers) {
				numbers.add(number.getNumber());
				numbersMap.put(number.getNumber(), number.getId());
			}
			needToSearchNumbers = true;
		}

		if (needToSearchNumbers) {
			adapterNumbers = new ArrayAdapter<String>(PreferencesActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, numbers);
			needToSearchNumbers = false;
		}

		listView.setAdapter(adapterNumbers);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Integer pos = position;

				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							String itemPassw = adapterNumbers.getItem(pos);

							TrustedNumber number = new TrustedNumber();
							number.setId(numbersMap.get(itemPassw));

							getContactRetrieveDSTrustedNumber().delete(number);

							adapterNumbers.remove(itemPassw);
							loadTrustedNumbersDB();
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							dialog.cancel();
						}
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
				builder.setMessage(getApplicationContext().getString(R.string.sett_message_passw_delete)).setPositiveButton(getApplicationContext().getString(R.string.label_yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.label_no), dialogClickListener).show();
			}
		});
		updateListViewHeight(listView);
	}

	private ContactRetrieveDS getContactRetrieveDSPassword() {
		if (contactRetrieveDS == null) {
			contactRetrieveDS = new PasswordDAO(getBaseContext());
		}
		return contactRetrieveDS instanceof PasswordDAO ? contactRetrieveDS : (contactRetrieveDS = new PasswordDAO(getBaseContext()));
	}

	private ContactRetrieveDS getContactRetrieveDSTrustedNumber() {
		if (contactRetrieveDS == null) {
			contactRetrieveDS = new TrustedNumberDAO(getBaseContext());
		}
		return contactRetrieveDS instanceof TrustedNumberDAO ? contactRetrieveDS : (contactRetrieveDS = new TrustedNumberDAO(getBaseContext()));
	}

	private void createComponents(int layout) {
		if (layout == R.layout.preferences_layout) {
			createComponentsViewPreferences();
			createComponentsListenersPreferences();
		} else if (layout == R.layout.password_preferences_layout) {
			createComponentsViewPasswordsPreferences();
			createComponentsListenersPasswordsPreferences();
			loadPasswordsDB();
			loadTrustedNumbersDB();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (layout == R.layout.manage_preferences_layout) {
			createComponentsViewManagePreferences();
			createComponentsListenersManagePreferences();
			showActualMail();
		}
	}

	private void createComponentsViewPreferences() {
		prefPasswords = (TextView) findViewById(R.id.prefPasswords);
		prefManage = (TextView) findViewById(R.id.prefManage);
	}

	private void createComponentsListenersPreferences() {
		prefPasswords.setOnClickListener(goViewPasswords);
		prefManage.setOnClickListener(goViewManage);
	}

	private void createComponentsViewPasswordsPreferences() {
		textPassw = (EditText) findViewById(R.id.textPassw);
		textTrustedNumber = (EditText) findViewById(R.id.textTrustedNumber);
		addPassw = (Button) this.findViewById(R.id.addPassw);
		delAll = (Button) this.findViewById(R.id.delAll);
		delAllTrutedNumbers = (Button) this.findViewById(R.id.delAllTrutedNumbers);
		findContact = (Button) this.findViewById(R.id.findContact);
		addTrustedNumber = (Button) this.findViewById(R.id.addTrustedNumber);
	}

	private void createComponentsListenersPasswordsPreferences() {
		addPassw.setOnClickListener(addPasswordListener);
		delAll.setOnClickListener(delPasswordListener);
		delAllTrutedNumbers.setOnClickListener(delAllTrutedNumbersListenerListener);
		addTrustedNumber.setOnClickListener(addTrustedNumberListener);
		findContact.setOnClickListener(findContactListener);
	}

	private void createComponentsViewManagePreferences() {
		textMailLabel = (TextView) findViewById(R.id.textMailLabel);
		textNewMail = (EditText) findViewById(R.id.textNewMail);
		changeMail = (Button) this.findViewById(R.id.changeMail);
		textNewMasterPassw = (TextView) findViewById(R.id.textNewMasterPassw);
		changeMasterPassw = (Button) this.findViewById(R.id.changeMasterPassw);
	}

	private void createComponentsListenersManagePreferences() {
		changeMail.setOnClickListener(changeMailListener);
		changeMasterPassw.setOnClickListener(changeMasterPasswListener);
	}

}