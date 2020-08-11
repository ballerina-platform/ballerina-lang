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

package org.wso2.transport.http.netty.contractimpl.common.certificatevalidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl.CRLCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl.CRLVerifier;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.pathvalidation.CertificatePathValidator;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.cert.CertificateEncodingException;

/**
 * Manager class responsible for verifying certificates. This class will use the available verifiers according to
 * a predefined policy (First check with OCSP access end point and then move to CRL distribution point to validate).
 */
public class RevocationVerificationManager {

    private int cacheSize = Constants.CACHE_DEFAULT_ALLOCATED_SIZE;
    private int cacheDelayMins = Constants.CACHE_DEFAULT_DELAY_MINS;
    private static final Logger LOG = LoggerFactory.getLogger(RevocationVerificationManager.class);

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
     * @throws CertificateVerificationException Occurs when certificate fails to be validated from both OCSP and CRL.
     * @return true If the process of certificate revocation becomes successful.
     */
    public boolean verifyRevocationStatus(javax.security.cert.X509Certificate[] peerCertificates)
            throws CertificateVerificationException {

        X509Certificate[] convertedCertificates = convert(peerCertificates);

        long start = System.currentTimeMillis();
        // If not set by the user, default cache size will be 50 and default cache delay will be 15 mins.
        OCSPCache ocspCache = OCSPCache.getCache();
        ocspCache.init(cacheSize, cacheDelayMins);
        CRLCache crlCache = CRLCache.getCache();
        crlCache.init(cacheSize, cacheDelayMins);

        RevocationVerifier[] verifiers = { new OCSPVerifier(ocspCache), new CRLVerifier(crlCache) };

        for (RevocationVerifier verifier : verifiers) {
            try {
                CertificatePathValidator pathValidator = new CertificatePathValidator(convertedCertificates, verifier);
                pathValidator.validatePath();
                if (LOG.isInfoEnabled()) {
                    LOG.info("Path verification is successful. Took {} ms.", System.currentTimeMillis() - start);
                }
                return true;
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} failed.", verifier.getClass().getSimpleName());
                    LOG.debug("Certificate verification with {} failed. ", verifier.getClass().getSimpleName(), e);
                }
            }
        }
        throw new CertificateVerificationException("Path verification failed for both OCSP and CRL");
    }

    /** Convert certificates and create a certificate chain.
     *
     * @param certs array of javax.security.cert.X509Certificate[] s.
     * @return the converted array of java.security.cert.X509Certificate[] s.
     * @throws CertificateVerificationException If an error occurs while converting certificates
     * from java to javax
     */
    private X509Certificate[] convert(javax.security.cert.X509Certificate[] certs)
            throws CertificateVerificationException {
        X509Certificate[] certChain = new X509Certificate[certs.length];
        Throwable exceptionThrown;
        for (int i = 0; i < certs.length; i++) {
            try {
                byte[] encoded = certs[i].getEncoded();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encoded);
                CertificateFactory certificateFactory = CertificateFactory.getInstance(Constants.X_509);
                certChain[i] = ((X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream));
                continue;
            } catch (CertificateEncodingException | CertificateException e) {
                exceptionThrown = e;
            }
            throw new CertificateVerificationException("Cant Convert certificates from javax to java", exceptionThrown);
        }
        return certChain;
    }
}

