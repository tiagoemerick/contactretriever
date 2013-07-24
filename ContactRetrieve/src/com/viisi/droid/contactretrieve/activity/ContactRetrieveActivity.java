package com.viisi.droid.contactretrieve.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.viisi.droid.contactretrieve.R;
import com.viisi.droid.contactretrieve.entity.Password;
import com.viisi.droid.contactretrieve.sqlite.ContactRetrieveDS;
import com.viisi.droid.contactretrieve.sqlite.PasswordDAO;
import com.viisi.droid.contactretrieve.util.Constants;
import com.viisi.droid.contactretrieve.util.Mail;

public class ContactRetrieveActivity extends Activity {

	/**
	 * http://stackoverflow.com/questions/11407943/this-handler-class-should-be-
	 * static-or-leaks-might-occur-incominghandler
	 * 
	 * o handlerMessage fica aguardando respostas na sua fila de mensagem. Se
	 * ele não for statico, ele irá guardar a referência ao objeto
	 * ContactRetrieveActivity e com isso o ContactRetrieveActivity nunca será
	 * limpado pelo GC, mesmo que tenha sido destruído. Ficará com ele em
	 * memória e poderá causar memory leak.
	 * 
	 */
	private static final class HandlerExtension extends Handler {
		private final ProgressDialog sendProgress;
		private final ContactRetrieveActivity contactRetrieveActivityContext;

		private HandlerExtension(ProgressDialog sendProgress, ContactRetrieveActivity contactRetrieveActivityContext) {
			this.sendProgress = sendProgress;
			this.contactRetrieveActivityContext = contactRetrieveActivityContext;
		}

		@Override
		public void handleMessage(Message msg) {
			Bundle data = msg.getData();
			Boolean statusSender = (Boolean) data.get(Constants.mail.mail_send_status);

			AlertDialog.Builder builder = new AlertDialog.Builder(contactRetrieveActivityContext);
			builder.setCancelable(false);

			if (statusSender == null || !statusSender) {
				builder.setMessage(contactRetrieveActivityContext.getString(R.string.send_mail_status_error));
			} else {
				builder.setMessage(contactRetrieveActivityContext.getString(R.string.send_mail_status_ok));
			}

			builder.setPositiveButton(contactRetrieveActivityContext.getString(R.string.label_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			sendProgress.dismiss();

			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	private static final int ID_EDIT_TEXT_MASTER_PASSW_SETTINGS = 25;
	private static final int ID_EDIT_TEXT_MASTER_PASSW = 30;
	private static final int ID_EDIT_TEXT_MAIL = 35;

	private Button settings;
	private Button createMasterPassw;
	private Button recoverMasterPassw;
	private Button help;
	private Button rate;

	private boolean masterPasswValidated;

	private ContactRetrieveDS contactRetrieveDS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		masterPasswValidated = false;

		createComponentsView();
		createComponentsListeners();
		showHideComponents();
		showMessageEmptyPasswords();

		AppRater.app_launched(this);
	}

	/**
	 * Apenas para teste. Fazer a chamada no onCreate
	 */
	@SuppressWarnings("unused")
	private void resetValues() {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.preferences.preference_master_password, "");
		editor.putString(Constants.preferences.preference_mail, "");

		editor.commit();
	}

	private void showHideComponents() {
		showHideCreateMasterPassw();
		showHideRecoverMasterPassw();
	}

	private void showMessageEmptyPasswords() {
		if (hasMasterPasswPreferences()) {
			@SuppressWarnings("unchecked")
			List<Password> passwords = (List<Password>) getContactRetrieveDSPassword().findAll();
			if (passwords == null || passwords.size() == 0) {
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							dialog.dismiss();
							break;
						}
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(ContactRetrieveActivity.this);
				builder.setMessage(getApplicationContext().getString(R.string.message_empty_passwords)).setPositiveButton(getApplicationContext().getString(R.string.label_ok), dialogClickListener).show();
			}
		}
	}

	private void showHideRecoverMasterPassw() {
		if (hasMasterPasswPreferences()) {
			recoverMasterPassw.setVisibility(ViewGroup.VISIBLE);
		} else {
			recoverMasterPassw.setVisibility(ViewGroup.GONE);
		}
	}

	private void showHideCreateMasterPassw() {
		if (!hasMasterPasswPreferences()) {
			createMasterPassw.setVisibility(ViewGroup.VISIBLE);
		} else {
			createMasterPassw.setVisibility(ViewGroup.GONE);
		}
	}

	private OnClickListener helpListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent i = new Intent(getBaseContext(), HelpActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener rateListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			AppRater.showRateDialog(ContactRetrieveActivity.this, null);
		}
	};

