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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationVerifier;

import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Used to validate the revocation status of a certificate chain obtained from the peer. A revocation verifier
 * (OCSP or CRL) should be given. Must be used only once when validating certificate chain for an SSLSession.
 * Create a new instance if need to be reused because the path validation process is state-full.
 * Not thread safe.
 */
public class CertificatePathValidator {
    private PathChecker pathChecker;

    // Certificate Chain with Root CA certificate (eg: peer cert, issuer cert, root cert)
    private List<X509Certificate> fullCertChain;
    // Certificate Chain without Root CA certificate. (eg: peer cert, issuer cert)
    private List<X509Certificate> certChain;
    private static final Logger LOG = LoggerFactory.getLogger(CertificatePathValidator.class);

    public CertificatePathValidator(X509Certificate[] certChainArray, RevocationVerifier verifier) {
        this.pathChecker = new PathChecker(certChainArray, verifier);
        init(certChainArray);
    }

    /**
     * Here revocation status checking is started from one below the root certificate in the chain (certChain).
     * Since ssl implementation ensures that at least one certificate in the chain is trusted,
     * we can logically say that the root is trusted.
     */
    private void init(X509Certificate[] certChainArray) {
        X509Certificate[] partCertChainArray = new X509Certificate[certChainArray.length - 1];
        System.arraycopy(certChainArray, 0, partCertChainArray, 0, partCertChainArray.length);
        certChain = Arrays.asList(partCertChainArray);
        fullCertChain = Arrays.asList(certChainArray);
    }

    /**
     * Certificate Path Validation process
     *
     * @throws CertificateVerificationException if validation process fails.
     */
    public void validatePath() throws CertificateVerificationException {

        Security.addProvider(new BouncyCastleProvider());
        CollectionCertStoreParameters params = new CollectionCertStoreParameters(fullCertChain);
        try {
            CertStore store = CertStore.getInstance("Collection", params, Constants.BOUNCY_CASTLE_PROVIDER);

            // create certificate path
            CertificateFactory fact = CertificateFactory
                    .getInstance(Constants.X_509, Constants.BOUNCY_CASTLE_PROVIDER);

            CertPath certPath = fact.generateCertPath(certChain);
            TrustAnchor trustAnchor = new TrustAnchor(fullCertChain.get(fullCertChain.size() - 1), null);
            Set<TrustAnchor> trust = Collections.singleton(trustAnchor);

            // perform validation
            CertPathValidator validator = CertPathValidator
                    .getInstance(Constants.ALGORITHM, Constants.BOUNCY_CASTLE_PROVIDER);
            PKIXParameters param = new PKIXParameters(trust);

            param.addCertPathChecker(pathChecker);
            param.setRevocationEnabled(false);
            param.addCertStore(store);
            param.setDate(new Date());

            validator.validate(certPath, param);
            if (LOG.isInfoEnabled()) {
                LOG.info("Certificate path validated");
            }
        } catch (CertPathValidatorException e) {
            throw new CertificateVerificationException(
                    "Certificate path validation failed on certificate number " + e.getIndex() + ", details: " + e
                            .getMessage(), e);
        } catch (Exception e) {
            throw new CertificateVerificationException("Certificate path validation failed", e);
        }
    }
}

