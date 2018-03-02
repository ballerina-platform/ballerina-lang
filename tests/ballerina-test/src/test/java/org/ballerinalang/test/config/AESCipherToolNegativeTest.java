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

package org.ballerinalang.test.config;

import org.ballerinalang.config.cipher.AESCipherTool;
import org.ballerinalang.config.cipher.AESCipherToolException;
import org.testng.annotations.Test;

/**
 * Test the AES Cipher tool negative scenarios.
 *
 * @since 0.964.0
 */
public class AESCipherToolNegativeTest {

    @Test(expectedExceptions = AESCipherToolException.class,
          expectedExceptionsMessageRegExp = "Given final block not properly padded. " +
                  "Such issues can arise if a bad key is used during decryption.")
    public void testEncryptionAndDecryptionWithTwoCipherTools() throws AESCipherToolException {
        String plainText = "this is the plain text";
        String encryptionKey = "abc&xyz";
        String decryptionKey = "xyz&abc";
        AESCipherTool encryptionCipherTool = new AESCipherTool(encryptionKey);
        String encryptedStr = encryptionCipherTool.encrypt(plainText);
        AESCipherTool decryptionCipherTool = new AESCipherTool(decryptionKey);
        decryptionCipherTool.decrypt(encryptedStr);
    }
}
