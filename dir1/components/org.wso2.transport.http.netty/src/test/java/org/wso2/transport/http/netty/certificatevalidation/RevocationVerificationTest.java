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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationVerifier;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl.CRLCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl.CRLVerifier;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.pathvalidation.CertificatePathValidator;

import java.security.Security;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

public class RevocationVerificationTest {

    /**
     * Tests CRL Path Validation with the use of a real certificate chain. The verification process will make
     * HTTP calls to remote CRL server URLs extracted from the certificates in the chain. Usually these certificates
     * will not be revoked. So the path validation must be successful to pass the test. In case they are revoked
     * or expired, new certificates should be added to the resources directory and Constants should be modified
     * accordingly. See the interface Constants for expiry dates of the certificates.
     *
     * @throws Exception
     */
    @Test
    public void testCRLPathValidation() throws Exception {
        //Add BouncyCastle as Security Provider.
        Security.addProvider(new BouncyCastleProvider());
        Utils utils = new Utils();
        X509Certificate[] certificates = utils.getRealCertificateChain();
        Throwable throwable = null;
        try {
            crlPathValidation(certificates);
        } catch (CertificateVerificationException e) {
            //Path Verification Should Pass. This catch block should not be called.
            throwable = e;
        }
        assertNull(throwable);
    }

    /**
     * Tests CRL path validation with fake certificates. The path validation should fail since they are fake and do not
     * contain proper information.
     *
     * @throws Exception
     */
    @Test
    public void testCRLPathValidationWithFakeCerts() throws Exception {
        //Add BouncyCastle as Security Provider.
        Security.addProvider(new BouncyCastleProvider());
        Utils utils = new Utils();
        X509Certificate[] fakeCertificates = utils.getFakeCertificateChain();
        Throwable throwable = null;
        try {
            crlPathValidation(fakeCertificates);
        } catch (CertificateVerificationException e) {
            //Path Verification Should fail. So this catch block should be called.
            throwable = e;
        }
        assertNotNull(throwable);
    }

    /**
     * Tests path validation with OCSP. The process makes remote HTTP requests to corresponding OCSP servers at the
     * certificate authorities. The path validation must be successful to pass the test.
     *
     * @throws Exception
     */
    @Test
    public void testOCSPPathValidation() throws Exception {
        //Add BouncyCastle as Security Provider.
        Security.addProvider(new BouncyCastleProvider());
        Utils utils = new Utils();
        X509Certificate[] certificates = utils.getRealCertificateChain();
        ocspPathValidation(certificates);
    }

    /**
     * Tests OCSP path validation with a chain of fake certificates. In order to pass the test, the path validation
     * should fail since the certificates are fake and do not contain right information.
     *
     * @throws Exception
     */
    @Test
    public void testOCSPPathValidationWithFakeCerts() throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        Utils utils = new Utils();
        X509Certificate[] fackeCertificates = utils.getFakeCertificateChain();
        Throwable throwable = null;
        try {
            ocspPathValidation(fackeCertificates);
        } catch (CertificateVerificationException e) {
            //Path Verification Should fail. So this catch block should be called.
            throwable = e;
        }
        assertNotNull(throwable);
    }

    private void crlPathValidation(X509Certificate[] certChain) throws Exception {

        CRLCache crlCache = CRLCache.getCache();
        crlCache.init(5, 5);
        RevocationVerifier verifier = new CRLVerifier(crlCache);
        CertificatePathValidator pathValidator = new CertificatePathValidator(certChain, verifier);
        pathValidator.validatePath();
    }

    private void ocspPathValidation(X509Certificate[] certChain) throws Exception {

        OCSPCache ocspCache = OCSPCache.getCache();
        ocspCache.init(5, 5);
        RevocationVerifier verifier = new OCSPVerifier(ocspCache);
        CertificatePathValidator pathValidator = new CertificatePathValidator(certChain, verifier);
        pathValidator.validatePath();
    }
}

