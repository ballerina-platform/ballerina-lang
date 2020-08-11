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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.certificatevalidation;

import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.contractimpl.listener.OCSPResponseBuilder;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * A test case for testing OCSP stapling.
 */
public class OCSPStaplingTest {

    private List<String> ocspUrlList = new ArrayList<String>();

    @BeforeClass
    public void setUp() throws Exception {
        Utils.setUp("ocspStapling");
    }

    @Test (description = "Tests with ocsp stapling enabled client and a server.")
    public void testOcspStapling() {
        Utils.testResponse();
    }

    @Test (description = "Tests retrieving AIA locations from the certificate inside the keystore.")
    public void testRetrievingAIALocations()
            throws IOException, KeyStoreException, CertificateVerificationException {
        String tlsStoreType = "PKCS12";
        String keyStoreFilePath = "/simple-test-config/localcrt.p12";
        String keyStorePassword = "localpwd";
        KeyStore keyStore = OCSPResponseBuilder
                .getKeyStore(new File(TestUtil.getAbsolutePath(keyStoreFilePath)), keyStorePassword, tlsStoreType);
        Enumeration<String> aliases = keyStore.aliases();
        String alias = "";
        boolean isAliasWithPrivateKey = false;
        while (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
            if (isAliasWithPrivateKey = keyStore.isKeyEntry(alias)) {
                break;
            }
        }
        if (isAliasWithPrivateKey) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            X509Certificate userCertificate = (X509Certificate) certificateChain[0];
            ocspUrlList = OCSPResponseBuilder.getAIALocations(userCertificate);
        }
        String ocspUrl = "http://127.0.0.1:8080";
        assertEquals(ocspUrlList.get(0), ocspUrl, "Failed to list the correct AIA locations");
    }

    @Test (description = "Tests the ocsp response retrieved from the actual CA.")
    public void testretrievingResponseFromWeb() throws Exception {
        Utils utils = new Utils();
        OCSPCache cache = OCSPCache.getCache();
        cache.init(5, 5);
        X509Certificate certificate = utils.getRealPeerCertificate();
        ocspUrlList = OCSPResponseBuilder.getAIALocations(certificate);
        OCSPReq ocspReq = Utils.getOCSPRequest(certificate, certificate.getSerialNumber());
        OCSPResp ocspResp = OCSPResponseBuilder.getOCSPResponse(ocspUrlList, ocspReq, certificate, cache);
        assertEquals(ocspResp.getStatus(), OCSPResponseStatus.SUCCESSFUL);
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        Utils.cleanUp();
    }
}

