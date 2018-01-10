package org.wso2.transport.http.netty.multipartdecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpBodyPart;
import org.wso2.transport.http.netty.message.MultipartRequestDecoder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultipartContentListener implements HttpConnectorListener {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartContentListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private CountDownLatch latch;
    private boolean isMultipartRequest;
    private List<HttpBodyPart> multiparts;

    @Override
    public void onMessage(HTTPCarbonMessage httpMessage) {
        executor.execute(() -> {
            latch = new CountDownLatch(1);
            MultipartRequestDecoder requestDecoder = new MultipartRequestDecoder(httpMessage);
            isMultipartRequest = requestDecoder.isMultipartRequest();
            if (isMultipartRequest) {
                try {
                    requestDecoder.parseBody();
                    multiparts = requestDecoder.getMultiparts();
                } catch (IOException e) {
                    LOG.error("Error occured while parsing multipart request in MultipartContentListener" , e);
                }
            }
            latch.countDown();
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }

    public boolean isMultipart() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("InterruptedException occurred while waiting to get the type of http request", e);
        }
        return isMultipartRequest;
    }

    public List<HttpBodyPart> getMultiparts() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("InterruptedException occurred while waiting for multiparts to receive ", e);
        }
        return multiparts;
    }
}
