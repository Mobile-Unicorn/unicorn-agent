package com.unicorn.mobile.net;

import android.os.Build;

public class NetworkClientFactory {
	public static NetworkClient createNetworkClient() {
		if (Build.VERSION.SDK_INT < 8) {
			return new HttpNetworkClient();
		}

		return new HttpUrlConnectionNetworkClient();
	}

}