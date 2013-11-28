package com.viisi.droid.contactretrieve.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.viisi.droid.contactretrieve.entity.Password;

public class PasswordDAO extends ContactRetrieveDS {

	private String[] allColumns = { SQLiteHelper.PASSWORD_COLUMN_ID, SQLiteHelper.PASSWORD_COLUMN_PASSWORD };

	public PasswordDAO(Context context) {
		super(context);
	}

	@Override
	public void insert(String passw) {
		open();

		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.PASSWORD_COLUMN_PASSWORD, passw);

		database.insert(SQLiteHelper.TABLE_NAME_PASSWORD, null, values);

		close();
	}

	@Override
	public void delete(Object object) {
		Password password = (Password) object;
		if (password != null && password.getId() != null) {
			open();
			database.delete(SQLiteHelper.TABLE_NAME_PASSWORD, SQLiteHelper.PASSWORD_COLUMN_ID + " = " + password.getId(), null);
			close();
		}
	}

	@Override
	public void deleteAll() {
		open();
		database.delete(SQLiteHelper.TABLE_NAME_PASSWORD, null, null);
		close();
	}

	public List<Password> findAll() {
		open();
		List<Password> passwords = new ArrayList<Password>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_NAME_PASSWORD, allColumns, null, null, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			Password passw = cursorToPassword(cursor);
			passwords.add(passw);
			cursor.moveToNext();
		}
		cursor.close();
		close();

		return passwords;
	}

	public int count() {
		open();

		Cursor cursor = database.rawQuery("select count(*) from " + SQLiteHelper.TABLE_NAME_PASSWORD, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);

		cursor.close();
		close();

		return count;
	}

	public Password findById(Long id) {
		Password passw = new Password();
		if (id != null && id.compareTo(0l) != 0) {
			open();

			Cursor cursor = database.query(SQLiteHelper.TABLE_NAME_PASSWORD, allColumns, SQLiteHelper.PASSWORD_COLUMN_ID + " = " + id, null, null, null, null);
			cursor.moveToFirst();
			passw = cursorToPassword(cursor);
			cursor.close();
			close();
		}
		return passw;
	}

	public List<Password> findByDesc(String number) {
		List<Password> passws = new ArrayList<Password>();
		if (number != null && !number.trim().equals("")) {
			open();

			Cursor cursor = database.query(SQLiteHelper.TABLE_NAME_PASSWORD, allColumns, SQLiteHelper.PASSWORD_COLUMN_PASSWORD + " like %" + number + "%", null, null, null, null);
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				Password passw = cursorToPassword(cursor);
				passws.add(passw);
				cursor.moveToNext();
			}

			cursor.close();
			close();
		}
		return passws;
	}

	private Password cursorToPassword(Cursor cursor) {
		Password password = new Password();
		password.setId(cursor.getLong(0));
		password.setPassw(cursor.getString(1));

		return password;
	}

}
