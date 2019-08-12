/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.contentaware.listeners;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class implements a DumbMessageListener. DumbMessageListener is responsible for just reading the inbound request
 * payload and doing nothing.
 */
public class DumbMessageListener implements HttpConnectorListener {
    private static final Logger LOG = LoggerFactory.getLogger(DumbMessageListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                do {
                    HttpContent httpContent = httpRequest.getHttpContent();
                    httpContent.release();
                    if (httpContent instanceof LastHttpContent) {
                        break;
                    }
                } while (true);
            } catch (Exception e) {
                LOG.error("Error occurred during message notification: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
