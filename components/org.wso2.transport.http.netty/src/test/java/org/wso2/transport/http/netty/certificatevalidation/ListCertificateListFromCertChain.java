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
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.wso2.transport.http.netty.contractimpl.listener.OCSPResponseBuilder.getCertInfo;

/**
 * This test is to test retrieving certificate list from certificate chains.
 */
public class ListCertificateListFromCertChain {

    @Test
    public void testCertChain() {
        SSLConfig sslConfig = new SSLConfig();
        String certChain = "/simple-test-config/certsAndKeys/ocspCertChain.crt";
        sslConfig.setServerCertificates(new File(TestUtil.getAbsolutePath(certChain)));
        try {
            List<X509Certificate> certsList = getCertInfo(sslConfig);
            assertEquals(2, certsList.size(), "Failed to list the certificate chain correctly");
        } catch (Exception e) {
            TestUtil.handleException("Failed to list the certificates from the certChain", e);
        }
    }
}

