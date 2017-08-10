package org.wso2.carbon.transport.http.netty.contentaware;

import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

/**
 * A Message Processor which respond in streaming manner without buffering.
 */
public class ResponseStreamingWithoutBufferingListener implements HTTPConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseTransformStreamingListener.class);
    private TransportsConfiguration configuration;

    public ResponseStreamingWithoutBufferingListener(TransportsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        try {
            CarbonMessage cMsg = new DefaultCarbonMessage(false);
            cMsg.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            cMsg.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            cMsg.setHeader(HttpHeaders.Names.CONTENT_TYPE, Constants.TEXT_PLAIN);
            cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);

            HTTPCarbonMessage httpCarbonMessage = HTTPMessageUtil.convertCarbonMessage(cMsg);

            httpRequestMessage.respond(httpCarbonMessage);
            while (!(httpRequestMessage.isEmpty() && httpRequestMessage.isEndOfMsgAdded())) {
                cMsg.addMessageBody(httpRequestMessage.getMessageBody());
            }
            cMsg.setEndOfMsgAdded(true);
        } finally {
            // Calling the release method to make sure that there won't be any memory leaks from netty
            httpRequestMessage.release();
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
