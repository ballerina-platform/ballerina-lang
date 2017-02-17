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

package org.ballerinalang.natives.connectors.http.server;

import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.natives.connectors.http.TransportConfigProvider;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.services.dispatchers.http.HTTPErrorHandler;
import org.wso2.carbon.messaging.handler.HandlerExecutor;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;

import java.io.PrintStream;
import java.util.Set;

/**
 * {@code HTTPListenerInitializer} is responsible for initializing http transport listener side configuration
 */
@Deprecated
public class HTTPListenerInitializer {

    private static PrintStream outStream = System.out;

    public static void initialize() {

        HTTPTransportContextHolder httpTransportContextHolder = HTTPTransportContextHolder.getInstance();
        httpTransportContextHolder.setHandlerExecutor(new HandlerExecutor());
        httpTransportContextHolder.setMessageProcessor(new MessageProcessor());

        BallerinaConnectorManager.getInstance().registerServerConnectorErrorHandler(new HTTPErrorHandler());

        TransportsConfiguration trpConfig = TransportConfigProvider.getConfiguration();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        Set<TransportProperty> transportProperties = trpConfig.getTransportProperties();

        HTTPTransportListener listener = new HTTPTransportListener(transportProperties, listenerConfigurations);
        listener.start();

        for (ListenerConfiguration listenerConfig : listenerConfigurations) {
            outStream.println("ballerina: started listener " +
                              listenerConfig.getScheme() + "-" + listenerConfig.getPort());
        }

    }

}
