/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.config.cipher;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This tools is used to encrypt and decrypt data using AES Algorithm CBC mode and PKCS #5 padding.
 *
 * @since 0.965.0
 */
public class AESCipherTool {

    private static final String ALGORITHM_AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_SHA_256 = "SHA-256";
    private static final int IV_SIZE = 16;
    private static final int SECRET_KEY_LENGTH = 16; // TODO: Make this 32 again after switching to Java 9

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * @param userSecret User secret String to encode and decode a value.
     * @throws AESCipherToolException if Any error occurs when preparing the tool with given user data.
     */
    public AESCipherTool(String userSecret) throws AESCipherToolException {
        this.secretKey = new SecretKeySpec(getSHA256Key(userSecret, SECRET_KEY_LENGTH), ALGORITHM_AES);
        this.secureRandom = new SecureRandom();
    }

    /**
     * This method is used to encrypt and base64 encode a plain value.
     *
     * @param value Value to be encrypted.
     * @return Encrypted value.
     */
    public String encrypt(String value) throws AESCipherToolException {
        try {
            byte[] ivByteArray = getSecureRandomBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivByteArray);
            Cipher encryptionCipher = Cipher.getInstance(ALGORITHM_AES_CBC_PKCS5);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] encryptedBytes = encryptionCipher.doFinal(getBytes(value));
            return encodeBase64(appendByteArrays(ivByteArray, encryptedBytes));
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException  err) {
            throw new AESCipherToolException(err.getMessage(), err);
        }
    }

    /**
     * This method is used to decrypt a given encrypted and base64 encoded value.
     *
     * @param value Encrypted value to be decrypted.
     * @return Decrypted value.
     */
    public String decrypt(String value) throws AESCipherToolException {
        try {
            byte[] decodedByteArray = decodeBase64(value);
            IvParameterSpec ivParameterSpec =
                    new IvParameterSpec(Arrays.copyOfRange(decodedByteArray, 0, IV_SIZE));
            Cipher decryptionCipher = Cipher.getInstance(ALGORITHM_AES_CBC_PKCS5);
            decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] encryptedByteArray = Arrays.copyOfRange(decodedByteArray, IV_SIZE, decodedByteArray.length);
            return new String(decryptionCipher.doFinal(encryptedByteArray), StandardCharsets.UTF_8);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException err) {
            throw new AESCipherToolException(err.getMessage(), err);
        }
    }

    private byte[] appendByteArrays(byte[] ivArray, byte[] encryptedArray) {
        int arrayLength = ivArray.length + encryptedArray.length;
        byte[] bytes = new byte[arrayLength];
        System.arraycopy(ivArray, 0, bytes, 0, ivArray.length);
        System.arraycopy(encryptedArray, 0, bytes, ivArray.length, encryptedArray.length);
        return bytes;
    }

    private byte[] getSecureRandomBytes() {
        byte[] bytes = new byte[IV_SIZE];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private byte[] getSHA256Key(String key, int keyLengthInBytes) throws AESCipherToolException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM_SHA_256);
            messageDigest.update(getBytes(key));
            byte[] keyBytes = new byte[keyLengthInBytes];
            System.arraycopy(messageDigest.digest(), 0, keyBytes, 0, keyBytes.length);
            return keyBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new AESCipherToolException(e.getMessage(), e);
        }
    }

    private String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] decodeBase64(String encodedValue) {
        return Base64.getDecoder().decode(getBytes(encodedValue));
    }

    private byte[] getBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
