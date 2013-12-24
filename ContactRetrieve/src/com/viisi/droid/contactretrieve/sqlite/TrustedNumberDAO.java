package com.viisi.droid.contactretrieve.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.viisi.droid.contactretrieve.entity.TrustedNumber;

public class TrustedNumberDAO extends ContactRetrieveDS {

	private String[] allColumns = { SQLiteHelper.TRUSTEDNUMBERS_COLUMN_ID, SQLiteHelper.TRUSTEDNUMBERS_COLUMN_NUMBER };

	public TrustedNumberDAO(Context context) {
		super(context);
	}

	@Override
	public void insert(String number) {
		open();

		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.TRUSTEDNUMBERS_COLUMN_NUMBER, number);

		database.insert(SQLiteHelper.TABLE_NAME_TRUSTEDNUMBERS, null, values);

		close();
	}

	@Override
	public void delete(Object object) {
		TrustedNumber tNumber = (TrustedNumber) object;
		if (tNumber != null && tNumber.getId() != null) {
			open();
			database.delete(SQLiteHelper.TABLE_NAME_TRUSTEDNUMBERS, SQLiteHelper.TRUSTEDNUMBERS_COLUMN_ID + " = " + tNumber.getId(), null);
			close();
		}
	}

	@Override
	public void deleteAll() {
		open();
		database.delete(SQLiteHelper.TABLE_NAME_TRUSTEDNUMBERS, null, null);
		close();
	}

	public List<TrustedNumber> findAll() {
		open();
		List<TrustedNumber> tNumbers = new ArrayList<TrustedNumber>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_NAME_TRUSTEDNUMBERS, allColumns, null, null, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			TrustedNumber passw = cursorToTrustedNumber(cursor);
			tNumbers.add(passw);
			cursor.moveToNext();
		}
		cursor.close();
		close();

		return tNumbers;
	}

	public int count() {
		open();

		Cursor cursor = database.rawQuery("select count(*) from " + SQLiteHelper.TABLE_NAME_TRUSTEDNUMBERS, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);

		cursor.close();
		close();

		return count;
	}

	public TrustedNumber findById(Long id) {
		TrustedNumber passw = new TrustedNumber();
		if (id != null && id.compareTo(0l) != 0) {
			open();

			Cursor cursor = database.query(SQLiteHelper.TABLE_NAME_TRUSTEDNUMBERS, allColumns, SQLiteHelper.TRUSTEDNUMBERS_COLUMN_ID + " = " + id, null, null, null, null);
			cursor.moveToFirst();
			passw = cursorToTrustedNumber(cursor);
			cursor.close();
			close();
		}
		return passw;
	}

	public List<TrustedNumber> findByDesc(String number) {
		List<TrustedNumber> passws = new ArrayList<TrustedNumber>();
		if (number != null && !number.trim().equals("")) {
			open();

			Cursor cursor = database.query(SQLiteHelper.TABLE_NAME_TRUSTEDNUMBERS, allColumns, SQLiteHelper.TRUSTEDNUMBERS_COLUMN_NUMBER + " like " + number, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				TrustedNumber passw = cursorToTrustedNumber(cursor);
				passws.add(passw);
				cursor.moveToNext();
			}
			cursor.close();
			close();
		}
		return passws;
	}

	private TrustedNumber cursorToTrustedNumber(Cursor cursor) {
		TrustedNumber tNumber = new TrustedNumber();
		tNumber.setId(cursor.getLong(0));
		tNumber.setNumber(cursor.getString(1));

		return tNumber;
	}

}
