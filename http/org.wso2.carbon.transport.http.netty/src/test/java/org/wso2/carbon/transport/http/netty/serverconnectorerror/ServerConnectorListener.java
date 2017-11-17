/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.serverconnectorerror;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpResponseStatusFuture;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Message Processor class to be used for test pass through scenarios
 */
public class ServerConnectorListener implements HttpConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectorListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private TransportsConfiguration configuration;
    private HttpClientConnector clientConnector;
    private HttpResponseStatusFuture statusFuture;
    private boolean isDone = false;
    private Throwable status;

    public ServerConnectorListener(TransportsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        executor.execute(() -> {
            HTTPCarbonMessage responseMsg = httpRequestMessage.cloneCarbonMessageWithData();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("Thread Interrupted while sleeping ", e);
            }
            if (status == null) {
                try {
                    statusFuture = httpRequestMessage.respond(responseMsg);
                    status = statusFuture.sync();
                } catch (ServerConnectorException e) {
                    logger.error("Error occurred during message processing: ", e);
                } catch (InterruptedException e) {
                    logger.error("Thread Interrupted while sleeping ", e);
                }
                isDone = true;
            } else {
                responseMsg.setProperty("Error", status.getMessage());
                try {
                    httpRequestMessage.respond(responseMsg);
                } catch (ServerConnectorException e) {
                    logger.error("Error occurred during message processing: ", e);
                }
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
