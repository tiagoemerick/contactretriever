package com.viisi.droid.contactretrieve.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sileria.android.Kit;
import com.sileria.android.SlidingTray;
import com.sileria.android.SlidingTray.OnDrawerOpenListener;
import com.viisi.droid.contactretrieve.R;
import com.viisi.droid.contactretrieve.entity.Password;
import com.viisi.droid.contactretrieve.sqlite.ContactRetrieveDS;
import com.viisi.droid.contactretrieve.sqlite.PasswordDAO;
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

	// Panels
	private TextView prefPasswords;
	private TextView prefManage;

	private EditText textPassw;
	private Button addPassw;
	private SlidingTray drawerListPassw;
	private TextView textMailLabel;
	private EditText textNewMail;
	private Button changeMail;
	private TextView textNewMasterPassw;
	private Button changeMasterPassw;
	private Button delAll;

	private boolean needToSearch;
	private List<String> passws;
	private ArrayAdapter<String> adapter;
	private Map<String, Long> passwMap;

	private ContactRetrieveDS contactRetrieveDS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Kit.init(getApplicationContext());

		int layout = R.layout.preferences_layout;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			layout = extras.getInt(Constants.layout.string_layout_preferences);
		}
		setContentView(layout);
		createComponents(layout);
	}

	private void showActualMail() {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
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
		}
	};

	private OnClickListener changeMailListener = new OnClickListener() {
		public void onClick(View v) {
			if (textNewMail != null && textNewMail.getText() != null && !textNewMail.getText().toString().trim().equals("")) {
				SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
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
				SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
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
					clearPasswordInputPreference(textPassw);
					closeDrawerListPassw();

					Message msg = new Message();
					handler.sendMessage(msg);
				}
			});
		}

		private ProgressDialog createProgressDialog() {
			final ProgressDialog dialog = ProgressDialog.show(PreferencesActivity.this, getApplicationContext().getString(R.string.app_name), getApplicationContext().getString(R.string.label_loading_message), false, false);
			dialog.setIcon(R.drawable.ic_launcher);
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

		private void clearPasswordInputPreference(EditText textPassw) {
			textPassw.setText("");
		}

		private void closeDrawerListPassw() {
			drawerListPassw.animateClose();
		}
	};

	private OnDrawerOpenListener listPasswordListener = new OnDrawerOpenListener() {

		@SuppressWarnings("unchecked")
		public void onDrawerOpened() {
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
		};
	};

	private ContactRetrieveDS getContactRetrieveDSPassword() {
		if (contactRetrieveDS == null) {
			contactRetrieveDS = new PasswordDAO(getBaseContext());
		}
		return contactRetrieveDS instanceof PasswordDAO ? contactRetrieveDS : (contactRetrieveDS = new PasswordDAO(getBaseContext()));
	}

	private void createComponents(int layout) {
		if (layout == R.layout.preferences_layout) {
			createComponentsViewPreferences();
			createComponentsListenersPreferences();
		} else if (layout == R.layout.password_preferences_layout) {
			createComponentsViewPasswordsPreferences();
			createComponentsListenersPasswordsPreferences();
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
		addPassw = (Button) this.findViewById(R.id.addPassw);
		delAll = (Button) this.findViewById(R.id.delAll);
		drawerListPassw = (SlidingTray) findViewById(R.id.drawer);
	}

	private void createComponentsListenersPasswordsPreferences() {
		addPassw.setOnClickListener(addPasswordListener);
		delAll.setOnClickListener(delPasswordListener);
		drawerListPassw.setOnDrawerOpenListener(listPasswordListener);
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