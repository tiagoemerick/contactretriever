package com.viisi.droid.contactretrieve.activity;

import org.holoeverywhere.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.viisi.droid.contactretrieve.R;
import com.viisi.droid.contactretrieve.util.Constants;

public class HelpActivity extends Activity {

	// Panels
	private TextView helpHowto;
	private TextView helpPassw;
	private TextView helpMasterPassw;
	private TextView helpSymbolsPassw;
	private TextView helpCosts;
	private TextView helpAbout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int layout = R.layout.help_layout;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			layout = extras.getInt(Constants.layout.string_layout_preferences);
		}

		setContentView(layout);
		createComponents(layout);
	}
	
	@Override
	protected void onPause() {
		overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
		super.onPause();
	}

	private OnClickListener goViewHowto = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), HelpActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.howto_help_layout);
			startActivity(i);
		}
	};

	private OnClickListener goViewPassw = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), HelpActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.passw_help_layout);
			startActivity(i);
		}
	};

	private OnClickListener goViewMasterPassw = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), HelpActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.master_passw_help_layout);
			startActivity(i);
		}
	};

	private OnClickListener goViewSymbols = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), HelpActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.symbols_passw_help_layout);
			startActivity(i);
		}
	};

	private OnClickListener goViewCosts = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), HelpActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.costs_passw_help_layout);
			startActivity(i);
		}
	};
	
	private OnClickListener goViewAbout = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), HelpActivity.class);
			i.putExtra(Constants.layout.string_layout_preferences, R.layout.about_passw_help_layout);
			startActivity(i);
		}
	};

	private void createComponents(int layout) {
		if (layout == R.layout.help_layout) {
			createComponentsViewHelp();
			createComponentsListenersHelp();
		}
	}

	private void createComponentsListenersHelp() {
		helpHowto.setOnClickListener(goViewHowto);
		helpPassw.setOnClickListener(goViewPassw);
		helpMasterPassw.setOnClickListener(goViewMasterPassw);
		helpSymbolsPassw.setOnClickListener(goViewSymbols);
		helpCosts.setOnClickListener(goViewCosts);
		helpAbout.setOnClickListener(goViewAbout);
	}

	private void createComponentsViewHelp() {
		helpHowto = (TextView) findViewById(R.id.helpHowto);
		helpPassw = (TextView) findViewById(R.id.helpPassw);
		helpMasterPassw = (TextView) findViewById(R.id.helpMasterPassw);
		helpSymbolsPassw = (TextView) findViewById(R.id.helpSymbolsPassw);
		helpCosts = (TextView) findViewById(R.id.helpCosts);
		helpAbout = (TextView) findViewById(R.id.helpAbout);
	}
}