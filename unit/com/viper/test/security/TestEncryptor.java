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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.viper.database.security.Encryptor;
import com.viper.database.utils.junit.AbstractTestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestEncryptor extends AbstractTestCase {

    private static final String EncryptDecryptSeed = "Ma1n5iaP!alpberw3$%6opwjkljl";
    private static final Encryptor encryptor = new Encryptor();

    @Test
    public void testGeneratePassword() throws Exception {

        Encryptor encryptor = new Encryptor();

        String seed = EncryptDecryptSeed;
        String secureKey = generateSecureKey(seed);
        System.out.println(secureKey);

        String fileKeyString = encryptor.encrypt(secureKey);
        System.out.println(fileKeyString);
    }

    @Test
    public void testSingleEncryption() throws Exception {

        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        Encryptor encryptor = new Encryptor();

        String value = encryptor.encrypt(expected);
        String actual = encryptor.decrypt(value);

        assertEquals(getCallerMethodName(), actual, expected);
    }

    @Test
    public void testDoubleEncryption() throws Exception {

        Encryptor encryptor = new Encryptor();

        String seed = EncryptDecryptSeed;
        String secureKey = generateSecureKey(seed);
        String fileKeyString = encryptor.encrypt(secureKey);

        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        String value = encryptor.encrypt(expected, fileKeyString.getBytes("utf-8"));
        String actual = encryptor.decrypt(value, fileKeyString.getBytes("utf-8"));

        assertEquals(getCallerMethodName(), actual, expected);
    }

    @Test
    public void testDoubleEncryption1() throws Exception {

        Encryptor encryptor = new Encryptor();

        String seed = EncryptDecryptSeed;
        String secureKey = generateSecureKey(seed);
        String fileKeyString = encryptor.encrypt(secureKey);

        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789a";

        String value = encryptor.encrypt(expected, fileKeyString.getBytes("utf-8"));
        String actual = encryptor.decrypt(value, fileKeyString.getBytes("utf-8"));

        assertEquals(getCallerMethodName(), actual, expected);
    }

    @Test
    public void testDoubleEncryption2() throws Exception {

        Encryptor encryptor = new Encryptor();

        String seed = EncryptDecryptSeed;
        String secureKey = generateSecureKey(seed);
        String fileKeyString = encryptor.encrypt(secureKey);

        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcde";

        String value = encryptor.encrypt(expected, fileKeyString.getBytes("utf-8"));
        String actual = encryptor.decrypt(value, fileKeyString.getBytes("utf-8"));

        assertEquals(getCallerMethodName(), actual, expected);
    }

    @Test
    public void testEncryptionByKeyFile() throws Exception {

        Encryptor encryptor = new Encryptor();

        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcde";

        String value = encryptor.encrypt(expected);
        String actual = encryptor.decrypt(value);

        assertEquals(getCallerMethodName(), actual, expected);
    }

    @Test
    public void testAESEncryptDecrypt() throws Exception {

        Encryptor encryptor = new Encryptor();

        String key = "TOPSECRET";
        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcde";

        String value = encryptor.encrypt_aes(expected, key);
        String actual = encryptor.decrypt_aes(value, key);

        assertEquals(getCallerMethodName(), actual, expected);
    }

    @Test
    public void testBase64() throws Exception {

        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+";
        String value = Encryptor.encode(expected.getBytes("utf-8"));
        String actual = new String(Encryptor.decode(value));

        assertEquals(getCallerMethodName(), expected.length(), actual.length());
        assertEquals(getCallerMethodName(), expected, actual);
    }

    @Test
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

    @Test
    public void testEncryptSecretKey() throws Exception {

        String secretKey = "abcdefghijklmnopqrstuvwxyz0123";

        // select SHA2(<secret-key>, 512);
        String expected = "f3182a236dc58480127051ac5b5bfcacec5af0ae0a2ddb3d7735d9ff7aeefe2fed6f58271a77ec086c2a6f5c5bee73be8e45292104d556b01b9cae56115c200b";

        String actual = Encryptor.hash16(secretKey, "SHA-512");

        assertEquals(getCallerMethodName(), expected.toUpperCase(), actual.toUpperCase());
    }

    @Test
    public void testAESEncrypt() throws Exception {

        String secretKey = "abcdefghijklmnopqrstuvwxyz0123";
        String value = "ALPHABETA";

        // select SHA2(<secret-key>, 512);
        String expected = "C69E6DF28FC47B651A9E049189E37230";

        String actual = encryptor.encrypt_aes(value, Encryptor.hash16(secretKey, "SHA-512"));

        assertEquals(getCallerMethodName(), expected.toUpperCase(), actual.toUpperCase());
    }

    @Test
    public void testAESEncrypt1() throws Exception {

        String key = "0123456789012345";
        String value = "password";

        // select SHA2(<secret-key>, 512);
        String expected = "E75C7C56AFB3EA4360A9856456F1C8A2";

        String actual = encryptor.encrypt_aes(value, key);

        assertEquals(getCallerMethodName(), expected.toUpperCase(), actual.toUpperCase());
    }

    @Test
    public void testAESDecrypt() throws Exception {

        String key = "0123456789012345";
        String expected = "password";

        // select SHA2(<secret-key>, 512);
        String value = "E75C7C56AFB3EA4360A9856456F1C8A2";

        String actual = encryptor.decrypt_aes(value, key);

        assertEquals(getCallerMethodName(), expected.toUpperCase(), actual.toUpperCase());
    }

    @Test
    public void testAESDecrypt10000() throws Exception {

        for (int i = 0; i < 10000; i++) {

            String key = "0123456789012345";
            String expected = "password";

            // select SHA2(<secret-key>, 512);
            String value = "E75C7C56AFB3EA4360A9856456F1C8A2";

            String actual = encryptor.decrypt_aes(value, key);

            assertEquals(getCallerMethodName(), expected.toUpperCase(), actual.toUpperCase());
        }
    }

    private void encryptFile(String filename) throws Exception {

        Encryptor encryptor = new Encryptor();
        writeFile(filename, encryptor.encrypt(readFile(filename)));
    }

    private String generateSecureKey(String password) throws Exception {

        int iterations = 1024;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt().getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 128);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return new String(Encryptor.encode(skf.generateSecret(spec).getEncoded()));
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