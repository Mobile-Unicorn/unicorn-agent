package com.unicorn.mobile.common;

import java.util.Map;

public interface ICollector {
	public abstract Map<String, String> parse();
	
	public abstract Map<String, String> recover();
}
