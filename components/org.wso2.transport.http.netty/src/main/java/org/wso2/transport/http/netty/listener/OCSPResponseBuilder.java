package org.wso2.transport.http.netty.listener;

import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.wso2.transport.http.netty.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

/**
 * A class for generating OCSP response.
 */
public class OCSPResponseBuilder {

    public static OCSPResp getOcspResponse(SSLConfig sslConfig, int cacheAllcatedSize, int cacheDelay)
            throws IOException, KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException,
            CertificateVerificationException {

        Certificate[] certificateChain;
        X509Certificate userCertificate = null;
        X509Certificate issuer = null;
        OCSPResp response = null;
        BasicOCSPResp basicResponse;
        SingleResp singleResponse = null;
        CertificateStatus certificateStatus = null;
        int cacheSize = Constants.CACHE_DEFAULT_ALLOCATED_SIZE;
        int cacheDelayMins = Constants.CACHE_DEFAULT_DELAY_MINS;

        if (cacheAllcatedSize != 0 && cacheAllcatedSize > Constants.CACHE_MIN_ALLOCATED_SIZE
                && cacheAllcatedSize < Constants.CACHE_MAX_ALLOCATED_SIZE) {
            cacheSize = cacheAllcatedSize;
        }
        if (cacheDelay != 0 && cacheDelay > Constants.CACHE_MIN_DELAY_MINS
                && cacheDelay < Constants.CACHE_MAX_DELAY_MINS) {
            cacheDelayMins = cacheDelay;
        }

        OCSPCache ocspCache = OCSPCache.getCache();
        ocspCache.init(cacheSize, cacheDelayMins);

        KeyStore keyStore = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass(),
                sslConfig.getTLSStoreType());

        Enumeration<String> aliases = keyStore.aliases();
        String alias = "";
        boolean isAliasWithPrivateKey = false;
        while (aliases.hasMoreElements()) {
            alias = (String) aliases.nextElement();
            // if alias refers to a private key break at that point
            // as we want to use that certificate.
            if (isAliasWithPrivateKey = keyStore.isKeyEntry(alias)) {
                break;
            }
        }

        if (isAliasWithPrivateKey) {
            // Load certificate chain
            certificateChain = keyStore.getCertificateChain(alias);
            userCertificate = (X509Certificate) certificateChain[0];
            issuer = (X509Certificate) certificateChain[certificateChain.length - 1];
        }
        if (userCertificate != null) {
            if (ocspCache.getOCSPCacheValue(userCertificate.getSerialNumber()) != null) {
                response = ocspCache.getOCSPCacheValue(userCertificate.getSerialNumber());
            } else {
                OCSPReq request = null;
                try {
                    request = OCSPVerifier.generateOCSPRequest(issuer, userCertificate.getSerialNumber());
                } catch (CertificateVerificationException e) {
                    throw new CertificateVerificationException("Unable to generate OCSP request", e);
                }

                List<String> locations = null;
                try {
                    locations = OCSPVerifier.getAIALocations(userCertificate);
                } catch (CertificateVerificationException e) {
                    throw new CertificateVerificationException("Unable to find AIA locations in the cetificate", e);
                }
                SingleResp[] responses = null;
                for (String serviceUrl : locations) {
                    try {
                        response = OCSPVerifier.getOCSPResponce(serviceUrl, request);
                        if (OCSPResponseStatus.SUCCESSFUL != response.getStatus()) {
                            continue; // Server didn't give the correct response.
                        }
                        basicResponse = (BasicOCSPResp) response.getResponseObject();
                        responses = (basicResponse == null) ? null : basicResponse.getResponses();
                    } catch (Exception e) {
                        continue;
                    }
                    if (responses != null && responses.length == 1) {
                        singleResponse = responses[0];
                        certificateStatus = singleResponse.getCertStatus();
                        if (certificateStatus != null) {
                            throw new IllegalStateException("certificate-status=" + certificateStatus);
                        }
                        if (!userCertificate.getSerialNumber().equals(singleResponse.getCertID().getSerialNumber())) {
                            throw new IllegalStateException(
                                    "Bad Serials=" + userCertificate.getSerialNumber() + " vs. " + singleResponse
                                            .getCertID().getSerialNumber());
                        }
                        ocspCache.setCacheValue(response, userCertificate.getSerialNumber(), singleResponse, request,
                                serviceUrl);
                    }
                }
                if (responses == null) {
                    throw new CertificateVerificationException("Unable to get OCSP responses from CA");
                }
            }
        }
        return response;
    }

    public static KeyStore getKeyStore(File keyStore, String keyStorePassword, String tlsStoreType)
            throws IOException {
        KeyStore ks = null;
        if (keyStore != null && keyStorePassword != null) {
            try (InputStream is = new FileInputStream(keyStore)) {
                ks = KeyStore.getInstance(tlsStoreType);
                ks.load(is, keyStorePassword.toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e);
            }
        }
        return ks;
    }
}

