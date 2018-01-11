/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

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

/**
 * HTTP connector Listener for multipart content reading.
 */
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
                    LOG.error("An error occurred while parsing multipart request in MultipartContentListener", e);
                }
            }
            latch.countDown();
        });
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("An error occurred while listening to multipart contents in MultipartContentListener",
                throwable.getMessage());
    }

    /**
     * Check whether the received message contains multiparts.
     *
     * @return a boolean indicating whether the received message contains multiparts
     */
    public boolean isMultipart() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("InterruptedException occurred while waiting to get the type of http request", e);
        }
        return isMultipartRequest;
    }

    /**
     * Get the list of body parts.
     *
     * @return a list of http body parts
     */
    public List<HttpBodyPart> getMultiparts() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("InterruptedException occurred while waiting for multiparts to receive ", e);
        }
        return multiparts;
    }

    /**
     * Reset body part list and variable that checks the multipart availability.
     */
    public void clearBodyParts() {
        multiparts = null;
        isMultipartRequest = false;
    }
}
