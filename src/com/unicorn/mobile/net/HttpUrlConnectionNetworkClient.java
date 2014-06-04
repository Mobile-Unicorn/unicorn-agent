package com.unicorn.mobile.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpUrlConnectionNetworkClient implements NetworkClient {
	private HttpURLConnection mConnection;
	private ResponseModel mResponseModel = new ResponseModel();

	public ResponseModel getInputStream(String urlStr) throws IOException {
		mConnection = getUrlConnection(urlStr);

		return handleServerResponse(mConnection);
	}

	public ResponseModel sendPostRequest(String urlStr, byte[] content)
			throws IOException {
		OutputStream sendToServer = null;
		mConnection = getUrlConnection(urlStr);
		mConnection.setRequestProperty("Content-Length",
				Integer.toString(content.length));
		mConnection.setRequestProperty("Connection", "Keep-Alive");
		mConnection.setRequestMethod("POST");
		mConnection.setDoOutput(true);
		mConnection.connect();

		sendToServer = mConnection.getOutputStream();
		sendToServer.write(content);
		sendToServer.flush();

		return handleServerResponse(mConnection);
	}

	public void close() {
		if (mConnection != null)
			mConnection.disconnect();
	}

	private ResponseModel handleServerResponse(HttpURLConnection mConnection)
			throws IOException {
		mResponseModel.status = mConnection.getResponseCode();
		if (mResponseModel.status / 100 == 2) {
			mResponseModel.in = mConnection.getInputStream();
		} else {
			mResponseModel.in = mConnection.getErrorStream();
		}
		
		return mResponseModel;
	}

	private HttpURLConnection getUrlConnection(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setReadTimeout(20000);
		connection.setConnectTimeout(20000);

		return connection;
	}

}