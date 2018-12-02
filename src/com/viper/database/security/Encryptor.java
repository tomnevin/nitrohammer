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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Hex;

/**
 * Every implementation of the Java platform is required to support the
 * following standard Cipher transformations with the keysizes in parentheses:
 * <ul>
 * <li>AES/CBC/NoPadding (128)</li>
 * <li>AES/CBC/PKCS5Padding (128)</li>
 * <li>AES/ECB/NoPadding (128)</li>
 * <li>AES/ECB/PKCS5Padding (128)</li>
 * <li>DES/CBC/NoPadding (56)</li>
 * <li>DES/CBC/PKCS5Padding (56)</li>
 * <li>DES/ECB/NoPadding (56)</li>
 * <li>DES/ECB/PKCS5Padding (56)</li>
 * <li>DESede/CBC/NoPadding (168)</li>
 * <li>DESede/CBC/PKCS5Padding (168)</li>
 * <li>DESede/ECB/NoPadding (168)</li>
 * <li>DESede/ECB/PKCS5Padding (168)</li>
 * <li>RSA/ECB/PKCS1Padding (1024, 2048)</li>
 * <li>RSA/ECB/OAEPWithSHA-1AndMGF1Padding (1024, 2048)</li>
 * <li>RSA/ECB/OAEPWithSHA-256AndMGF1Padding (1024, 2048)</li>
 * </ul>
 *
 */

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

    /**
     * 
     * @param values
     * @param length
     * @return
     * @throws UnsupportedEncodingException
     */

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

    /**
     * 
     * @param password
     * @param algorithm
     *            (SHA-256, SHA-512)
     * @return
     * @throws Exception
     */
    public static String hash64(String password, String algorithm) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(algorithm);
        byte[] bytes = sha.digest(password.getBytes());
        return encode(bytes);
    }

    /**
     * 
     * @param password
     * @param algorithm
     *            (SHA-256, SHA-512)
     * @return
     * @throws Exception
     */
    public static String hash16(String password, String algorithm) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(algorithm);
        byte[] bytes = sha.digest(password.getBytes());
        return Hex.encodeHexString(bytes);
    }

    /**
     * Custom encryption for strings, keys, algorithms, are all hidden. Very Simple
     * to use.
     * 
     * @param value
     *            the string which has been encrypted
     * @return the decrypt string based on custom internal settings. `
     * @throws Exception
     */

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

    // Mirror of the mysql database encrypt AES_ENCRYPT routine.
    public String encrypt_aes(String value, String strKey) throws Exception {
        byte[] keyBytes = makeAESKey(strKey);

        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cleartext = value.getBytes("UTF-8");
        byte[] ciphertextBytes = cipher.doFinal(cleartext);

        return new String(Hex.encodeHex(ciphertextBytes));
    }

    public String decrypt_aes(String valueHex, String strKey) throws Exception {
        byte[] keyBytes = makeAESKey(strKey);

        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        Cipher decipher = Cipher.getInstance("AES");

        decipher.init(Cipher.DECRYPT_MODE, key);

        char[] cleartext = valueHex.toCharArray();

        byte[] decodeHex = Hex.decodeHex(cleartext);

        byte[] ciphertextBytes = decipher.doFinal(decodeHex);

        return new String(ciphertextBytes);
    }

    public byte[] makeAESKey(String strKey) throws Exception {
        final byte[] key = new byte[16];
        int i = 0;
        for (byte b : strKey.getBytes("ASCII")) {
            key[i++ % 16] ^= b;
        }
        return key;
    }

    /**
     * Given the value which is hex encoded, the transformation, and the encryption
     * key in string form. Encrypt the value and return,
     * 
     * @param valueEnc
     *            the value to be encrypted, in HEX encoded form
     * @param transformation
     *            the transformation ie (AES/NOPADDING)
     * @param keyString
     *            the key to be used for the encryption.
     * @return the encrypted value string encoded in hex format.
     * 
     * @throws Exception
     * 
     * @note the algorithm to be used for the encryption is taken from the
     *       transformation, all characters upto the '/'.
     * 
     */
    public String encrypt(final String valueEnc, final byte[] keyString) throws Exception {

        return encrypt(valueEnc, ALGORITHM, TRANSFORMATION, keyString, ivKey);
    }

    /**
     * Given the value which is hex encoded, the transformation, and the encryption
     * key in string form. Encrypt the value and return,
     * 
     * @param valueEnc
     *            the value to be encrypted, in HEX encoded form
     * @param transformation
     *            the transformation ie (AES/NOPADDING)
     * @param keyString
     *            the key to be used for the encryption.
     * @return the encrypted value string encoded in hex format.
     * 
     * @throws Exception
     * 
     * @note the algorithm to be used for the encryption is taken from the
     *       transformation, all characters upto the '/'.
     * 
     */
    public String encrypt(final String valueEnc, String algorithm, String transformation, final String keyString)
            throws Exception {

        return encrypt(valueEnc, algorithm, transformation, keyString.getBytes("ASCII"), null);
    }

    /**
     * Given the value which is hex encoded, the encryption algorithm, the
     * transformation, and the encryption key in string form. Encrypt the value and
     * return, The keyString should have been encrypted with the same algorithm with
     * the keyKey value.
     * 
     * @param valueEnc
     *            the value to be encrypted, in HEX encoded form
     * @param transformation
     *            the transformation ie (AES/NOPADIDNG)
     * @param keyString
     *            the key to be used for the encryption.
     * @return the encrypted value string encoded in hex format.
     * 
     * @throws Exception
     * 
     * @note the algorithm to be used for the encryption is taken from the
     *       transformation, all characters up to the '/'.
     * 
     * @note the
     * 
     */
    public String encrypt(final String valueEnc, String algorithm, String transformation, final byte[] keyString,
            final byte[] keyKey) throws Exception {

        try {
            final Key key = generateKey(keyString, algorithm);
            final Cipher c = Cipher.getInstance(transformation);

            // Is key encrypted?
            if (keyKey != null) {
                byte bytes[] = decode(ivKey);
                IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes, 0, bytes.length);

                c.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);

            } else {
                c.init(Cipher.ENCRYPT_MODE, key);

            }

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

    /**
     * 
     * @param encryptedValue
     * @param keyString
     * @return
     */

    public String decrypt(final String encryptedValue, final byte[] keyString) {
        return decrypt(encryptedValue, ALGORITHM, TRANSFORMATION, keyString);
    }

    public String decrypt(final String encryptedValue, String algorithm, String transformation,
            final byte[] keyString) {

        return decrypt(encryptedValue, algorithm, transformation, keyString, ivKey);
    }

    public String decrypt(final String encryptedValue, String algorithm, String transformation, final byte[] keyString,
            final byte[] keyKey) {

        int len = 0;

        try {

            final Key key = generateKey(keyString, algorithm);
            final Cipher c = Cipher.getInstance(transformation);

            // Is key encrypted?
            if (keyKey != null) {
                byte bytes[] = decode(ivKey);
                IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes, 0, bytes.length);

                c.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

            } else {
                c.init(Cipher.DECRYPT_MODE, key);

            }

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

    public void decrypt(final String infile, String algorithm, String transformation, final String keyString,
            final String outfile) {

        try {

            final Key key = generateKey(keyString.getBytes(), algorithm);
            final Cipher c = Cipher.getInstance(transformation);

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