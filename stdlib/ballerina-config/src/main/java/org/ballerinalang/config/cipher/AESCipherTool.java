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
 */
public class AESCipherTool {

    private final String algorithm = "AES";
    private final int keyLength = 16;
    private final SecretKey secretKey;

    /**
     * @param secretKeyStr User secret String to encode and decode a value.
     */
    public AESCipherTool(String secretKeyStr) {
        this.secretKey = new SecretKeySpec(fixSecretKeyLength(secretKeyStr, keyLength).getBytes(), algorithm);
    }

    /**
     * This method is used to encrypt a given value.
     *
     * @param value Value to be encrypted.
     * @return Encrypted value.
     */
    public String encrypt(String value) {
        try {
            Cipher encryptionCipher = Cipher.getInstance(algorithm);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return encodeBase64(encryptionCipher.doFinal(value.getBytes()));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException err) {
            throw new BallerinaSecurityException(err.getMessage(), err);
        }
    }

    /**
     * This method is used to decrypt a given encrypted value.
     *
     * @param value Encrypted value to be decrypted.
     * @return Decrypted value.
     */
    public String decrypt(String value) {
        try {
            Cipher decryptionCipher = Cipher.getInstance(algorithm);
            decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(decryptionCipher.doFinal(decodeBase64(value)));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException err) {
            throw new BallerinaSecurityException(err.getMessage(), err);
        }
    }

    private String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] decodeBase64(String encodedValue) {
        return Base64.getDecoder().decode(encodedValue.getBytes());
    }

    private String fixSecretKeyLength(String key, int length) {
        int paddingLength = key.length() % length;
        for (int i = 0; i < length - paddingLength; i++) {
            key = key.concat(" ");
        }
        return key;
    }
}