	private OnClickListener settingsListener = new OnClickListener() {
		public void onClick(View v) {
			if (hasMasterPasswPreferences()) {
				if (masterPasswValidated) {
					startPreferences();
				} else {
					final Dialog dialog = new Dialog(ContactRetrieveActivity.this);
					dialog.setTitle(getApplicationContext().getString(R.string.label_type_master_password));

					LinearLayout ll = new LinearLayout(ContactRetrieveActivity.this);
					ll.setOrientation(LinearLayout.VERTICAL);

					final EditText masterPassw = new EditText(ContactRetrieveActivity.this);
					masterPassw.setId(ID_EDIT_TEXT_MASTER_PASSW_SETTINGS);
					masterPassw.setWidth(50);
					masterPassw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					ll.addView(masterPassw);

					Button validate = new Button(ContactRetrieveActivity.this);
					validate.setId(5);
					validate.setText(getApplication().getString(R.string.label_enter));
					validate.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
							String masterPasswP = prefs.getString(Constants.preferences.preference_master_password, "");

							if (masterPassw != null && masterPassw.getText() != null && !masterPassw.getText().toString().equals("")) {
								if (!masterPasswP.equals("") && masterPasswP.equalsIgnoreCase(masterPassw.getText().toString())) {
									masterPasswValidated = true;

									dialog.dismiss();
									startPreferences();
								} else {
									Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.message_wrong_passw), Toast.LENGTH_SHORT).show();
								}
							} else {
								Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.label_invalid_value), Toast.LENGTH_SHORT).show();
							}
						}
					});

					ll.addView(validate);

					dialog.setContentView(ll);
					dialog.show();
				}

			} else {
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							dialog.dismiss();
							break;
						}
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(ContactRetrieveActivity.this);
				builder.setMessage(getApplicationContext().getString(R.string.message_master_passw_required)).setPositiveButton(getApplicationContext().getString(R.string.label_ok), dialogClickListener).show();
			}
		}

		private void startPreferences() {
			Intent i = new Intent(getBaseContext(), PreferencesActivity.class);
			startActivity(i);
		}
	};
	private OnClickListener recoverMasterPasswListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
			final String mailRecover = prefs.getString(Constants.preferences.preference_mail, "");

			if (!mailRecover.equals("")) {
				final Dialog dialog = new Dialog(ContactRetrieveActivity.this);
				dialog.setTitle(getApplicationContext().getString(R.string.label_recover_master_password));

				LinearLayout ll = new LinearLayout(ContactRetrieveActivity.this);
				ll.setOrientation(LinearLayout.VERTICAL);

				TextView tvMasterPassw = new TextView(ContactRetrieveActivity.this);
				StringBuilder textinfo = new StringBuilder();
				textinfo.append(getApplicationContext().getString(R.string.send_mail_infor_one));
				textinfo.append(": ");
				textinfo.append(mailRecover);
				textinfo.append(". ");
				textinfo.append(getApplicationContext().getString(R.string.send_mail_infor_two));
				tvMasterPassw.setText(textinfo.toString());

				ll.addView(tvMasterPassw);

				Button btSend = new Button(ContactRetrieveActivity.this);
				btSend.setId(6);
				btSend.setText(getApplication().getString(R.string.button_send));
				btSend.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
						sendMail(prefs, mailRecover);
					}

					private ProgressDialog createProgressDialog() {
						final ProgressDialog dialog = ProgressDialog.show(ContactRetrieveActivity.this, getApplicationContext().getString(R.string.app_name), getApplicationContext().getString(R.string.label_loading_message), false, false);
						dialog.setIcon(R.drawable.ic_launcher);
						return dialog;
					}

					private void sendMail(final SharedPreferences prefs, final String mailRecover) {
						final ProgressDialog sendProgress = createProgressDialog();

						String subject = new String(getApplicationContext().getString(R.string.app_name) + ": " + getApplicationContext().getString(R.string.subject_password_recover));
						String masterPassw = prefs.getString(Constants.preferences.preference_master_password, "");
						String text = new String(getApplicationContext().getString(R.string.text_password_recover) + ": " + masterPassw);

						final Mail m = new Mail();

						String[] toArr = { mailRecover };
						m.setTo(toArr);
						m.setFrom(Constants.mail.mail_send_fakefrom);
						m.setSubject(subject);
						m.setBody(text);

						final Handler handler = new HandlerExtension(sendProgress, ContactRetrieveActivity.this);

						Runnable runnableSendMail = new Runnable() {
							@Override
							public void run() {
								Message msg = new Message();
								Bundle data = new Bundle();
								try {
									// howAddAttachment(m);
									boolean send = m.send();
									data.putBoolean(Constants.mail.mail_send_status, send);
								} catch (Exception e) {
								} finally {
									msg.setData(data);
									handler.sendMessage(msg);
								}
							}

							// private void howAddAttachment(final Mail m)
							// throws Exception {
							// File m_Sdcard =
							// Environment.getExternalStorageDirectory();
							// File cacheDir = new
							// File(m_Sdcard.getAbsolutePath() +
							// "/CriarFolder");
							// if (!cacheDir.exists()) {
							// cacheDir.mkdirs();
							// }
							// m.addAttachment(m_Sdcard.getAbsolutePath() +
							// "/CriarFolder/arquivo.pdf");
							// }
						};
						Thread sendMailThread = new Thread(runnableSendMail);
						sendMailThread.start();
					}
				});
				ll.addView(btSend);

				Button btCancel = new Button(ContactRetrieveActivity.this);
				btCancel.setText(getApplicationContext().getString(R.string.label_cancel));
				btCancel.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				ll.addView(btCancel);

				dialog.setContentView(ll);
				dialog.show();
			}
		}
	};

	private OnClickListener createMasterPasswListener = new OnClickListener() {
		public void onClick(View v) {
			final Dialog dialog = new Dialog(ContactRetrieveActivity.this);
			dialog.setTitle(getApplicationContext().getString(R.string.label_create_master_password));

			LinearLayout ll = new LinearLayout(ContactRetrieveActivity.this);
			ll.setOrientation(LinearLayout.VERTICAL);

			TextView tvMasterPassw = new TextView(ContactRetrieveActivity.this);
			tvMasterPassw.setText(R.string.label_type_master_password);
			ll.addView(tvMasterPassw);

			final EditText masterPasw = new EditText(ContactRetrieveActivity.this);
			masterPasw.setId(ID_EDIT_TEXT_MASTER_PASSW);
			masterPasw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			ll.addView(masterPasw);

			TextView tvMail = new TextView(ContactRetrieveActivity.this);
			tvMail.setText(R.string.label_mail_recover_master_password);
			ll.addView(tvMail);

			final EditText mail = new EditText(ContactRetrieveActivity.this);
			mail.setId(ID_EDIT_TEXT_MAIL);
			mail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			ll.addView(mail);

			RelativeLayout rl = new RelativeLayout(ContactRetrieveActivity.this);

			RelativeLayout.LayoutParams lpSave = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpSave.leftMargin = 75;

			Button saveMasterPassw = new Button(ContactRetrieveActivity.this);
			saveMasterPassw.setId(10);
			saveMasterPassw.setText(getApplication().getString(R.string.label_save));
			saveMasterPassw.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (isValidContent()) {
						SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString(Constants.preferences.preference_master_password, masterPasw.getText().toString());
						editor.putString(Constants.preferences.preference_mail, mail.getText().toString());

						editor.commit();

						showHideComponents();

						dialog.dismiss();
					} else {
						Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.label_invalid_value), Toast.LENGTH_SHORT).show();
					}
				}

				private boolean isValidContent() {
					if (masterPasw != null && masterPasw.getText() != null && !masterPasw.getText().toString().trim().equals("")) {
						if (mail != null && mail.getText() != null && !mail.getText().toString().trim().equals("")) {
							return true;
						}
					}
					return false;
				}
			});
			rl.addView(saveMasterPassw, lpSave);

			Button cancel = new Button(ContactRetrieveActivity.this);
			cancel.setId(20);
			cancel.setText(getApplication().getString(R.string.label_cancel));
			cancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			RelativeLayout.LayoutParams lpCancel = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpCancel.addRule(RelativeLayout.RIGHT_OF, saveMasterPassw.getId());
			rl.addView(cancel, lpCancel);

			ll.addView(rl);

			dialog.setContentView(ll);
			dialog.show();
		}
	};

	private void createComponentsView() {
		settings = (Button) this.findViewById(R.id.settings);
		createMasterPassw = (Button) this.findViewById(R.id.addMasterPassw);
		recoverMasterPassw = (Button) this.findViewById(R.id.recoverMasterPassw);
		help = (Button) this.findViewById(R.id.help);
		rate = (Button) this.findViewById(R.id.rate);
	}

	private void createComponentsListeners() {
		settings.setOnClickListener(settingsListener);
		createMasterPassw.setOnClickListener(createMasterPasswListener);
		recoverMasterPassw.setOnClickListener(recoverMasterPasswListener);
		help.setOnClickListener(helpListener);
		rate.setOnClickListener(rateListener);
	}

	private boolean hasMasterPasswPreferences() {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.preferences.preferencefilename, 0);
		String masterPassw = prefs.getString(Constants.preferences.preference_master_password, "");
		if (!masterPassw.equals("")) {
			return true;
		}
		return false;
	}

	private ContactRetrieveDS getContactRetrieveDSPassword() {
		if (contactRetrieveDS == null) {
			contactRetrieveDS = new PasswordDAO(getBaseContext());
		}
		return contactRetrieveDS instanceof PasswordDAO ? contactRetrieveDS : (contactRetrieveDS = new PasswordDAO(getBaseContext()));
	}
}