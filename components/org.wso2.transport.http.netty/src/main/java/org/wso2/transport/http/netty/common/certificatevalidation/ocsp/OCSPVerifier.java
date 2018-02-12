/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.transport.http.netty.common.certificatevalidation.ocsp;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.cert.ocsp.UnknownStatus;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.common.certificatevalidation.RevocationStatus;
import org.wso2.transport.http.netty.common.certificatevalidation.RevocationVerifier;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to check if a Certificate is revoked or not by its CA using Online Certificate Status Protocol (OCSP).
 */
public class OCSPVerifier implements RevocationVerifier {

    private OCSPCache cache;
    private static final Logger log = LoggerFactory.getLogger(OCSPVerifier.class);
    private static final String BC = "BC";

    public OCSPVerifier(OCSPCache cache) {
        this.cache = cache;
    }

    /**
     * Gets the revocation status (Good, Revoked or Unknown) of the given peer certificate.
     *
     * @param peerCert   The certificate we want to check if revoked.
     * @param issuerCert Needed to create OCSP request.
     * @return revocation status of the peer certificate.
     * @throws CertificateVerificationException
     */
    public RevocationStatus checkRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateVerificationException {

        //check cache
        if (cache != null) {
            SingleResp resp = cache.getCacheValue(peerCert.getSerialNumber());
            if (resp != null) {
                //If cant be casted, we have used the wrong cache.
                RevocationStatus status = getRevocationStatus(resp);
                log.info("OCSP response taken from cache....");
                return status;
            }
        }

        OCSPReq request = generateOCSPRequest(issuerCert, peerCert.getSerialNumber());
        //This list will sometimes have non ocsp urls as well.
        List<String> locations = getAIALocations(peerCert);

        for (String serviceUrl : locations) {

            SingleResp[] responses;
            try {
                OCSPResp ocspResponse = getOCSPResponce(serviceUrl, request);
                if (OCSPResponseStatus.SUCCESSFUL != ocspResponse.getStatus()) {
                    continue; // Server didn't give the response right.
                }

                BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResponse.getResponseObject();
                responses = (basicResponse == null) ? null : basicResponse.getResponses();
            } catch (Exception e) {
                continue;
            }

            if (responses != null && responses.length == 1) {
                SingleResp resp = responses[0];
                RevocationStatus status = getRevocationStatus(resp);
                if (cache != null) {
                    cache.setCacheValue(peerCert.getSerialNumber(), resp, request, serviceUrl);
                }
                return status;
            }
        }
        throw new CertificateVerificationException("Cant get Revocation Status from OCSP.");
    }

    private RevocationStatus getRevocationStatus(SingleResp resp) throws CertificateVerificationException {
        Object status = resp.getCertStatus();
        if (status == CertificateStatus.GOOD) {
            return RevocationStatus.GOOD;
        } else if (status instanceof RevokedStatus) {
            return RevocationStatus.REVOKED;
        } else if (status instanceof UnknownStatus) {
            return RevocationStatus.UNKNOWN;
        }
        throw new CertificateVerificationException("Cant recognize Certificate Status");
    }

