package org.wso2.transport.http.netty.http2.clienttimeout;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.OperationStatus;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.http2.listeners.Http2ServerWaitDuringDataWrite;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.wso2.transport.http.netty.util.Http2Util.getHttp2Client;

public class TimeoutDuringResponseReceive {
    private static final Logger LOG = LoggerFactory.getLogger(TimeoutDuringResponseReceive.class);

    private HttpClientConnector h2PriorOffClient;
    private HttpClientConnector h2PriorOnClient;
    private ServerConnector serverConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        connectorFactory = new DefaultHttpWsConnectorFactory();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setVersion(Constants.HTTP_2_0);
        //Set this to a value larger than client socket timeout value, to make sure that the client times out first
        listenerConfiguration.setSocketIdleTimeout(500000);
        serverConnector = connectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        Http2ServerWaitDuringDataWrite http2ServerConnectorListener = new Http2ServerWaitDuringDataWrite(12000);
        future.setHttpConnectorListener(http2ServerConnectorListener);
        future.sync();

        h2PriorOnClient = getHttp2Client(connectorFactory, true, 1000);
        h2PriorOffClient = getHttp2Client(connectorFactory, false, 1000);
    }

    @Test(enabled = false)
    public void testHttp2ClientTimeoutWithPriorOff() {
        HttpCarbonMessage request = MessageGenerator.generateRequest(HttpMethod.POST, "test");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = h2PriorOffClient.send(request);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
//            Throwable error = listener.getHttpErrorMessage();
//            AssertJUnit.assertNotNull(error);
//            assertTrue(error instanceof EndpointTimeOutException,
//                       "Exception is not an instance of EndpointTimeOutException");
//            String result = error.getMessage();
//            assertEquals(result, Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE,
//                         "Expected error message not received");
            Thread.sleep(10000); //Wait for the stream reset error.
            OperationStatus status = responseFuture.getStatus();
            if (status.getCause() != null) {
                String errorMsg = status.getCause().getMessage();
                assertEquals(errorMsg, Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE);
            } else {
                fail("TimeoutDuringResponseDataWrite is heavily dependent on timing of events and needs an alternate " +
                             "solution");
            }
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testHttp2ClientTimeout test case", e);
        }
    }

    @Test
    public void testHttp2ClientTimeoutWithPriorOn() {
        HttpCarbonMessage request = MessageGenerator.generateRequest(HttpMethod.POST, "test");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = h2PriorOnClient.send(request);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
//            Throwable error = listener.getHttpErrorMessage();
//            AssertJUnit.assertNotNull(error);
//            assertTrue(error instanceof EndpointTimeOutException,
//                       "Exception is not an instance of EndpointTimeOutException");
//            String result = error.getMessage();
//            assertEquals(result, Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE,
//                         "Expected error message not received");
            Thread.sleep(10000); //Wait for the stream reset error.
            OperationStatus status = responseFuture.getStatus();
            if (status.getCause() != null) {
                String errorMsg = status.getCause().getMessage();
                assertEquals(errorMsg, Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE);
            } else {
                fail("TimeoutDuringResponseDataWrite is heavily dependent on timing of events and needs an alternate " +
                             "solution");
            }
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testHttp2ClientTimeout test case", e);
        }
    }

    @AfterClass
    public void cleanUp() {
        h2PriorOffClient.close();
        h2PriorOnClient.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
