package com.unicorn.mobile.dao;

import android.provider.BaseColumns;

public class EventModel {
	private int eventId;
	private String eventData;
	
	public EventModel() {
	}
	
	public EventModel(int id, String data) {
		eventId = id;
		eventData = data;
	}

	public int getId() {
		return eventId;
	}

	public void setId(int eid) {
		eventId = eid;
	}

	public String getData() {
		return eventData;
	}

	public void setData(String data) {
		eventData = data;
	}
	
	/* Inner class that defines the table contents */
	protected static class EventEntry implements BaseColumns {
		public static final String TABLE_NAME = "event";
		public static final String COLUMN_NAME_EVENT_ID = "eid";
		public static final String COLUMN_NAME_DATA = "data";
	}
	
}
