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

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.Req;
import org.bouncycastle.cert.ocsp.RespID;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.common.certificatevalidation.RevocationStatus;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPVerifier;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.testng.Assert.assertTrue;

public class OCSPVerifierTest {

    /**
     * A fake certificate signed by a fake CA is made as the revoked certificate. The created OCSP response to the
     * OCSP request will say that that the fake peer certificate is revoked. the SingleResp derived from the OCSP
     * response will be put into the cache against the serial number of the fake peer certificate. Since the SingleResp
     * which corresponds to the revokedSerialNumber is in the cache, there will NOT be a call to a remote OCSP server.
     * Note that the serviceUrl passed to cache.setCacheValue(..) is null since it is not needed.
     *
     * @throws Exception
     */
    @Test
    public void testOCSPVerifier() throws Exception {

        //Add BouncyCastle as Security Provider.
        Security.addProvider(new BouncyCastleProvider());

        Utils utils = new Utils();
        KeyPair caKeyPair = utils.generateRSAKeyPair();
        X509Certificate caCert = utils.generateFakeRootCert(caKeyPair);

        KeyPair peerKeyPair = utils.generateRSAKeyPair();
        BigInteger revokedSerialNumber = BigInteger.valueOf(111);

        X509Certificate revokedCertificate = utils
                .generateFakeCertificate(caCert, peerKeyPair.getPublic(), revokedSerialNumber, caKeyPair);

        OCSPReq request = getOCSPRequest(caCert, revokedSerialNumber);

        byte[] issuerCertEnc = caCert.getEncoded();
        X509CertificateHolder certificateHolder = new X509CertificateHolder(issuerCertEnc);
        DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder()
                .setProvider(Constants.BOUNCY_CASTLE_PROVIDER).build();

        // CertID structure is used to uniquely identify certificates that are the subject of
        // an OCSP request or response and has an ASN.1 definition. CertID structure is defined in RFC 2560.
        CertificateID revokedID = new CertificateID(digCalcProv.get(CertificateID.HASH_SHA1), certificateHolder,
                revokedSerialNumber);

        OCSPResp response = generateOCSPResponse(request, certificateHolder, caKeyPair.getPrivate(), revokedID);
        SingleResp singleResp = ((BasicOCSPResp) response.getResponseObject()).getResponses()[0];

        OCSPCache cache = OCSPCache.getCache();
        cache.init(5, 5);
        cache.setCacheValue(response, revokedSerialNumber, singleResp, request, null);

        OCSPVerifier ocspVerifier = new OCSPVerifier(cache);
        RevocationStatus status = ocspVerifier.checkRevocationStatus(revokedCertificate, caCert);

        //the cache will have the SingleResponse derived from the create OCSP response and it will be checked to see
        //if the fake certificate is revoked. So the status should be REVOKED.
        assertTrue(status == RevocationStatus.REVOKED);
    }

    /**
     * An OCSP request is made to be given to the fake CA.
     *
     * @param caCert Fake CA certificate.
     * @param revokedSerialNumber Serial number of the certificate which needs to be checked if revoked.
     * @return Created OCSP request.
     * @throws Exception
     */
    private OCSPReq getOCSPRequest(X509Certificate caCert, BigInteger revokedSerialNumber) throws Exception {
        OCSPVerifier ocspVerifier = new OCSPVerifier(null);
        Class ocspVerifierClass = ocspVerifier.getClass();
        Method generateOCSPRequest = ocspVerifierClass
                .getDeclaredMethod("generateOCSPRequest", X509Certificate.class, BigInteger.class);
        generateOCSPRequest.setAccessible(true);
        return (OCSPReq) generateOCSPRequest.invoke(ocspVerifier, caCert, revokedSerialNumber);
    }

    /**
     * This makes the corresponding OCSP response to the OCSP request which is sent to the fake CA. If the request
     * has a certificateID which is marked as revoked by the CA, the OCSP response will say that the certificate
     * which is referred by the request, is revoked.
     *
     * @param request OCSP request which asks if the certificate is revoked.
     * @param caPrivateKey PrivateKey of the fake CA.
     * @param revokedID ID in fake CA which is checked against the certificateId in the request.
     * @return Created OCSP response by the fake CA.
     * @throws NoSuchProviderException
     * @throws OCSPException
     * @throws OperatorCreationException
     */
    public OCSPResp generateOCSPResponse(OCSPReq request, X509CertificateHolder certificateHolder,
            PrivateKey caPrivateKey, CertificateID revokedID)
            throws NoSuchProviderException, OCSPException, OperatorCreationException {

        BasicOCSPRespBuilder basicOCSPRespBuilder = new BasicOCSPRespBuilder(
                new RespID(certificateHolder.getSubject()));
        Extension extension = request
                .getExtension(new ASN1ObjectIdentifier(OCSPObjectIdentifiers.id_pkix_ocsp.getId()));

        if (extension != null) {
            basicOCSPRespBuilder.setResponseExtensions(new Extensions(extension));
        }

        Req[] requests = request.getRequestList();

        for (Req req : requests) {
            CertificateID certID = req.getCertID();

            if (certID.equals(revokedID)) {
                RevokedStatus revokedStatus = new RevokedStatus(new Date(), CRLReason.privilegeWithdrawn);
                Date nextUpdate = new Date(new Date().getTime() + TestConstants.NEXT_UPDATE_PERIOD);
                Date thisUpdate = new Date(new Date().getTime());
                basicOCSPRespBuilder.addResponse(certID, revokedStatus, thisUpdate, nextUpdate);
            } else {
                basicOCSPRespBuilder.addResponse(certID, CertificateStatus.GOOD);
            }
        }

        X509CertificateHolder[] chain = { certificateHolder };
        ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider(Constants.BOUNCY_CASTLE_PROVIDER)
                .build(caPrivateKey);
        BasicOCSPResp basicResp = basicOCSPRespBuilder.build(signer, chain, new Date());
        OCSPRespBuilder builder = new OCSPRespBuilder();

        return builder.build(OCSPRespBuilder.SUCCESSFUL, basicResp);
    }
}

