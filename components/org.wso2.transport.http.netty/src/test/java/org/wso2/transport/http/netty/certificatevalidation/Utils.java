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
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Contains utility methods used by the test classes.
 */
public class Utils {

    public X509Certificate generateFakeRootCert(KeyPair pair) throws Exception {
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded());
        X500Name subjectDN = new X500Name("CN=Test CA Certificate");
        BigInteger serialNumber = BigInteger.valueOf(1);
        Date validityStartDate = new Date(System.currentTimeMillis());
        Date validityEndDate = new Date(System.currentTimeMillis() + TestConstants.VALIDITY_PERIOD);
        X509v1CertificateBuilder builder = new X509v1CertificateBuilder(subjectDN, serialNumber, validityStartDate,
                validityEndDate, subjectDN, subPubKeyInfo);

        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1WithRSAEncryption");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(PrivateKeyFactory.createKey(pair.getPrivate().getEncoded()));

        X509CertificateHolder holder = builder.build(contentSigner);

        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);
        return cert;
    }

    public KeyPair generateRSAKeyPair() throws Exception {

        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
        kpGen.initialize(1024, new SecureRandom());
        return kpGen.generateKeyPair();
    }

    public X509Certificate getFakeCertificateForCRLTest(X509Certificate caCert, KeyPair keyPair,
            BigInteger serialNumber, X509Certificate realPeerCertificate)
            throws IOException, OperatorCreationException, CertificateException {

        X500Name subjectDN = new X500Name("CN=Test End Certificate");
        Date validityStartDate = new Date(System.currentTimeMillis());
        Date validityEndDate = new Date(System.currentTimeMillis() + TestConstants.VALIDITY_PERIOD);
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(subjectDN, serialNumber, validityStartDate,
                validityEndDate, subjectDN, subPubKeyInfo);
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1WithRSAEncryption");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded()));
        builder.copyAndAddExtension(new ASN1ObjectIdentifier("2.5.29.31"), true,
                new JcaX509CertificateHolder(realPeerCertificate));

        X509CertificateHolder holder = builder.build(contentSigner);

        X509Certificate fakecert = new JcaX509CertificateConverter().getCertificate(holder);

        return fakecert;
    }

    public X509Certificate generateFakeCertificate(X509Certificate caCert, PublicKey peerPublicKey,
            BigInteger serialNumber, KeyPair caKeyPair)
            throws IOException, OperatorCreationException, CertificateException {

        X500Name subjectDN = new X500Name("CN=Test End Certificate");
        Date validityStartDate = new Date(System.currentTimeMillis());
        Date validityEndDate = new Date(System.currentTimeMillis() + TestConstants.VALIDITY_PERIOD);
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(peerPublicKey.getEncoded());
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(subjectDN, serialNumber, validityStartDate,
                validityEndDate, subjectDN, subPubKeyInfo);
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1WithRSAEncryption");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(PrivateKeyFactory.createKey(caKeyPair.getPrivate().getEncoded()));

        X509CertificateHolder holder = builder.build(contentSigner);

        X509Certificate fakecert = new JcaX509CertificateConverter().getCertificate(holder);

        return fakecert;
    }

    /**
     * Generate X509Certificate object from the peer certificate file in resources directory.
     *
     * @return the created certificate object.
     * @throws Exception
     */
    public X509Certificate getRealPeerCertificate() throws Exception {
        return createCertificateFromResourceFile(TestConstants.INTERMEDIATE_CERT);
    }

    /**
     * Create a certificate chain from the certificates in the resources directory.
     *
     * @return created array of certificates.
     * @throws Exception
     */
    public X509Certificate[] getRealCertificateChain() throws Exception {

        X509Certificate intermediateCert = createCertificateFromResourceFile(TestConstants.INTERMEDIATE_CERT);
        X509Certificate rootCert = createCertificateFromResourceFile(TestConstants.ROOT_CERT);

        return new X509Certificate[] { intermediateCert, rootCert };
    }

    /**
     * Generates a fake certificate chain. The array will contain two certificates, the root and the peer.
     *
     * @return the created array of certificates.
     * @throws Exception
     */
    public X509Certificate[] getFakeCertificateChain() throws Exception {

        KeyPair rootKeyPair = generateRSAKeyPair();
        X509Certificate rootCert = generateFakeRootCert(rootKeyPair);
        KeyPair entityKeyPair = generateRSAKeyPair();
        BigInteger entitySerialNum = BigInteger.valueOf(111);
        X509Certificate entityCert = generateFakeCertificate(rootCert, entityKeyPair.getPublic(),
                entitySerialNum, rootKeyPair);
        return new X509Certificate[] { entityCert, rootCert };
    }

    private X509Certificate createCertificateFromResourceFile(String resourcePath) throws Exception {

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
        File faceBookCertificateFile = new File(this.getClass().getResource(resourcePath).toURI());
        InputStream in = new FileInputStream(faceBookCertificateFile);
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(in);
        return certificate;
    }
}
