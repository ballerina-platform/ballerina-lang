package org.wso2.transport.http.netty.http2;

import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.File;

public class TestHttp2Ssl {
    private ServerConnector serverConnector;

    public static void main(String[] args) throws InterruptedException {
        TestHttp2Ssl http2Ssl = new TestHttp2Ssl();
        http2Ssl.setup();
    }

    @Test
    public void setup() throws InterruptedException {

        TransportsConfiguration transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");

        HttpWsConnectorFactory factory = new DefaultHttpWsConnectorFactory();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(8443);
        listenerConfiguration.setScheme(Constants.HTTPS_SCHEME);
        listenerConfiguration.setVersion(String.valueOf(Constants.HTTP_2_0));
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        listenerConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        serverConnector = factory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

//        http2ClientConnector = factory
//                .createHttp2ClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
//                        HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration,
//                                Constants.HTTP_SCHEME));
    }

//    @Test
//    public void testHttp2Post() {
//        String testValue = "Test Http2 Message";
//        HTTPCarbonMessage httpCarbonMessage = RequestGenerator.generateRequest(HttpMethod.POST, testValue);
//        HTTPCarbonMessage response = MessageSender.sendMessage(httpCarbonMessage, http2ClientConnector);
//        assertNotNull(response);
//        String result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
//        assertEquals(testValue, result);
//    }
//
//    @AfterClass
//    public void cleanUp() {
//        http2ClientConnector.close();
//        serverConnector.stop();
//    }
}

