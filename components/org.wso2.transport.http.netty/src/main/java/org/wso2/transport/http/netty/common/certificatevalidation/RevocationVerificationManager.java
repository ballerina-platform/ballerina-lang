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

package org.wso2.transport.http.netty.common.certificatevalidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.certificatevalidation.crl.CRLCache;
import org.wso2.transport.http.netty.common.certificatevalidation.crl.CRLVerifier;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.common.certificatevalidation.pathvalidation.CertificatePathValidator;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.cert.CertificateEncodingException;

/**
 * Manager class responsible for verifying certificates. This class will use the available verifiers according to
 * a predefined policy.
 */
public class RevocationVerificationManager {

    private int cacheSize = Constants.CACHE_DEFAULT_ALLOCATED_SIZE;
    private int cacheDelayMins = Constants.CACHE_DEFAULT_DELAY_MINS;
    private static final Logger log = LoggerFactory.getLogger(RevocationVerificationManager.class);

    public RevocationVerificationManager(Integer cacheAllocatedSize, Integer cacheDelayMins) {

        if (cacheAllocatedSize != null && cacheAllocatedSize > Constants.CACHE_MIN_ALLOCATED_SIZE
                && cacheAllocatedSize < Constants.CACHE_MAX_ALLOCATED_SIZE) {
            this.cacheSize = cacheAllocatedSize;
        }
        if (cacheDelayMins != null && cacheDelayMins > Constants.CACHE_MIN_DELAY_MINS
                && cacheDelayMins < Constants.CACHE_MAX_DELAY_MINS) {
            this.cacheDelayMins = cacheDelayMins;
        }
    }

    /**
     * This method first tries to verify the given certificate chain using OCSP since OCSP verification is
     * faster. If that fails it tries to do the verification using CRL.
     *
     * @param peerCertificates javax.security.cert.X509Certificate[] array of peer certificate chain from peer/client.
     * @throws CertificateVerificationException
     */
    public void verifyRevocationStatus(javax.security.cert.X509Certificate[] peerCertificates)
            throws CertificateVerificationException {

        X509Certificate[] convertedCertificates = convert(peerCertificates);

        long start = System.currentTimeMillis();

        OCSPCache ocspCache = OCSPCache.getCache();
        ocspCache.init(cacheSize, cacheDelayMins);
        CRLCache crlCache = CRLCache.getCache();
        crlCache.init(cacheSize, cacheDelayMins);

        RevocationVerifier[] verifiers = { new OCSPVerifier(ocspCache), new CRLVerifier(crlCache) };

        for (RevocationVerifier verifier : verifiers) {
            try {
                CertificatePathValidator pathValidator = new CertificatePathValidator(convertedCertificates, verifier);
                pathValidator.validatePath();
                log.info("Path verification Successful. Took " + (System.currentTimeMillis() - start) + " ms.");
                return;
            } catch (Exception e) {
                log.info(verifier.getClass().getSimpleName() + " failed.");
                log.debug("Certificate verification with " + verifier.getClass().getSimpleName() + " failed. ", e);
            }
        }
        throw new CertificateVerificationException("Path Verification Failed for both OCSP and CRL");
    }

    /**
     * @param certs array of javax.security.cert.X509Certificate[] s.
     * @return the converted array of java.security.cert.X509Certificate[] s.
     * @throws CertificateVerificationException
     */
    private X509Certificate[] convert(javax.security.cert.X509Certificate[] certs)
            throws CertificateVerificationException {
        X509Certificate[] certChain = new X509Certificate[certs.length];
        Throwable exceptionThrown;
        for (int i = 0; i < certs.length; i++) {
            try {
                byte[] encoded = certs[i].getEncoded();
                ByteArrayInputStream bis = new ByteArrayInputStream(encoded);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                certChain[i] = ((X509Certificate) cf.generateCertificate(bis));
                continue;
            } catch (CertificateEncodingException e) {
                exceptionThrown = e;
            } catch (CertificateException e) {
                exceptionThrown = e;
            }
            throw new CertificateVerificationException("Cant Convert certificates from javax to java", exceptionThrown);
        }
        return certChain;
    }
}

