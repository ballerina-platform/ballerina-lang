/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.common;

import org.ballerinalang.stdlib.internal.jwt.crypto.JWSSigner;
import org.ballerinalang.stdlib.internal.jwt.crypto.RSASigner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;

/**
 * Common util functions used in stdlib tests
 * @since 0.990.3
 */

public class CommonTestUtils {

    // Used in HTTP and Auth modules
    public String generateJWT() throws Exception {
        String header = buildHeader();
        String jwtHeader = new String(Base64.getUrlEncoder().encode(header.getBytes()));
        String body = buildBody();
        String jwtBody = new String(Base64.getUrlEncoder().encode(body.getBytes()));
        String assertion = jwtHeader + "." + jwtBody;
        String algorithm = "RS256";
        PrivateKey privateKey = getPrivateKey();
        JWSSigner signer = new RSASigner(privateKey);
        String signature = signer.sign(assertion, algorithm);
        return assertion + "." + signature;
    }

    private PrivateKey getPrivateKey() throws Exception {
        KeyStore keyStore;
        InputStream file = new FileInputStream(new File(getClass().getClassLoader().getResource(
                "datafiles/keystore/ballerinaKeystore.p12").getPath()));
        keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(file, "ballerina".toCharArray());
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("ballerina", new KeyStore
                .PasswordProtection("ballerina".toCharArray()));
        return pkEntry.getPrivateKey();
    }

    private String buildHeader() {
        return "{\n" +
                "  \"alg\": \"RS256\",\n" +
                "  \"typ\": \"JWT\"\n" +
                "}";
    }

    private String buildBody() {
        long time = System.currentTimeMillis() + 10000000;
        return "{\n" +
                "  \"sub\": \"John\",\n" +
                "  \"iss\": \"wso2\",\n" +
                "  \"aud\": \"ballerina\",\n" +
                "  \"scope\": \"John test Doe\",\n" +
                "  \"roles\": [\"admin\",\"admin2\"],\n" +
                "  \"exp\": " + time + "\n" +
                "}";
    }
}
