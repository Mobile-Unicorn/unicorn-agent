package com.unicorn.mobile.dao;

import com.unicorn.mobile.dao.EventModel.EventEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UnicornDbHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "CyouAgent.db";

	private static final String INTEGER_TYPE = " INTEGER";
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
			+ EventEntry.TABLE_NAME + " (" + EventEntry._ID
			+ " INTEGER PRIMARY KEY," + EventEntry.COLUMN_NAME_EVENT_ID
			+ INTEGER_TYPE + COMMA_SEP + EventEntry.COLUMN_NAME_DATA
			+ TEXT_TYPE + " )";
	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ EventEntry.TABLE_NAME;

	public UnicornDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}
	
}
