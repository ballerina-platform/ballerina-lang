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

package org.wso2.transport.http.netty.contractimpl.listener;

import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A class for generating OCSP response.
 */
public class OCSPResponseBuilder {
    private OCSPResponseBuilder() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(OCSPResponseBuilder.class);
    private static List<X509Certificate> certList = new ArrayList<>();
    private static X509Certificate userCertificate = null;
    private static X509Certificate issuer = null;

    static OCSPResp generateOcspResponse(SSLConfig sslConfig, int cacheAllocatedSize, int cacheDelay)
            throws IOException, KeyStoreException, CertificateVerificationException, CertificateException {

        int cacheSize = Constants.CACHE_DEFAULT_ALLOCATED_SIZE;
        int cacheDelayMins = Constants.CACHE_DEFAULT_DELAY_MINS;

        if (cacheAllocatedSize != 0 && cacheAllocatedSize > Constants.CACHE_MIN_ALLOCATED_SIZE
                && cacheAllocatedSize < Constants.CACHE_MAX_ALLOCATED_SIZE) {
            cacheSize = cacheAllocatedSize;
        }
        if (cacheDelay != 0 && cacheDelay > Constants.CACHE_MIN_DELAY_MINS
                && cacheDelay < Constants.CACHE_MAX_DELAY_MINS) {
            cacheDelayMins = cacheDelay;
        }

        OCSPCache ocspCache = OCSPCache.getCache();
        ocspCache.init(cacheSize, cacheDelayMins);

        if (sslConfig.getKeyStore() != null) {
            KeyStore keyStore = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass(),
                    sslConfig.getTLSStoreType());
            if (keyStore != null) {
                getUserCerAndIssuer(keyStore);
            }
        } else {
            certList = getCertInfo(sslConfig);
            userCertificate = certList.get(0);
            issuer = certList.get(1);
        }
        List<String> locations;
        if (userCertificate == null) {
            throw new CertificateVerificationException("Could not get revocation status from OCSP.");
        } else {
            //Check whether the ocsp response is still there in the cache.
            // If it is there, we don't need to get it from CA.
            if (ocspCache.getOCSPCacheValue(userCertificate.getSerialNumber()) != null) {
                return ocspCache.getOCSPCacheValue(userCertificate.getSerialNumber());
            } else {
                OCSPReq request;
                request = OCSPVerifier.generateOCSPRequest(issuer, userCertificate.getSerialNumber());
                locations = getAIALocations(userCertificate);
                return getOCSPResponse(locations, request, userCertificate, ocspCache);
            }
        }
    }

    private static void getUserCerAndIssuer(KeyStore keyStore) throws KeyStoreException {
        Certificate[] certificateChain;
        //Get own certificate and the issuer certificate.
        Enumeration<String> aliases = keyStore.aliases();
        String alias = "";
        boolean isAliasWithPrivateKey = false;
        while (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
            isAliasWithPrivateKey = keyStore.isKeyEntry(alias);
            if (isAliasWithPrivateKey) {
                break;
            }
        }
        if (isAliasWithPrivateKey) {
            // Load certificate chain
            certificateChain = keyStore.getCertificateChain(alias);
            //user certificate is there in the 0 th position of a certificate chain.
            userCertificate = (X509Certificate) certificateChain[0];
            //issuer certificate is in the last position of a certificate chain.
            issuer = (X509Certificate) certificateChain[certificateChain.length - 1];
        }
    }

    /**
     * Method to create a keystore and return.
     *
     * @param keyStoreFile     keyStore file
     * @param keyStorePassword keyStore password
     * @param tlsStoreType     PKCS12
     * @return keystore
     * @throws IOException Occurs if it fails to create the keystore.
     */
    public static KeyStore getKeyStore(File keyStoreFile, String keyStorePassword, String tlsStoreType)
            throws IOException {
        KeyStore keyStore = null;
        if (keyStoreFile != null && keyStorePassword != null) {
            try (InputStream inputStream = new FileInputStream(keyStoreFile)) {
                keyStore = KeyStore.getInstance(tlsStoreType);
                keyStore.load(inputStream, keyStorePassword.toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e);
            }
        }
        return keyStore;
    }

    /**
     * List Certificate authority url information.
     *
     * @param userCertificate User's own certificate.
     * @return AIA Locations
     * @throws CertificateVerificationException If an error occurs while getting the AIA locations from the certificate.
     */
    public static List<String> getAIALocations(X509Certificate userCertificate)
            throws CertificateVerificationException {
        List<String> locations;
        //List the AIA locations from the certificate. Those are the URL's of CA s.
        try {
            locations = OCSPVerifier.getAIALocations(userCertificate);
        } catch (CertificateVerificationException e) {
            throw new CertificateVerificationException("Failed to find AIA locations in the cetificate", e);
        }
        return locations;
    }

    /**
     * Get OCSP response from cache.
     *
     * @param locations       CA locations
     * @param request         OCSP request
     * @param userCertificate User's certificate
     * @param ocspCache       cache to store ocsp responses
     * @return Ocsp response
     * @throws CertificateVerificationException If an error occurs while getting the response.
     */
    public static OCSPResp getOCSPResponse(List<String> locations, OCSPReq request, X509Certificate userCertificate,
            OCSPCache ocspCache) throws CertificateVerificationException {
        SingleResp[] responses;
        BasicOCSPResp basicResponse;
        CertificateStatus certificateStatus;
        for (String serviceUrl : locations) {
            OCSPResp response;
            try {
                response = OCSPVerifier.getOCSPResponce(serviceUrl, request);
                if (OCSPResponseStatus.SUCCESSFUL != response.getStatus()) {
                    continue; // Server didn't give the correct response.
                }
                basicResponse = (BasicOCSPResp) response.getResponseObject();
                responses = (basicResponse == null) ? null : basicResponse.getResponses();
            } catch (OCSPException | CertificateVerificationException e) {
                LOG.debug("OCSP response failed for url{}. Hence trying the next url", serviceUrl);
                continue;
            }
            if (responses != null && responses.length == 1) {
                SingleResp singleResponse = responses[0];
                certificateStatus = singleResponse.getCertStatus();
                if (certificateStatus != null) {
                    throw new IllegalStateException("certificate-status=" + certificateStatus);
                }
                //User certificates serial number and response coming from CA needs to be same.
                if (!userCertificate.getSerialNumber().equals(singleResponse.getCertID().getSerialNumber())) {
                    throw new IllegalStateException(
                            "Bad Serials=" + userCertificate.getSerialNumber() + " vs. " + singleResponse.getCertID()
                                    .getSerialNumber());
                }                //If the response state is successful we cache the response.
                ocspCache.setCacheValue(response, userCertificate.getSerialNumber(), singleResponse, request,
                        serviceUrl);
                return response;
            }
        }
        throw new CertificateVerificationException("Could not get revocation status from OCSP.");
    }

    public static List<X509Certificate> getCertInfo(SSLConfig sslConfig) throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        try (FileInputStream certInputStream = new FileInputStream(sslConfig.getServerCertificates())) {
            while (certInputStream.available() > 1) {
                Certificate cert = certificateFactory.generateCertificate(certInputStream);
                certList.add((X509Certificate) cert);
            }
            return certList;
        }
    }
}

