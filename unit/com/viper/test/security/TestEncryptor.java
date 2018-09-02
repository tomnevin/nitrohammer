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

package com.viper.test.security;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.viper.database.security.Encryptor;

import junit.framework.TestCase;

public class TestEncryptor extends TestCase {

	private static final String EncryptDecryptSeed = "Ma1n5iaP!alpberw3$%6opwjkljl";

	public void testGeneratePassword() throws Exception {

		Encryptor encryptor = new Encryptor();

		String seed = EncryptDecryptSeed;
		String secureKey = generateSecureKey(seed);
		System.out.println(secureKey);

		String fileKeyString = encryptor.encrypt(secureKey);
		System.out.println(fileKeyString);
	}

	public void testSingleEncryption() throws Exception {

		String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		Encryptor encryptor = new Encryptor();

		String value = encryptor.encrypt(expected);
		String actual = encryptor.decrypt(value);

		assertEquals("testSingleEncryption: ", actual, expected);
	}

	public void testDoubleEncryption() throws Exception {

		Encryptor encryptor = new Encryptor();

		String seed = EncryptDecryptSeed;
		String secureKey = generateSecureKey(seed);
		String fileKeyString = encryptor.encrypt(secureKey);

		String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		String value = encryptor.encrypt(expected, fileKeyString.getBytes("utf-8"));
		String actual = encryptor.decrypt(value, fileKeyString.getBytes("utf-8"));

		assertEquals("testDoubleEncryption: ", actual, expected);
	}

	public void testDoubleEncryption1() throws Exception {

		Encryptor encryptor = new Encryptor();

		String seed = EncryptDecryptSeed;
		String secureKey = generateSecureKey(seed);
		String fileKeyString = encryptor.encrypt(secureKey);

		String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789a";

		String value = encryptor.encrypt(expected, fileKeyString.getBytes("utf-8"));
		String actual = encryptor.decrypt(value, fileKeyString.getBytes("utf-8"));

		assertEquals("testDoubleEncryption1: ", actual, expected);
	}

	public void testDoubleEncryption2() throws Exception {

		Encryptor encryptor = new Encryptor();

		String seed = EncryptDecryptSeed;
		String secureKey = generateSecureKey(seed);
		String fileKeyString = encryptor.encrypt(secureKey);

		String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcde";

		String value = encryptor.encrypt(expected, fileKeyString.getBytes("utf-8"));
		String actual = encryptor.decrypt(value, fileKeyString.getBytes("utf-8"));

		assertEquals("testDoubleEncryption2: ", actual, expected);
	}

	public void testEncryptionByKeyFile() throws Exception {

		Encryptor encryptor = new Encryptor();

		String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcde";

		String value = encryptor.encrypt(expected);
		String actual = encryptor.decrypt(value);

		assertEquals("testEncryptionByKeyFile: ", actual, expected);
	}

	public void testBase64() throws Exception {

		Encryptor encryptor = new Encryptor();

		String expected = readFile("./etc/conf/config.properties");
		String value = encryptor.encode(expected.getBytes("utf-8"));
		String actual = new String(encryptor.decode(value));

		assertEquals("testBase64: ", expected.length(), actual.length());
		assertEquals("testBase64: ", expected, actual);
	}

	public void testConfigProperties() throws Exception {

		String expected = readFile("./etc/conf/config.properties");
		writeFile("./etc/conf/config.properties", expected);
		String actual = readFile("./etc/conf/config.properties");

		assertEquals("testConfigProperties: ", expected.length(), actual.length());
		assertEquals("testConfigProperties: ", expected, actual);
	}

	public void testGenerateEncryptedFiles() throws Exception {

		encryptFile("./etc/conf/config.properties");
		File file = new File("./etc/test/rules/Dec18");
		if (file != null) {
			for (String filename : file.list()) {
				if (!filename.endsWith(".enc")) {
					encryptFile(file.getAbsolutePath() + "/" + filename);
				}
			}
		}
	}

	private void encryptFile(String filename) throws Exception {

		Encryptor encryptor = new Encryptor();
		writeFile(filename, encryptor.encrypt(readFile(filename)));
	}

	private String generateSecureKey(String password) throws Exception {

		Encryptor encryptor = new Encryptor();

		int iterations = 1024;
		char[] chars = password.toCharArray();
		byte[] salt = getSalt().getBytes();

		PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		return new String(encryptor.encode(skf.generateSecret(spec).getEncoded()));
	}

	private String getSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt.toString();
	}

	public static String readFile(String filename) throws Exception {
		byte[] bytes = Files.readAllBytes(new File(filename).toPath());
		return new String(bytes, "utf-8");
	}

	public static void writeFile(String filename, String value) throws Exception {
		Files.write(new File(filename).toPath(), value.getBytes("utf-8"), StandardOpenOption.CREATE,
		        StandardOpenOption.TRUNCATE_EXISTING);
	}
}