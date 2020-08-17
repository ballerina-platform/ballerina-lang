/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.netty.trailer;

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.net.netty.contentaware.listeners.TrailerHeaderListener;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.ServerConnector;
import org.ballerinalang.net.netty.contract.ServerConnectorFuture;
import org.ballerinalang.net.netty.contract.config.ListenerConfiguration;
import org.ballerinalang.net.netty.contract.exceptions.ServerConnectorException;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.util.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test template for HTTP response trailer headers server setup.
 *
 * @since 6.3.1
 */
public class TrailerHeaderTestTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(TrailerHeaderTestTemplate.class);

    private HttpWsConnectorFactory httpWsConnectorFactory;
    private ServerConnector serverConnector;

    public void setup(ListenerConfiguration listenerConfiguration, HttpHeaders trailers,
                      TrailerHeaderListener.MessageType messageType) {

        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpWsConnectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();

        TrailerHeaderListener trailerHeaderListener = new TrailerHeaderListener();
        trailerHeaderListener.setTrailer(trailers);
        trailerHeaderListener.setMessageType(messageType);
        serverConnectorFuture.setHttpConnectorListener(trailerHeaderListener);

        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for server connector to start");
        }
    }

    public void cleanUp() throws ServerConnectorException {
        try {
            serverConnector.stop();
            httpWsConnectorFactory.shutdown();
        } catch (Exception e) {
            LOG.warn("Resource clean up is interrupted", e);
        }
    }
}
