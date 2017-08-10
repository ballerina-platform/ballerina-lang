package org.wso2.carbon.transport.http.netty.contentaware;

import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Message Processor which respond in streaming manner without buffering.
 */
public class ResponseStreamingWithoutBufferingListener implements HTTPConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseTransformStreamingListener.class);
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        executor.execute(() -> {
            HTTPCarbonMessage cMsg = new HTTPCarbonMessage();
            cMsg.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            cMsg.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            cMsg.setHeader(HttpHeaders.Names.CONTENT_TYPE, Constants.TEXT_PLAIN);
            cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
            executor.execute(() -> httpRequestMessage.respond(cMsg));
            while (!(httpRequestMessage.isEmpty() && httpRequestMessage.isEndOfMsgAdded())) {
                cMsg.addMessageBody(httpRequestMessage.getMessageBody());
            }
            cMsg.setEndOfMsgAdded(true);
            httpRequestMessage.release();
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
