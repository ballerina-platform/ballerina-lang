/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.sender.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;

import java.util.Iterator;
import java.util.Set;

/**
 * A class represents client bootstrap configurations.
 */
public class BootstrapConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapConfiguration.class);

    private static BootstrapConfiguration bootstrapConfig;

    private boolean tcpNoDelay = true;

    private int connectTimeOut = 15000;

    private int reciveBufferSize = 1048576;

    private int sendBufferSize = 1048576;

    private boolean keepAlive = true;

    private boolean socketReuse = false;

    private int socketTimeout = 15;

    private BootstrapConfiguration(Set<TransportProperty> transportPropertySet) {

        if (transportPropertySet != null && !transportPropertySet.isEmpty()) {
            Iterator iterator = transportPropertySet.iterator();
            while (iterator.hasNext()) {
                TransportProperty property = (TransportProperty) iterator.next();
                if (property.getName().equals(Constants.CLINET_BOOTSTRAP_TCP_NO_DELY)) {
                    tcpNoDelay = (Boolean) property.getValue();
                } else if (property.getName().equals(Constants.CLINET_BOOTSTRAP_CONNECT_TIME_OUT)) {
                    connectTimeOut = (Integer) property.getValue();
                } else if (property.getName().equals(Constants.CLINET_BOOTSTRAP_RECEIVE_BUFFER_SIZE)) {
                    reciveBufferSize = (Integer) property.getValue();
                } else if (property.getName().equals(Constants.CLINET_BOOTSTRAP_SEND_BUFFER_SIZE)) {
                    sendBufferSize = (Integer) property.getValue();
                } else if (property.getName().equals(Constants.CLINET_BOOTSTRAP_SO_TIMEOUT)) {
                    socketTimeout = (Integer) property.getValue();
                } else if (property.getName().equals(Constants.CLINET_BOOTSTRAP_KEEPALIVE)) {
                    keepAlive = (Boolean) property.getValue();
                } else if (property.getName().equals(Constants.CLINET_BOOTSTRAP_SO_REUSE)) {
                    socketReuse = (Boolean) property.getValue();
                }
            }

        }
        logger.debug(Constants.CLINET_BOOTSTRAP_TCP_NO_DELY + ": " + tcpNoDelay);
        logger.debug(Constants.CLINET_BOOTSTRAP_CONNECT_TIME_OUT + ":" + connectTimeOut);
        logger.debug(Constants.CLINET_BOOTSTRAP_RECEIVE_BUFFER_SIZE + ":" + reciveBufferSize);
        logger.debug(Constants.CLINET_BOOTSTRAP_SEND_BUFFER_SIZE + ":" + sendBufferSize);
        logger.debug(Constants.CLINET_BOOTSTRAP_SO_TIMEOUT + ":" + socketTimeout);
        logger.debug(Constants.CLINET_BOOTSTRAP_KEEPALIVE + ":" + keepAlive);
        logger.debug(Constants.CLINET_BOOTSTRAP_SO_REUSE + ":" + socketReuse);
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public int getReciveBufferSize() {
        return reciveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public boolean isSocketReuse() {
        return socketReuse;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public static BootstrapConfiguration getInstance() {
        return bootstrapConfig;
    }

    public static void createBootStrapConfiguration(Set<TransportProperty> transportProperties) {
        bootstrapConfig = new BootstrapConfiguration(transportProperties);

    }

}