    /**
     * Gets an ASN.1 encoded OCSP response (as defined in RFC 2560) from the given service URL. Currently supports
     * only HTTP.
     *
     * @param serviceUrl URL of the OCSP endpoint.
     * @param request    an OCSP request object.
     * @return OCSP response encoded in ASN.1 structure.
     * @throws CertificateVerificationException
     */
    protected OCSPResp getOCSPResponce(String serviceUrl, OCSPReq request) throws CertificateVerificationException {

        try {
            byte[] array = request.getEncoded();
            if (serviceUrl.startsWith("http")) {
                HttpURLConnection con;
                URL url = new URL(serviceUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Content-Type", "application/ocsp-request");
                con.setRequestProperty("Accept", "application/ocsp-response");
                con.setDoOutput(true);
                try (OutputStream out = con.getOutputStream();
                        DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));) {

                    dataOut.write(array);

                    dataOut.flush();
                    dataOut.close();

                    //Check errors in response:
                    if (con.getResponseCode() / 100 != 2) {
                        throw new CertificateVerificationException(
                                "Error getting ocsp response." + "Response code is " + con.getResponseCode());
                    }

                    //Get Response
                    InputStream in = (InputStream) con.getContent();
                    return new OCSPResp(in);
                }
            } else {
                throw new CertificateVerificationException("Only http is supported for ocsp calls");
            }
        } catch (IOException e) {
            throw new CertificateVerificationException("Cannot get ocspResponse from url: " + serviceUrl, e);
        }
    }

    /**
     * This method generates an OCSP Request to be sent to an OCSP endpoint.
     *
     * @param issuerCert   is the Certificate of the Issuer of the peer certificate we are interested in.
     * @param serialNumber of the peer certificate.
     * @return generated OCSP request.
     * @throws CertificateVerificationException
     */
    private OCSPReq generateOCSPRequest(X509Certificate issuerCert, BigInteger serialNumber)
            throws CertificateVerificationException {

        //Add provider BC
        Security.addProvider(new BouncyCastleProvider());
        try {

            byte[] issuerCertEnc = issuerCert.getEncoded();
            X509CertificateHolder certificateHolder = new X509CertificateHolder(issuerCertEnc);
            DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder().setProvider(BC).build();

            //  CertID structure is used to uniquely identify certificates that are the subject of
            // an OCSP request or response and has an ASN.1 definition. CertID structure is defined in RFC 2560
            CertificateID id = new CertificateID(digCalcProv.get(CertificateID.HASH_SHA1), certificateHolder,
                    serialNumber);

            // basic request generation with nonce
            OCSPReqBuilder builder = new OCSPReqBuilder();
            builder.addRequest(id);

            // create details for nonce extension. The nonce extension is used to bind
            // a request to a response to prevent replay attacks. As the name implies,
            // the nonce value is something that the client should only use once within a reasonably small period.
            BigInteger nonce = BigInteger.valueOf(System.currentTimeMillis());

            //to create the request Extension
            builder.setRequestExtensions(new Extensions(new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false,
                    new DEROctetString(nonce.toByteArray()))));

            return builder.build();

        } catch (OCSPException | OperatorCreationException | IOException | CertificateEncodingException e) {
            throw new CertificateVerificationException("Cannot generate OSCP Request with the given certificate", e);
        }
    }

    /**
     * Authority Information Access (AIA) is a non-critical extension in an X509 Certificate. This contains the
     * URL of the OCSP endpoint if one is available.
     *
     * @param cert is the certificate
     * @return a lit of URLs in AIA extension of the certificate which will hopefully contain an OCSP endpoint.
     * @throws CertificateVerificationException
     */
    private List<String> getAIALocations(X509Certificate cert) throws CertificateVerificationException {

        //Gets the DER-encoded OCTET string for the extension value for Authority information access Points
        byte[] aiaExtensionValue = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (aiaExtensionValue == null) {
            throw new CertificateVerificationException("Certificate Doesn't have Authority Information Access points");
        }
        AuthorityInformationAccess authorityInformationAccess;
        ASN1InputStream asn1InputStream = null;

        try {
            DEROctetString oct = (DEROctetString) (new ASN1InputStream(new ByteArrayInputStream(aiaExtensionValue))
                    .readObject());
            asn1InputStream = new ASN1InputStream(oct.getOctets());
            authorityInformationAccess = AuthorityInformationAccess.getInstance(asn1InputStream.readObject());
        } catch (IOException e) {
            throw new CertificateVerificationException("Cannot read certificate to get OSCP urls", e);
        } finally {
            try {
                if (asn1InputStream != null) {
                    asn1InputStream.close();
                }
            } catch (IOException e) {
                log.error("Cannot close ASN1InputStream");
            }
        }

        List<String> ocspUrlList = new ArrayList<String>();
        AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
        for (AccessDescription accessDescription : accessDescriptions) {

            GeneralName gn = accessDescription.getAccessLocation();
            if (gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
                DERIA5String str = DERIA5String.getInstance(gn.getName());
                String accessLocation = str.getString();
                ocspUrlList.add(accessLocation);
            }
        }
        if (ocspUrlList.isEmpty()) {
            throw new CertificateVerificationException("Cant get OCSP urls from certificate");
        }
        return ocspUrlList;
    }

}
