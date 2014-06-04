package com.unicorn.mobile.net;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

class HttpNetworkClient implements NetworkClient {
	private HttpClient mClient;

	public ResponseModel getInputStream(String url) throws IOException {
		mClient = createNewHttpClient();
		HttpResponse response = this.mClient.execute(new HttpGet(url));

		return handleServerResponse(this.mClient, response);
	}

	public ResponseModel sendPostRequest(String url, byte[] content)
			throws IOException {
		mClient = createNewHttpClient();
		HttpPost httpPost = createPostRequest(url, content);
		HttpResponse response = mClient.execute(httpPost);

		return handleServerResponse(mClient, response);
	}

	public void close() {
		closeWithClient(mClient);
	}

	private HttpPost createPostRequest(String url, byte[] content) {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new ByteArrayEntity(content));

		return httpPost;
	}

	private void closeWithClient(HttpClient client) {
		if ((client != null) && (client.getConnectionManager() != null))
			client.getConnectionManager().shutdown();
	}

	private ResponseModel handleServerResponse(HttpClient client,
			HttpResponse response) throws IOException {
		ResponseModel model = new ResponseModel();
		model.status = response.getStatusLine().getStatusCode();
		model.in = response.getEntity().getContent();
		closeWithClient(client);

		return model;
	}

	private HttpClient createNewHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 20000);
		HttpConnectionParams.setSoTimeout(params, 20000);

		return new DefaultHttpClient(params);
	}

}