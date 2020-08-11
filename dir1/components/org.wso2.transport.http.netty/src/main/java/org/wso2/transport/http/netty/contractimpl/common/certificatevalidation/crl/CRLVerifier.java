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

package org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationStatus;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is used to verify a certificate is revoked or not by using the Certificate Revocation List published
 * by the CA.
 */
public class CRLVerifier implements RevocationVerifier {

    private CRLCache cache;
    private static final Logger LOG = LoggerFactory.getLogger(CRLVerifier.class);

    public CRLVerifier(CRLCache cache) {
        this.cache = cache;
    }

    /**
     * Checks revocation status (Good, Revoked, unknown) of the peer certificate.
     *
     * @param peerCert   peer certificate
     * @param issuerCert issuer certificate of the peer.
     * @return revocation status of the peer certificate.
     * @throws CertificateVerificationException Occurs when it fails to verify the status of certificate.
     */
    public RevocationStatus checkRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateVerificationException {

        List<String> list = getCrlDistributionPoints(peerCert);
        //check with distributions points in the list one by one. if one fails move to the other.
        for (String crlUrl : list) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Trying to get CRL for URL: {}", crlUrl);
            }

            if (cache != null) {
                X509CRL x509CRL = cache.getCacheValue(crlUrl);
                if (x509CRL != null) {
                    RevocationStatus status = getRevocationStatus(x509CRL, peerCert);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("CRL taken from cache.");
                    }
                    return status;
                }
            }
            try {
                X509CRL x509CRL = downloadCRLFromWeb(crlUrl);
                if (x509CRL != null) {
                    if (cache != null) {
                        cache.setCacheValue(crlUrl, x509CRL);
                    }
                    return getRevocationStatus(x509CRL, peerCert);
                }
            } catch (IOException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Either URL is bad or can't build X509CRL. So check with the next url in the list.", e);
                }
            }
        }
        throw new CertificateVerificationException("Cannot check revocation status with the certificate");
    }

    private RevocationStatus getRevocationStatus(X509CRL x509CRL, X509Certificate peerCert) {
        if (x509CRL.isRevoked(peerCert)) {
            return RevocationStatus.REVOKED;
        } else {
            return RevocationStatus.GOOD;
        }
    }

    /**
     * Downloads CRL from the crlUrl. Does not support HTTPS.
     *
     * @param crlURL URL of the CRL distribution point.
     * @return Downloaded CRL.
     * @throws IOException If an error occurs while downloading the CRL from web.
     * @throws CertificateVerificationException If an error occurs in CRL download process.
     */
    protected X509CRL downloadCRLFromWeb(String crlURL) throws IOException, CertificateVerificationException {
        URL url = new URL(crlURL);
        try (InputStream crlStream = url.openStream()) {
            CertificateFactory cf = CertificateFactory.getInstance(Constants.X_509);
            return (X509CRL) cf.generateCRL(crlStream);
        } catch (MalformedURLException e) {
            throw new CertificateVerificationException("CRL URL is malformed", e);
        } catch (IOException e) {
            throw new CertificateVerificationException("Cant reach URI: " + crlURL + " - only support HTTP", e);
        } catch (CertificateException e) {
            throw new CertificateVerificationException(e);
        } catch (CRLException e) {
            throw new CertificateVerificationException("Cannot generate X509CRL from the stream data", e);
        }
    }

    /**
     * Extracts all CRL distribution point URLs from the "CRL Distribution Point"
     * extension in a X.509 certificate. If CRL distribution point extension is
     * unavailable, returns an empty list.
     */
    private List<String> getCrlDistributionPoints(X509Certificate cert) throws CertificateVerificationException {

        //Gets the DER-encoded OCTET string for the extension value for CRLDistributionPoints.
        byte[] crlDPExtensionValue = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crlDPExtensionValue == null) {
            throw new CertificateVerificationException("Certificate doesn't have CRL distribution points");
        }
        //crlDPExtensionValue is encoded in ASN.1 format.
        ASN1InputStream asn1In = new ASN1InputStream(crlDPExtensionValue);
        //DER (Distinguished Encoding Rules) is one of ASN.1 encoding rules defined in ITU-T X.690, 2002, specification.
        //ASN.1 encoding rules can be used to encode any data object into a binary file. Read the object in octets.
        CRLDistPoint distPoint;
        try {
            DEROctetString crlDEROctetString = (DEROctetString) asn1In.readObject();
            //Get Input stream in octets.
            distPoint = getOctetInputStream(crlDEROctetString);
        } catch (IOException e) {
            throw new CertificateVerificationException("Cannot read certificate to get CRL URLs", e);
        } finally {
            try {
                asn1In.close();
            } catch (IOException e) {
                LOG.error("Cannot close input stream", e);
            }
        }

        List<String> crlUrls = new ArrayList<>();
        //Loop through ASN1Encodable DistributionPoints.
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            //get ASN1Encodable DistributionPointName.
            DistributionPointName dpn = dp.getDistributionPoint();
            if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                //Create ASN1Encodable General Names.
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                // Look for a URI
                for (GeneralName genName : genNames) {
                    if (genName.getTagNo() == GeneralName.uniformResourceIdentifier) {
                        //DERIA5String contains an ascii string.
                        //A IA5String is a restricted character string type in the ASN.1 notation.
                        String url = DERIA5String.getInstance(genName.getName()).getString().trim();
                        crlUrls.add(url);
                    }
                }
            }
        }

        if (crlUrls.isEmpty()) {
            throw new CertificateVerificationException("Cant get CRL urls from certificate");
        }
        return crlUrls;
    }

    private CRLDistPoint getOctetInputStream(DEROctetString crlDEROctetString) throws CertificateVerificationException {
        try (ASN1InputStream asn1InOctets = new ASN1InputStream(crlDEROctetString.getOctets())) {
            ASN1Primitive crlDERObject = asn1InOctets.readObject();
            return CRLDistPoint.getInstance(crlDERObject);

        } catch (IOException e) {
            throw new CertificateVerificationException("Cannot read certificate to get CRL URLs", e);
        }
    }
}
