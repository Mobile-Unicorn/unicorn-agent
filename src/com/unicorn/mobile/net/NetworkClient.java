package com.unicorn.mobile.net;

import java.io.IOException;

public abstract interface NetworkClient {
	public abstract ResponseModel getInputStream(String paramString)
			throws IOException;

	public abstract ResponseModel sendPostRequest(String urlStr, byte[] content)
			throws IOException;

	public abstract void close();
}