package org.wso2.carbon.transport.http.netty.passthrough;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Message Processor class to be used for test pass through scenarios
 */
public class PassthroughMessageProcessorListener implements HTTPConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(PassthroughMessageProcessorListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private TransportsConfiguration configuration;
    private HTTPClientConnector clientConnector;

    public PassthroughMessageProcessorListener(TransportsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        httpRequestMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
        httpRequestMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
        try {
            HTTPClientConnectorFuture httpClientConnectorFuture = clientConnector.send(httpRequestMessage);
            httpClientConnectorFuture.setHTTPConnectorListener(new HTTPConnectorListener() {
                @Override
                public void onMessage(HTTPCarbonMessage httpResponse) {
                    httpRequestMessage.respond(httpResponse);
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
