/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.html #{util.YYYY()} Viper Software Services
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 *
 * -----------------------------------------------------------------------------
 */

package com.viper.database.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64InputStream;

public class Encryptor {

	// see Encryption test case for data generation.
	// see ant target 'key', in build.xml

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/CBC/NoPadding";

	private static final byte[] HiddenKey;

	static {
		HiddenKey = new byte[] { 0x56, 0x54, 0x56, 0x50, 0x65, 0x58, 0x70, 0x52, 0x53, 0x58, 0x70, 0x30, 0x61, 0x6D,
				0x31, 0x42, 0x64, 0x48, 0x49, 0x31, 0x59, 0x56, 0x49, 0x79, 0x57, 0x46, 0x70, 0x4F, 0x64, 0x7A, 0x30,
				0x39 };
	}

	private static final byte[] ivKey;

	static {
		ivKey = new byte[] { 0x4E, 0x54, 0x78, 0x2F, 0x43, 0x76, 0x72, 0x69, 0x7A, 0x4B, 0x4E, 0x4F, 0x32, 0x6A, 0x2B,
				0x4A, 0x4C, 0x66, 0x61, 0x4B, 0x38, 0x41, 0x3D, 0x3D };
	}

	public static String encode(byte[] values, int length) throws UnsupportedEncodingException {
		return Base64.getEncoder().encodeToString(Arrays.copyOf(values, length));
	}

	public static String encode(byte[] values) throws UnsupportedEncodingException {
		return Base64.getEncoder().encodeToString(values);
	}

	public static byte[] decode(String values) {
		return Base64.getDecoder().decode(values);
	}

	public static byte[] decode(byte[] values) {
		return Base64.getDecoder().decode(values);
	}

	public String decryptPassword(final String value) throws Exception {
		if (value == null || value.length() == 0) {
			return null;
		}
		if (value != null && value.startsWith("ENC:")) {
			return decrypt(value.substring("ENC:".length()), decode(HiddenKey));
		}
		return value;
	}

	// Single encryption
	public String encrypt(final String value) throws Exception {
		if (value == null || value.length() == 0) {
			return null;
		}

		return encrypt(value, decode(HiddenKey));
	}

	public String decrypt(final String value) throws Exception {
		if (value == null || value.length() == 0) {
			return null;
		}

		return decrypt(value, decode(HiddenKey));
	}

	public String encrypt(final String valueEnc, final byte[] keyString) throws Exception {

		try {
			final Key key = generateKey(keyString, ALGORITHM);
			final Cipher c = Cipher.getInstance(TRANSFORMATION);

			// Initialization vector:
			byte bytes[] = decode(ivKey);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes, 0, bytes.length);

			c.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);

			final byte[] rawValue = pad(valueEnc.getBytes("utf8"), 16);
			final byte[] encValue = new byte[rawValue.length * 3];

			int length = c.doFinal(rawValue, 0, rawValue.length, encValue, 0);
			if (length == encValue.length) {
				System.err.println("WARNING: encryption used up entire buffer.");
			}

			return encode(encValue, length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public String encrypt(final String valueEnc, String algorithm, final String keyString) throws Exception {

		try {
			String shortAlgorithm = algorithm;
			if (algorithm.indexOf("/") != -1) {
				shortAlgorithm = algorithm.substring(0, algorithm.indexOf("/"));
			}

			final Key key = generateKey(keyString.getBytes(), shortAlgorithm);
			final Cipher c = Cipher.getInstance(algorithm);

			c.init(Cipher.ENCRYPT_MODE, key);

			final byte[] rawValue = pad(valueEnc.getBytes("utf8"), 16);
			final byte[] encValue = new byte[rawValue.length * 3];

			int length = c.doFinal(rawValue, 0, rawValue.length, encValue, 0);
			if (length == encValue.length) {
				System.err.println("WARNING: encryption used up entire buffer.");
			}

			return encode(encValue, length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public String decrypt(final String encryptedValue, final byte[] keyString) {

		int len = 0;

		try {

			final Key key = generateKey(keyString, ALGORITHM);
			final Cipher c = Cipher.getInstance(TRANSFORMATION);

			// Initialization vector:
			byte bytes[] = decode(ivKey);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes, 0, bytes.length);

			c.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

			byte[] decorVal = pad(decode(encryptedValue), 16);

			len = decorVal.length;
			final byte[] decValue = new byte[decorVal.length * 4];

			int length = c.doFinal(decorVal, 0, decorVal.length, decValue, 0);
			if (length == decValue.length) {
				System.err.println("WARNING: decryption used up entire buffer: " + length);
			}

			int newLength = length;
			for (int index = length - 1; index >= 0; index--) {
				if (decValue[index] != 0) {
					newLength = index + 1;
					break;
				}
			}

			return new String(decValue, 0, newLength, "utf8");
		} catch (Exception ex) {
			System.err.println("ERROR: base64Decode length: " + len);
			ex.printStackTrace();
		}
		return null;
	}

	public void decrypt(final String infile, String algorithm, final String keyString, final String outfile) {

		try {
			String shortAlgorithm = algorithm;
			if (algorithm.indexOf("/") != -1) {
				shortAlgorithm = algorithm.substring(0, algorithm.indexOf("/"));
			}

			final Key key = generateKey(keyString.getBytes(), shortAlgorithm);
			final Cipher c = Cipher.getInstance(algorithm);

			c.init(Cipher.DECRYPT_MODE, key);

			FileInputStream fis = new FileInputStream(infile);
			Base64InputStream bis = new Base64InputStream(fis, false);
			CipherInputStream in = new CipherInputStream(bis, c);
			FileOutputStream out = new FileOutputStream(outfile);

			final int len = 8;
			byte[] b = new byte[len];
			while (true) {
				int i = in.read(b);
				if (i == -1) {
					break;
				} else if (i != 8) {
					for (int j = i; j < len; j++) {
						b[j] = 0;
					}
				}
				out.write(b, 0, len);
			}
			in.close();
			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private Key generateKey(final byte[] secKey, String algorithm) throws Exception {
		final byte[] keyVal = secKey;
		int length = (keyVal.length > 16) ? 16 : keyVal.length;

		return new SecretKeySpec(keyVal, 0, length, algorithm);
	}

	private byte[] pad(byte[] original, int padding) {
		try {
			if ((original.length % padding) == 0) {
				return original;
			}
			int newLength = original.length + padding - (original.length % padding);
			return Arrays.copyOf(original, newLength);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}