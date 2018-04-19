/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.jwt.crypto;

import org.ballerinalang.nativeimpl.internal.jwt.crypto.JWSSigner;
import org.ballerinalang.nativeimpl.internal.jwt.crypto.RSASigner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;

/**
 * Test native functions used to sign JWT token.
 */
public class JWTSignerTest {

    @Test(description = "Test RSASigner with SHA-256 hashing ")
    public void testRSA256Verifier() throws Exception {
        String data = "ewogICJhbGciOiAiUlMyNTYiLAogICJ0eXAiOiAiSldUIgp9.ewogICJzdWIiOiAiMTIzNjU0IiwKICAibmFtZSI6ICJK" +
                "b2huIiwKICAiaXNzIjogIndzbzIiLAogICJhdWQiOiAiYmFsbGVyaW5hIiwKICAiZXhwIjogMTUxOTk5NDU2NDI0OQp9";
        String signature = "X10zu93zSfo0TJQdyDrWZEr5RfX-8vA3dNuxkVRhhj_v51Q7FQ2WUP_rQpJGL2VyFpu23W1ypXXGiDMqDZodqQ8v" +
                "cf1ElO_qIC6ls0Ay6fHzjpLQdVU7bkFfpuqoboXfOSLCxwzHnvKNIWqmVBHW7CE4jPjb7_11QpT1CxwIUSXtVFk2" +
                "Z3gpCyfwCVe_JXtBwDbyCQGO_g2tKUSwHvvNDu3THgCcB2ALIS_JznaK9iPf55YmeNwB_KRGkaY-VLvQ5iUILWp2" +
                "J5SF3QavfXMNhv8GoEDBe2ZfbQgH5E-TpakoL51Ix8vELiznVl7sbtAqlD97440hW3wXoq68kboCVQ==";
        String algorithm = "RS256";
        PrivateKey privateKey = getPrivateKey();
        JWSSigner signer = new RSASigner(privateKey);
        Assert.assertEquals(signature, signer.sign(data, algorithm));
    }

    private PrivateKey getPrivateKey() throws Exception {
        KeyStore keyStore;
        InputStream file = new FileInputStream(new File(getClass().getClassLoader().getResource(
                "datafiles/security/keyStore/ballerinaKeystore.p12").getPath()));
        keyStore = java.security.KeyStore.getInstance("pkcs12");
        keyStore.load(file, "ballerina".toCharArray());
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("ballerina", new KeyStore
                .PasswordProtection("ballerina".toCharArray()));
        return pkEntry.getPrivateKey();
    }
}
