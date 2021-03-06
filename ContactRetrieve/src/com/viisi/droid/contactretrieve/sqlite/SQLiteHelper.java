package com.viisi.droid.contactretrieve.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "contactretrieve.db";

	// Password
	public static final String TABLE_NAME_PASSWORD = "passwords";
	public static final String PASSWORD_COLUMN_ID = "_id";
	public static final String PASSWORD_COLUMN_PASSWORD = "password";

	// TrustedNumber
	public static final String TABLE_NAME_TRUSTEDNUMBERS = "trustednumbers";
	public static final String TRUSTEDNUMBERS_COLUMN_ID = "_id";
	public static final String TRUSTEDNUMBERS_COLUMN_NUMBER = "number";

	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String PASSWORD_TABLE_CREATE = "create table " + TABLE_NAME_PASSWORD + "( " + PASSWORD_COLUMN_ID + " integer primary key autoincrement, " + PASSWORD_COLUMN_PASSWORD + " text not null);";
	private static final String TRUSTEDNUMBERS_TABLE_CREATE = "create table " + TABLE_NAME_TRUSTEDNUMBERS + "( " + TRUSTEDNUMBERS_COLUMN_ID + " integer primary key autoincrement, " + TRUSTEDNUMBERS_COLUMN_NUMBER + " text not null);";

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PASSWORD_TABLE_CREATE);
		db.execSQL(TRUSTEDNUMBERS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PASSWORD);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TRUSTEDNUMBERS);
		onCreate(db);
	}

}
