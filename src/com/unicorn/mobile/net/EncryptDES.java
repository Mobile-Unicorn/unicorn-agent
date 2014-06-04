package com.unicorn.mobile.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.content.Context;
import android.util.Base64;

public class EncryptDES {
	private Context mContext;
	private Cipher mCipher;
	private Key mKey;
	private final String keyStore = "des.dat";

	public EncryptDES(Context ctx) throws NoSuchAlgorithmException,
			NoSuchPaddingException {
		mContext = ctx;
		mCipher = Cipher.getInstance("DES");
		mKey = getKey();
	}

	private Key getKey() {
		Key key = null;
		InputStream in = null;
		ObjectInputStream oin = null;
		try {
			in = mContext.getAssets().open(keyStore);
			oin = new ObjectInputStream(in);
			key = (Key) oin.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				oin.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return key;
	}

	/**
	 * 
	 * @param bytes
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] Encrytor(String str) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		mCipher.init(Cipher.ENCRYPT_MODE, mKey);
		byte[] cipherByte = mCipher.doFinal(str.getBytes());
		byte[] bytes = Base64.encode(cipherByte, Base64.DEFAULT);
		return bytes;
	}

	/**
	 * 
	 * @param bytes
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] Decryptor(byte[] bytes) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		mCipher.init(Cipher.DECRYPT_MODE, mKey);
		byte[] cipherByte = mCipher.doFinal(bytes);
		byte[] ret = Base64.decode(cipherByte, Base64.DEFAULT);
		return ret;
	}

}
