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
 * @since 0.964.0
 */
public class AESCipherTool {

    private static final String ALGORITHM_AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_SHA_256 = "SHA-256";
    private static final String DEFAULT_INITIALIZATION_VECTOR = "0123456789ABCDEF";

    private final Cipher encryptionCipher;
    private final Cipher decryptionCipher;

    /**
     * @param userSecret User secret String to encode and decode a value.
     * @throws AESCipherToolException if Any error occurs when preparing the tool with given user data.
     */
    public AESCipherTool(String userSecret) throws AESCipherToolException {
        this(userSecret, null);
    }

    /**
     * @param userSecret User secret String to encode and decode a value.
     * @param initializingVector Initializing vector to encode and decode values.
     * @throws AESCipherToolException if Any error occurs when preparing the tool with given user data.
     */
    public AESCipherTool(String userSecret, String initializingVector) throws AESCipherToolException {
        try {
            IvParameterSpec ivParameterSpec;
            if (initializingVector == null) {
                ivParameterSpec = new IvParameterSpec(getSHA256Key(DEFAULT_INITIALIZATION_VECTOR, 16));
            } else {
                if (initializingVector.length() > 16) {
                    throw new InvalidKeyException("Initializing vector can only have 16 byte value");
                }
                ivParameterSpec = new IvParameterSpec(getSHA256Key(initializingVector, 16));
            }

            SecretKey secretKey = new SecretKeySpec(getSHA256Key(userSecret, 32), ALGORITHM_AES);
            this.encryptionCipher = Cipher.getInstance(ALGORITHM_AES_CBC_PKCS5);
            this.encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            this.decryptionCipher = Cipher.getInstance(ALGORITHM_AES_CBC_PKCS5);
            this.decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | InvalidKeyException e) {
            throw new AESCipherToolException(e.getMessage(), e);
        }
    }

    /**
     * This method is used to encrypt and base64 encode a plain value.
     *
     * @param value Value to be encrypted.
     * @return Encrypted value.
     */
    public String encrypt(String value) throws AESCipherToolException {
        try {
            return encodeBase64(encryptionCipher.doFinal(getBytes(value)));
        } catch (BadPaddingException | IllegalBlockSizeException err) {
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
            return new String(decryptionCipher.doFinal(decodeBase64(value)), StandardCharsets.UTF_8);
        } catch (BadPaddingException | IllegalBlockSizeException err) {
            throw new AESCipherToolException(err.getMessage(), err);
        }
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
