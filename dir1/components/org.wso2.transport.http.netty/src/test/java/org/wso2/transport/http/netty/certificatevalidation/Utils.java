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
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
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
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants.BOUNCY_CASTLE_PROVIDER;

/**
 * Contains utility methods used by the test classes.
 */
class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static HttpClientConnector httpClientConnector;
    private static ServerConnector serverConnector;
    private static HttpWsConnectorFactory factory;

    X509Certificate generateFakeRootCert(KeyPair pair) throws Exception {
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

        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    KeyPair generateRSAKeyPair() throws Exception {

        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", Constants.BOUNCY_CASTLE_PROVIDER);
        kpGen.initialize(1024, new SecureRandom());
        return kpGen.generateKeyPair();
    }

    X509Certificate getFakeCertificateForCRLTest(X509Certificate caCert, KeyPair keyPair, BigInteger serialNumber,
            X509Certificate realPeerCertificate)
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
        builder.copyAndAddExtension(new ASN1ObjectIdentifier(TestConstants.CRL_DISTRIBUTION_POINT_EXTENSION), true,
                new JcaX509CertificateHolder(realPeerCertificate));

        X509CertificateHolder holder = builder.build(contentSigner);

        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    X509Certificate generateFakeCertificate(X509Certificate caCert, PublicKey peerPublicKey, BigInteger serialNumber,
            KeyPair caKeyPair)
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

        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    /**
     * Generate X509Certificate object from the peer certificate file in resources directory.
     *
     * @return the created certificate object.
     * @throws Exception if an error occurs while getting the peer certificate.
     */
    X509Certificate getRealPeerCertificate() throws Exception {
        return createCertificateFromResourceFile(TestConstants.INTERMEDIATE_CERT);
    }

    /**
     * Create a certificate chain from the certificates in the resources directory.
     *
     * @return created array of certificates.
     * @throws Exception if an error occurs while getting the certificate chain.
     */
    X509Certificate[] getRealCertificateChain() throws Exception {

        X509Certificate intermediateCert = createCertificateFromResourceFile(TestConstants.INTERMEDIATE_CERT);
        X509Certificate rootCert = createCertificateFromResourceFile(TestConstants.ROOT_CERT);

        return new X509Certificate[] { intermediateCert, rootCert };
    }

    /**
     * Generates a fake certificate chain. The array will contain two certificates, the root and the peer.
     *
     * @return the created array of certificates.
     * @throws Exception if an error occurs while getting the certificate chain.
     */
    X509Certificate[] getFakeCertificateChain() throws Exception {

        KeyPair rootKeyPair = generateRSAKeyPair();
        X509Certificate rootCert = generateFakeRootCert(rootKeyPair);
        KeyPair entityKeyPair = generateRSAKeyPair();
        BigInteger entitySerialNum = BigInteger.valueOf(111);
        X509Certificate entityCert = generateFakeCertificate(rootCert, entityKeyPair.getPublic(),
                entitySerialNum, rootKeyPair);
        return new X509Certificate[] { entityCert, rootCert };
    }

    private X509Certificate createCertificateFromResourceFile(String resourcePath) throws Exception {

        CertificateFactory certFactory = CertificateFactory
                .getInstance(Constants.X_509, Constants.BOUNCY_CASTLE_PROVIDER);
        File faceBookCertificateFile = new File(this.getClass().getResource(resourcePath).toURI());
        InputStream in = new FileInputStream(faceBookCertificateFile);

        return (X509Certificate) certFactory.generateCertificate(in);
    }

    private static void createMockOCSPResponse() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Utils utils = new Utils();
        KeyPair caKeyPair = utils.generateRSAKeyPair();

        X509Certificate caCert = utils.generateFakeRootCert(caKeyPair);
        BigInteger serialNumber = BigInteger.valueOf(01);

        OCSPReq request = getOCSPRequest(caCert, serialNumber);
        byte[] issuerCertEnc = caCert.getEncoded();
        X509CertificateHolder certificateHolder = new X509CertificateHolder(issuerCertEnc);

        OCSPResp response = generateOCSPResponse(request, certificateHolder, caKeyPair.getPrivate());
        SingleResp singleResp = ((BasicOCSPResp) response.getResponseObject()).getResponses()[0];
        cacheOcspResponse(response, serialNumber, singleResp, request);
    }

    private static void cacheOcspResponse(OCSPResp response, BigInteger serialNumber, SingleResp singleResp,
            OCSPReq request) {
        OCSPCache cache = OCSPCache.getCache();
        int cacheSize = 51;
        int cacheDelay = 15;
        cache.init(cacheSize, cacheDelay);
        cache.setCacheValue(response, serialNumber, singleResp, request, null);
    }

    /**
     * This method creates the OCSP response for the OCSP request. In here we are providing a non revoked certificate.
     * The OCSP response will say that the certificate is GOOD.
     *
     * @param request OCSP request which asks if the certificate is revoked.
     * @param caPrivateKey PrivateKey of the fake CA.
     * @return Created OCSP response by the fake CA.
     * @throws OCSPException If an error occurs when generating ocsp response.
     * @throws OperatorCreationException If an error occurs when creating bouncy castle operator.
     */
    private static OCSPResp generateOCSPResponse(OCSPReq request, X509CertificateHolder certificateHolder,
            PrivateKey caPrivateKey) throws OCSPException, OperatorCreationException {

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
            Date nextUpdate = new Date(new Date().getTime() + TestConstants.NEXT_UPDATE_PERIOD);
            Date thisUpdate = new Date(new Date().getTime());
            basicOCSPRespBuilder.addResponse(certID, CertificateStatus.GOOD, thisUpdate, nextUpdate);
        }

        X509CertificateHolder[] chain = { certificateHolder };
        ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider(BOUNCY_CASTLE_PROVIDER)
                .build(caPrivateKey);
        BasicOCSPResp basicResp = basicOCSPRespBuilder.build(signer, chain, new Date());
        OCSPRespBuilder builder = new OCSPRespBuilder();

        return builder.build(OCSPRespBuilder.SUCCESSFUL, basicResp);
    }

    /**
     * An OCSP request is created to be given to the fake CA. The certificate serial numbers are similar to the
     * ballerina client's serial number.
     *
     * @param caCert Fake CA certificate.
     * @param serialNumber Serial number of the certificate which needs to be checked if revoked.
     * @return Created OCSP request.
     * @throws Exception If any error occurs.
     */
    static OCSPReq getOCSPRequest(X509Certificate caCert, BigInteger serialNumber) throws Exception {
        OCSPVerifier ocspVerifier = new OCSPVerifier(null);
        Class ocspVerifierClass = ocspVerifier.getClass();
        Method generateOCSPRequest = ocspVerifierClass
                .getDeclaredMethod("generateOCSPRequest", X509Certificate.class, BigInteger.class);
        generateOCSPRequest.setAccessible(true);
        return (OCSPReq) generateOCSPRequest.invoke(ocspVerifier, caCert, serialNumber);
    }

    private static SenderConfiguration getSenderConfigs(String type) {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        String trustStoreFilePath = "/simple-test-config/cacerts.p12";
        List<Parameter> clientParams = new ArrayList<>(1);
        Parameter paramClientCiphers = new Parameter("ciphers", "TLS_RSA_WITH_AES_128_CBC_SHA");
        clientParams.add(paramClientCiphers);
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(trustStoreFilePath));
        String trustStorePassword = "cacertspassword";
        senderConfiguration.setTrustStorePass(trustStorePassword);
        senderConfiguration.setParameters(clientParams);
        senderConfiguration.setTLSStoreType("PKCS12");
        senderConfiguration.setHostNameVerificationEnabled(false);
        if (type.equals("ocspStapling")) {
            senderConfiguration.setOcspStaplingEnabled(true);
        }
        senderConfiguration.setValidateCertEnabled(true);
        senderConfiguration.setScheme(HTTPS_SCHEME);
        return senderConfiguration;
    }

    private static ListenerConfiguration setListenerConfiguration(String keyStore, String keyStorePassword,
            String type) {
        List<Parameter> serverParams = new ArrayList<>(1);
        Parameter paramServerCiphers = new Parameter("ciphers", "TLS_RSA_WITH_AES_128_CBC_SHA");
        serverParams.add(paramServerCiphers);
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setParameters(serverParams);
        listenerConfiguration.setPort(TestUtil.SERVER_PORT3);
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(keyStore));
        listenerConfiguration.setKeyStorePass(keyStorePassword);
        listenerConfiguration.setScheme(HTTPS_SCHEME);
        listenerConfiguration.setTLSStoreType("PKCS12");
        if (type.equals("ocspStapling")) {
            listenerConfiguration.setOcspStaplingEnabled(true);
        }
        return listenerConfiguration;
    }

    static void testResponse() {
        try {
            String testValue = "Test";
            HttpCarbonMessage msg = TestUtil.createHttpsPostReq(TestUtil.SERVER_PORT3, testValue, "");

            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(30, TimeUnit.SECONDS);

            HttpCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(testValue, result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running the test", e);
        }
    }

    static void setUp(String type) throws Exception {
        createMockOCSPResponse();
        factory = new DefaultHttpWsConnectorFactory();

        String keyStoreFilePath = "/simple-test-config/localcrt.p12";
        String keyStorePassword = "localpwd";
        serverConnector = factory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                Utils.setListenerConfiguration(keyStoreFilePath, keyStorePassword, type));
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory.createHttpClientConnector(new HashMap<>(), Utils.getSenderConfigs(type));
    }

    static void cleanUp() {
        httpClientConnector.close();
        serverConnector.stop();
        try {
            factory.shutdown();
        } catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for HttpWsFactory to shutdown", e);
        }
    }
}

