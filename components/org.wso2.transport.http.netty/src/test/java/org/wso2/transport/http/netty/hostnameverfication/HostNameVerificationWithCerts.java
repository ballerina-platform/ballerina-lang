package org.wso2.transport.http.netty.hostnameverfication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.https.MutualSSLTestCase;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.transport.http.netty.common.Constants.HTTPS_SCHEME;

/**
 * Created by bhashinee on 8/15/18.
 */
public class HostNameVerificationWithCerts {
    private static Logger logger = LoggerFactory.getLogger(MutualSSLTestCase.class);

    private static HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory factory;
    private ServerConnector connector;
    File certFile = new File("/home/bhashinee/TiggerStack/comodoCerts/nginx.crt");
    File keyFile = new File("/home/bhashinee/TiggerStack/comodoCerts/nginx.key");
    //                File certFile = new File("/home/bhashinee/TiggerStack/comodoCerts/NewCerts/certificate.crt");
    //                File keyFile = new File("/home/bhashinee/TiggerStack/comodoCerts/NewCerts/privateKey.key");

    @BeforeClass
    public void setup() throws InterruptedException {

        factory = new DefaultHttpWsConnectorFactory();

        ListenerConfiguration listenerConfiguration = getListenerConfiguration();

        connector = factory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = connector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory.createHttpClientConnector(new HashMap<>(), getSenderConfigs());
    }

    private ListenerConfiguration getListenerConfiguration() {
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(TestUtil.SERVER_PORT3);
        listenerConfiguration.setServerKeyFile("/home/bhashinee/TiggerStack/comodoCerts/new_key.key.pcks8");
        listenerConfiguration.setServerCertificates("/home/bhashinee/TiggerStack/comodoCerts/public.crt");
        //        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        //        listenerConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        listenerConfiguration.setScheme(HTTPS_SCHEME);
//        listenerConfiguration.setOcspStaplingEnabled(true);
        return listenerConfiguration;
    }

    private SenderConfiguration getSenderConfigs() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        //        senderConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        //        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.TRUST_STORE_FILE_PATH));
        //        senderConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        //        senderConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration
                .setClientTrustCertificates("/home/bhashinee/TiggerStack/comodoCerts/public.crt");
        senderConfiguration.setScheme(HTTPS_SCHEME);
        senderConfiguration.setHostNameVerificationEnabled(false);
//        senderConfiguration.setOcspStaplingEnabled(true);
        return senderConfiguration;
    }

    @Test
    public void testHttpsPost() {
        try {
            String testValue = "Test";
            HttpCarbonMessage msg = TestUtil.createHttpsPostReq(TestUtil.SERVER_PORT3, testValue, "");

            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(5, TimeUnit.SECONDS);

            HttpCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(testValue, result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running httpsGetTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        connector.stop();
        httpClientConnector.close();
        try {
            factory.shutdown();
        } catch (Exception e) {
            logger.warn("Interrupted while waiting for response two", e);
        }
    }
}

