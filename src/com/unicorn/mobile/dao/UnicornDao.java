package com.unicorn.mobile.dao;

import java.util.ArrayList;
import java.util.List;

import com.unicorn.mobile.dao.EventModel.EventEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UnicornDao {
	
	private static UnicornDbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	
	public UnicornDao(Context ctx) {
		mDbHelper = new UnicornDbHelper(ctx);
	}
	
	public long insert(EventModel model) {
		mDatabase = mDbHelper.getWritableDatabase();
		ContentValues initVals = new ContentValues();
		initVals.put(EventEntry.COLUMN_NAME_EVENT_ID, model.getId());
		initVals.put(EventEntry.COLUMN_NAME_DATA, model.getData());
		return mDatabase.insert(EventEntry.TABLE_NAME, null, initVals);
	}
	
	public int update(EventModel model) {
		mDatabase = mDbHelper.getReadableDatabase();
		ContentValues initVals = new ContentValues();
		initVals.put(EventEntry.COLUMN_NAME_DATA, model.getData());
		String selection = EventEntry.COLUMN_NAME_EVENT_ID + " LIKE ?";
		String[] selectionArgs = { String.valueOf(model.getId()) };
		return mDatabase.update(EventEntry.TABLE_NAME, initVals, selection, selectionArgs);
	}
	
	public void delete(Integer... eids) {
		int len = eids.length;
		String[] selectionArgs = new String[len];
		StringBuffer builder = new StringBuffer();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				builder.append('?').append(',');
				selectionArgs[i] = eids[i].toString();
			}
			builder.deleteCharAt(builder.length() - 1);
		}
		
		mDatabase = mDbHelper.getWritableDatabase();
		String selection = EventEntry.COLUMN_NAME_EVENT_ID + " IN (" + builder.toString() + ")";
		mDatabase.delete(EventEntry.TABLE_NAME, selection, selectionArgs);
		mDatabase.close();
	}
	
	public EventModel queryEntry(Integer eid) {
		mDatabase = mDbHelper.getReadableDatabase();
		String sql = "SELECT * FROM " + EventEntry.TABLE_NAME + " WHERE " + EventEntry.COLUMN_NAME_EVENT_ID + "=?";
		Cursor cursor = mDatabase.rawQuery(sql,	new String[] { String.valueOf(eid) });
		EventModel event = null;
		if (cursor.moveToNext()) {
			event = new EventModel(cursor.getInt(0), cursor.getString(1));
		}
		return event;
	}
	
	public List<EventModel> queryAll() {
		List<EventModel> events = new ArrayList<EventModel>();
		mDatabase = mDbHelper.getReadableDatabase();
		String sql = "SELECT * FROM " + EventEntry.TABLE_NAME;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			EventModel event = new EventModel(cursor.getInt(0), cursor.getString(1));
			events.add(event);
		}
		return events;
	}

}
