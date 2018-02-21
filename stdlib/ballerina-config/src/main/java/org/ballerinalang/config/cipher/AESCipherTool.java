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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * This tools is used to encrypt and decrypt data using AES Algorithm.
 *
 * @since 0.963.0
 */
public class AESCipherTool {

    private static final String ALGORITHM = "AES";
    private static final int KEY_LENGTH = 16;
    private final Cipher encryptionCipher;
    private final Cipher decryptionCipher;

    /**
     * @param userSecret User secret String to encode and decode a value.
     */
    public AESCipherTool(String userSecret) throws AESCipherToolException {
        try {
            SecretKey secretKey = new SecretKeySpec(getBytes(fixSecretKeyLength(userSecret, KEY_LENGTH)), ALGORITHM);
            this.encryptionCipher = Cipher.getInstance(ALGORITHM);
            this.encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            this.decryptionCipher = Cipher.getInstance(ALGORITHM);
            this.decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AESCipherToolException(e.getMessage(), e);
        }
    }

    /**
     * This method is used to encrypt a given value.
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
     * This method is used to decrypt a given encrypted value.
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

    private String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] decodeBase64(String encodedValue) {
        return Base64.getDecoder().decode(getBytes(encodedValue));
    }

    private byte[] getBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private String fixSecretKeyLength(String key, int length) {
        int paddingLength = key.length() % length;
        for (int i = 0; i < length - paddingLength; i++) {
            key = key.concat(" ");
        }
        return key;
    }
}
