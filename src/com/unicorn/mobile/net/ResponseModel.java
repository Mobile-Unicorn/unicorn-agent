package com.unicorn.mobile.net;

import java.io.InputStream;

/**
 * Holds data about the response message returning from HTTP request.
 * 
 * @author Connor
 * 
 */
public class ResponseModel {
	public ResponseModel() {
		
	}
	
	/**
	 * The HTTP method
	 */
	public String method;

	/**
	 * The HTTP status code
	 */
	public int status;

	public InputStream in;
}
