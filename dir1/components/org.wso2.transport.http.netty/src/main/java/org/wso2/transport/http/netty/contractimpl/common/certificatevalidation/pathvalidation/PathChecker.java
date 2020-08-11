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

package org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.pathvalidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationStatus;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationVerifier;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * This class is used by CertificatePathValidator to check revocation status of the certificate chain.
 * Certificates in the chain will be passed to the check(..,..) method one by one.
 * This is not Thread safe since the process is state full. Should not be shared among threads.
 */
public class PathChecker extends PKIXCertPathChecker {

    private X509Certificate[] certChainArray;
    private RevocationVerifier verifier;
    private int position;
    private static final Logger LOG = LoggerFactory.getLogger(PathChecker.class);

    protected PathChecker(X509Certificate[] certChainArray, RevocationVerifier verifier) {
        this.certChainArray = certChainArray;
        //initialize position to Root Certificate position.
        this.position = certChainArray.length - 1;
        this.verifier = verifier;
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("Forward checking is not supported");
        }
    }

    /**
     * Forward checking is not supported. Certificates should be passed from the most trusted CA certificate
     * to the target certificate. This is the default implementation of the Path validator used
     * CertPathValidator.getInstance("PKIX", "BC") in CertificatePathValidator;
     */
    @Override
    public boolean isForwardCheckingSupported() {
        return false;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Collections.emptySet();
    }

    /**
     * Used by CertPathValidator to pass the certificates one by one from the certificate chain.
     *
     * @param cert the certificate passed to be checked.
     * @param unresolvedCritExts not used in this method.
     * @throws CertPathValidatorException if any error occurs while verifying the status given by CA.
     */
    @Override
    public void check(Certificate cert, Collection<String> unresolvedCritExts)
            throws CertPathValidatorException {
        RevocationStatus status;
        try {
            status = verifier.checkRevocationStatus((X509Certificate) cert, nextIssuer());
            if (LOG.isInfoEnabled()) {
                LOG.info("Certificate status is: {}", status.getMessage());
        }
            if (status != RevocationStatus.GOOD) {
                throw new CertPathValidatorException("Revocation Status is Not Good");
            }
        } catch (CertificateVerificationException e) {
            throw new CertPathValidatorException(e);
        }
    }

    /**
     * @return the immediate issuer certificate of the current certificate which is being checked. This is tracked
     * by the position variable
     */
    private X509Certificate nextIssuer() {
        //get immediate issuer.
        if (position > 0) {
            return certChainArray[position--];
        } else {
            throw new ArrayIndexOutOfBoundsException("Certificate Chain Index Out of Bounds");
        }
    }
}

