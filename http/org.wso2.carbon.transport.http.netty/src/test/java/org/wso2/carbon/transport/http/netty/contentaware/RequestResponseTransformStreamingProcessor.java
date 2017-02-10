/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.contentaware;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Streaming processor which reads from same and write to same message
 */
public class RequestResponseTransformStreamingProcessor implements CarbonMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseTransformStreamingProcessor.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ClientConnector clientConnector;

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback callback) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null
                            && carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
                            .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {

                        callback.done(carbonMessage);
                    } else {
                        InputStream inputStream = carbonMessage.getInputStream();
                        OutputStream outputStream = carbonMessage.getOutputStream();
                        byte[] bytes = IOUtils.toByteArray(inputStream);
                        outputStream.write(bytes);
                        outputStream.flush();
                        carbonMessage.setEndOfMsgAdded(true);
                        carbonMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                        carbonMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
                        EngineCallBack engineCallBack = new EngineCallBack(callback);
                        clientConnector.send(carbonMessage, engineCallBack);
                    }
                } catch (IOException e) {
                    logger.error("Error while reading stream", e);
                } catch (ClientConnectorException e) {
                    logger.error("MessageProcessor is not supported ", e);
                }
            }
        });

        return false;
    }

    @Override
    public void setTransportSender(TransportSender sender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
        this.clientConnector = clientConnector;
    }

    @Override
    public String getId() {
        return null;
    }

    private class EngineCallBack implements CarbonCallback {

        CarbonCallback requestCallBack;

        EngineCallBack(CarbonCallback carbonCallback) {
            requestCallBack = carbonCallback;
        }

        @Override
        public void done(CarbonMessage carbonMessage) {
            InputStream inputS = carbonMessage.getInputStream();
            OutputStream outputS = carbonMessage.getOutputStream();
            try {
                byte[] bytes = IOUtils.toByteArray(inputS);
                outputS.write(bytes);
                outputS.flush();
                carbonMessage.setEndOfMsgAdded(true);
            } catch (IOException e) {
                throw new RuntimeException("Cannot read Input Stream from Response", e);
            }
            requestCallBack.done(carbonMessage);
        }

    }
}
