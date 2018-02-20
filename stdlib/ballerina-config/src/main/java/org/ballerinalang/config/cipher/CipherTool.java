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
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * This tools is used to encrypt and decrypt data.
 */
public interface CipherTool {

    /**
     * This method is used to encrypt a given value.
     *
     * @param value Value to be encrypted.
     * @param encryptionKey Key which is used to encrypt the value.
     * @return Encrypted value.
     */
    String encrypt(String value, String encryptionKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
                   IllegalBlockSizeException;

    /**
     * This method is used to decrypt a given encrypted value.
     *
     * @param value Encrypted value to be decrypted.
     * @param decryptionKey Key which is used to decrypt the value.
     * @return Decrypted value.
     */
    String decrypt(String value, String decryptionKey)
                           throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException,
                                  IllegalBlockSizeException;
}
