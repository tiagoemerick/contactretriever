package com.viisi.droid.contactretrieve.activity.preferences;

import android.preference.PreferenceActivity;

public class UserPreferencesActivity extends PreferenceActivity {

	/*
	private static final String SHARED_PREFERENCE_NAME = "CRPreferences";
	public static final String KEY_PASSWORD = "passwordPref";

	private SharedPreferences userPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.fadein, R.anim.fadeout);

		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(SHARED_PREFERENCE_NAME);
		addPreferencesFromResource(R.xml.myapppreferences);

		createListeners();
	}

	private void createListeners() {
		getUserSharedPreferences().registerOnSharedPreferenceChangeListener(settingsChangedListener);
	}

	private OnSharedPreferenceChangeListener settingsChangedListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (isPasswordFieldChanged(key)) {
				insertPasswordDB(sharedPreferences, key);
				clearPasswordInputPreference(key);
			}
		}

		private void clearPasswordInputPreference(String key) {
			String value = getUserSharedPreferences().getString(key, "");
			if (value != null && !value.equalsIgnoreCase("")) {
				SharedPreferences.Editor prefsEditor = getUserSharedPreferences().edit();
				prefsEditor.remove(key);
				prefsEditor.commit();
			}
		}

		private void insertPasswordDB(SharedPreferences sharedPreferences, String key) {
			String valueToInsert = sharedPreferences.getString(key, "");
			if (valueToInsert != null && !valueToInsert.equalsIgnoreCase("")) {
//				Intent i = new Intent(getBaseContext(), ContactRetrieveService.class);
				Intent i = new Intent(getBaseContext(), PreferencesActivity.class);
				i.putExtra("keyReferenceTableToInsert", KEY_PASSWORD);
				i.putExtra("valueToInsert", valueToInsert);

				PendingIntent sentPI = PendingIntent.getService(getBaseContext(), 0, i, 0);
				try {
					sentPI.send();
				} catch (CanceledException e) {
				}
			}
		}

		private boolean isPasswordFieldChanged(String key) {
			if (key.equalsIgnoreCase(KEY_PASSWORD)) {
				return true;
			}
			return false;
		}

	};

	private SharedPreferences getUserSharedPreferences() {
		if (userPrefs == null) {
			userPrefs = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
		}
		return userPrefs;
	}
	*/

}