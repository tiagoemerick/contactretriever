package com.viisi.droid.contactretrieve.sqlite;

import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.viisi.droid.contactretrieve.entity.IEntity;

public abstract class ContactRetrieveDS {

	protected SQLiteDatabase database;
	private SQLiteHelper dbHelper;

	protected ContactRetrieveDS(Context context) {
		dbHelper = new SQLiteHelper(context);
	}

	protected void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	protected void close() {
		dbHelper.close();
	}

	public abstract void insert(String value);

	public abstract void delete(Object object);

	public abstract void deleteAll();

	public abstract List<? extends IEntity> findAll();

	public abstract Object findById(Long id);

	public abstract List<? extends IEntity> findByDesc(String desc);

	public abstract int count();
}
