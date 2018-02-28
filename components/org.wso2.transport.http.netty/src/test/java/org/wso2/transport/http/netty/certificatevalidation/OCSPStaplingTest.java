/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.certificatevalidation;

import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.listener.OCSPResponseBuilder;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Contains test cases for getting AIA locations from the certificate and to test OCSPResponse when CA fails to respond.
 */
public class OCSPStaplingTest {

    private String keyStoreFilePath = "/simple-test-config/localcrt.p12";
    private String keyStorePassword = "localpwd";
    private String tlsStoreType = "PKCS12";
    private X509Certificate userCertificate;
    private List<String> ocspUrlList = new ArrayList<String>();
    private int cacheSize = 51;
    private int cacheDelay = 2;
    private String ocspUrl = "http://127.0.0.1:8080";

    @Test
    public void testRetrievingAIALocations()
            throws IOException, KeyStoreException, CertificateVerificationException {
        KeyStore keyStore = OCSPResponseBuilder
                .getKeyStore(new File(TestUtil.getAbsolutePath(keyStoreFilePath)), keyStorePassword, tlsStoreType);
        Enumeration<String> aliases = keyStore.aliases();
        String alias = "";
        boolean isAliasWithPrivateKey = false;
        while (aliases.hasMoreElements()) {
            alias = (String) aliases.nextElement();
            if (isAliasWithPrivateKey = keyStore.isKeyEntry(alias)) {
                break;
            }
        }
        if (isAliasWithPrivateKey) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            userCertificate = (X509Certificate) certificateChain[0];
            ocspUrlList = OCSPVerifier.getAIALocations(userCertificate);
        }
        assertEquals(ocspUrlList.get(0).toString(), ocspUrl, "Failed to list the correct AIA locations");
    }

    @Test
    public void testOCSPResponse()
            throws NoSuchAlgorithmException, CertificateVerificationException, UnrecoverableEntryException,
            KeyStoreException, IOException {
        SSLConfig sslConfig = new SSLConfig(new File(TestUtil.getAbsolutePath(keyStoreFilePath)), keyStorePassword);
        sslConfig.setTLSStoreType(tlsStoreType);
        Throwable throwable = null;
        try {
            OCSPResponseBuilder.getOcspResponse(sslConfig, cacheSize, cacheDelay);
        } catch (CertificateVerificationException e) {
            throwable = e;
        }
        boolean hasException = false;
        if (throwable.getMessage().contains("Failed to get OCSP responses from CA") || throwable.getMessage()
                .contains("Cannot get OCSP Response from url")) {
            hasException = true;
        }
        assertTrue(hasException, "Process has failed before getting the OCSP response");
    }
}

