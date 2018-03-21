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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the AES Cipher tool.
 *
 * @since 0.965.0
 */
public class AESCipherToolTest {

    @Test
    public void testEncryptionAndDecryptionWithSameTool() throws AESCipherToolException {
        String plainText1 = "this is the plain text one";
        String plainText2 = "this is the plain text two";
        String encryptionKey = "abc&xyz";
        AESCipherTool aesCipherTool = new AESCipherTool(encryptionKey);
        String encryptedStr1 = aesCipherTool.encrypt(plainText1);
        Assert.assertEquals(plainText1, aesCipherTool.decrypt(encryptedStr1));
        String encryptedStr2 = aesCipherTool.encrypt(plainText2);
        Assert.assertEquals(plainText2, aesCipherTool.decrypt(encryptedStr2));
    }

    @Test
    public void testEncryptionAndDecryptionWithTwoCipherTools() throws AESCipherToolException {
        String plainText = "this is the plain text";
        String encryptionKey = "abc&xyz";
        AESCipherTool encryptionCipherTool = new AESCipherTool(encryptionKey);
        String encryptedStr = encryptionCipherTool.encrypt(plainText);
        AESCipherTool decryptionCipherTool = new AESCipherTool(encryptionKey);
        Assert.assertEquals(plainText, decryptionCipherTool.decrypt(encryptedStr));
    }

    @Test
    public void testEncryptionAndDecryptionWithLongSecret() throws AESCipherToolException {
        String plainText = "this is the plain text";
        String encryptionKey = "abcdefghijklmnopqrstuvwxyz";
        AESCipherTool aesCipherTool = new AESCipherTool(encryptionKey);
        String encryptedStr = aesCipherTool.encrypt(plainText);
        Assert.assertEquals(plainText, aesCipherTool.decrypt(encryptedStr));
    }
}
