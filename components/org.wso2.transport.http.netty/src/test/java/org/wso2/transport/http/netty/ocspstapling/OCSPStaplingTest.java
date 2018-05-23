/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.ocspstapling;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
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
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.certificatevalidation.TestConstants;
import org.wso2.transport.http.netty.certificatevalidation.Utils;
import org.wso2.transport.http.netty.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.common.certificatevalidation.Constants;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPCache;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.listener.OCSPResponseBuilder;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * A test case for testing OCSP stapling.
 */
public class OCSPStaplingTest {

    private static final Logger log = LoggerFactory.getLogger(OCSPStaplingTest.class);
    private String trustStoreFilePath = "/simple-test-config/cacerts.p12";
    private String trustStorePassword = "cacertspassword";
    private HttpClientConnector httpClientConnector;
    private String tlsStoreType = "PKCS12";
    private String httpsScheme = "https";
    private int port = 7777;
    private List<String> ocspUrlList = new ArrayList<String>();
    private ServerConnector serverConnector;
    private HttpWsConnectorFactory factory;

    @BeforeClass
    public void setUp() throws Exception {
        createMockOCSPResponse();
        TransportsConfiguration transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");
        Set<SenderConfiguration> senderConfig = transportsConfiguration.getSenderConfigurations();
        setSenderConfigs(senderConfig);
        factory = new DefaultHttpWsConnectorFactory();

        String keyStoreFilePath = "/simple-test-config/localcrt.p12";
        String keyStorePassword = "localpwd";
        serverConnector = factory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                setListenerConfiguration(keyStoreFilePath, keyStorePassword));
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory
                .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                        HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, httpsScheme));

    }

    private void setSenderConfigs(Set<SenderConfiguration> senderConfig) {
        senderConfig.forEach(config -> {
            if (config.getId().contains(httpsScheme)) {
                config.setTrustStoreFile(TestUtil.getAbsolutePath(trustStoreFilePath));
                config.setTrustStorePass(trustStorePassword);
                config.setTLSStoreType(tlsStoreType);
                config.setHostNameVerificationEnabled(false);
                config.setOcspStaplingEnabled(true);
            }
        });
    }

    @Test (description = "Tests with ocsp stapling enabled client and a server.")
    public void testOcspStapling() {
        try {
            String testValue = "Test";
            HTTPCarbonMessage msg = TestUtil.createHttpsPostReq(port, testValue, "");

            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(10, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(testValue, result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running ocsp stapling test", e);
        }
    }

    @Test (description = "Tests retrieving AIA locations from the certificate inside the keystore.")
    public void testRetrievingAIALocations()
            throws IOException, KeyStoreException, CertificateVerificationException {
        String tlsStoreType = "PKCS12";
        String keyStoreFilePath = "/simple-test-config/localcrt.p12";
        String keyStorePassword = "localpwd";
        KeyStore keyStore = OCSPResponseBuilder
                .getKeyStore(new File(TestUtil.getAbsolutePath(keyStoreFilePath)), keyStorePassword, tlsStoreType);
        Enumeration<String> aliases = keyStore.aliases();
        String alias = "";
        boolean isAliasWithPrivateKey = false;
        while (aliases.hasMoreElements()) {
            alias = (String) aliases.nextElement();
            if (isAliasWithPrivateKey = keyStore.isKeyEntry(alias)) {
                break;
            }
        }
        if (isAliasWithPrivateKey) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            X509Certificate userCertificate = (X509Certificate) certificateChain[0];
            ocspUrlList = OCSPResponseBuilder.getAIALocations(userCertificate);
        }
        String ocspUrl = "http://127.0.0.1:8080";
        assertEquals(ocspUrlList.get(0), ocspUrl, "Failed to list the correct AIA locations");
    }

    private ListenerConfiguration setListenerConfiguration(String keyStore, String keyStorePassword) {
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(port);
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(keyStore));
        listenerConfiguration.setKeyStorePass(keyStorePassword);
        listenerConfiguration.setScheme(httpsScheme);
        listenerConfiguration.setTLSStoreType(tlsStoreType);
        listenerConfiguration.setOcspStaplingEnabled(true);
        return listenerConfiguration;
    }

    /**
     * This method creates the OCSP response for the OCSP request. In here we are providing a non revoked certificate.
     * The OCSP response will say that the certificate is GOOD.
     *
     * @param request OCSP request which asks if the certificate is revoked.
     * @param caPrivateKey PrivateKey of the fake CA.
     * @return Created OCSP response by the fake CA.
     * @throws NoSuchProviderException   If an error occurs when registering bouncy castle as the security provider.
     * @throws OCSPException If an error occurs when generating ocsp response.
     * @throws OperatorCreationException If an error occurs when creating bouncy castle operator.
     */
    private OCSPResp generateOCSPResponse(OCSPReq request, X509CertificateHolder certificateHolder,
            PrivateKey caPrivateKey) throws NoSuchProviderException, OCSPException, OperatorCreationException {

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
        ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider(Constants.BOUNCY_CASTLE_PROVIDER)
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
    private OCSPReq getOCSPRequest(X509Certificate caCert, BigInteger serialNumber) throws Exception {
        OCSPVerifier ocspVerifier = new OCSPVerifier(null);
        Class ocspVerifierClass = ocspVerifier.getClass();
        Method generateOCSPRequest = ocspVerifierClass
                .getDeclaredMethod("generateOCSPRequest", X509Certificate.class, BigInteger.class);
        generateOCSPRequest.setAccessible(true);
        return (OCSPReq) generateOCSPRequest.invoke(ocspVerifier, caCert, serialNumber);
    }

    private void createMockOCSPResponse() throws Exception {
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

    private void cacheOcspResponse(OCSPResp response, BigInteger serialNumber, SingleResp singleResp, OCSPReq request) {
        OCSPCache cache = OCSPCache.getCache();
        int cacheSize = 51;
        int cacheDelay = 15;
        cache.init(cacheSize, cacheDelay);
        cache.setCacheValue(response, serialNumber, singleResp, request, null);
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        httpClientConnector.close();
        serverConnector.stop();
        try {
            factory.shutdown();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for HttpWsFactory to shutdown", e);
        }
    }
}

