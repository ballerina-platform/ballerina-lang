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
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationStatus;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl.CRLCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl.CRLVerifier;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class CRLVerifierTest {

    /**
     * To test revoked certificate CRLVerifier behaviour, a fake certificate will be created, signed
     * by a fake root certificate. CrlDistributionPoint extension will be extracted from
     * the real peer certificate in resources directory and copied to the fake certificate as a certificate extension.
     * So the criDistributionPointURL in the fake certificate will be the same as in the real certificate.
     * The created X509CRL object having criDistributionPointURL CRLCache. Since the crl is in the
     * cache, there will NOT be a remote call to the CRL server at criDistributionPointURL.
     *
     * @throws Exception
     */
    @Test
    public void testRevokedCertificate() throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        Utils utils = new Utils();
        X509Certificate realPeerCertificate = utils.getRealPeerCertificate();
        String crlDistributionPointUrl = getCRLDistributionPointUrl(realPeerCertificate);

        KeyPair caKeyPair = utils.generateRSAKeyPair();
        X509Certificate fakeCACert = utils.generateFakeRootCert(caKeyPair);

        KeyPair peerKeyPair = utils.generateRSAKeyPair();
        BigInteger revokedSerialNumber = BigInteger.valueOf(111);

        X509Certificate fakeRevokedCertificate = utils
                .getFakeCertificateForCRLTest(fakeCACert, peerKeyPair, revokedSerialNumber, realPeerCertificate);

        X509CRL x509CRL = createCRL(fakeRevokedCertificate, caKeyPair.getPrivate(), revokedSerialNumber);

        CRLCache cache = CRLCache.getCache();
        cache.init(5, 5);
        cache.setCacheValue(crlDistributionPointUrl, x509CRL);

        CRLVerifier crlVerifier = new CRLVerifier(cache);
        RevocationStatus status = crlVerifier.checkRevocationStatus(fakeRevokedCertificate, null);

        //the fake crl we created will be checked if the fake certificate is revoked. So the status should be REVOKED.
        assertTrue(status == RevocationStatus.REVOKED);
    }

    /**
     * This method is used to extract CRL distribution points from real peer certificate.
     *
     * @param certificate is a certificate with a proper CRLDistributionPoints extension.
     * @return the extracted cRLDistributionPointUrl.
     * @throws Exception
     */
    private String getCRLDistributionPointUrl(X509Certificate certificate) throws Exception {

        CRLVerifier crlVerifier = new CRLVerifier(null);
        // use reflection since getCrlDistributionPoints() is private.
        Class<? extends CRLVerifier> crlVerifierClass = crlVerifier.getClass();
        Method getCrlDistributionPoints = crlVerifierClass
                .getDeclaredMethod("getCrlDistributionPoints", X509Certificate.class);
        getCrlDistributionPoints.setAccessible(true);

        //getCrlDistributionPoints(..) returns a list of urls. Get the first one.
        List<String> distPoints = (List<String>) getCrlDistributionPoints.invoke(crlVerifier, certificate);
        return distPoints.get(0);
    }

    /**
     * Creates a fake CRL for the fake CA. The fake certificate with the given revokedSerialNumber will be marked
     * as Revoked in the returned CRL.
     *
     * @param caCert              Fake CA certificate.
     * @param caPrivateKey        Private key of the fake CA.
     * @param revokedSerialNumber Serial number of the fake peer certificate made to be marked as revoked.
     * @return Created fake CRL
     * @throws Exception
     */
    private static X509CRL createCRL(X509Certificate caCert, PrivateKey caPrivateKey, BigInteger revokedSerialNumber)
            throws Exception {

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        Date now = new Date();
        X500Name issuer = X500Name.getInstance(PrincipalUtil.getIssuerX509Principal(caCert).getEncoded());
        X509v2CRLBuilder builder = new X509v2CRLBuilder(issuer, new Date());
        builder.addCRLEntry(revokedSerialNumber, new Date(), 9);
        builder.setNextUpdate(new Date(now.getTime() + TestConstants.NEXT_UPDATE_PERIOD));
        builder.addExtension(new ASN1ObjectIdentifier(TestConstants.CRL_DISTRIBUTION_POINT_EXTENSION), false,
                extUtils.createAuthorityKeyIdentifier(caCert));
        builder.addExtension(new ASN1ObjectIdentifier("2.5.29.20"), false, new CRLNumber(BigInteger.valueOf(1)));
        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");

        contentSignerBuilder.setProvider(Constants.BOUNCY_CASTLE_PROVIDER);
        X509CRLHolder cRLHolder = builder.build(contentSignerBuilder.build(caPrivateKey));
        JcaX509CRLConverter converter = new JcaX509CRLConverter();
        converter.setProvider(Constants.BOUNCY_CASTLE_PROVIDER);
        return converter.getCRL(cRLHolder);
    }
}

