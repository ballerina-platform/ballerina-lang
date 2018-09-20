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

package org.wso2.transport.http.netty.contractimpl.sender.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.common.Util;

import java.util.Map;

/**
 * A class represents client bootstrap configurations.
 */
public class BootstrapConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapConfiguration.class);

    private boolean tcpNoDelay;
    private boolean keepAlive;
    private boolean socketReuse;
    private int connectTimeOut;
    private int receiveBufferSize;
    private int sendBufferSize;
    private int socketTimeout;

    public BootstrapConfiguration(Map<String, Object> properties) {

        connectTimeOut = Util.getIntProperty(
                properties, Constants.CLIENT_BOOTSTRAP_CONNECT_TIME_OUT, 15000);

        tcpNoDelay = Util.getBooleanProperty(
                properties, Constants.CLIENT_BOOTSTRAP_TCP_NO_DELY, true);

        receiveBufferSize = Util.getIntProperty(
                properties, Constants.CLIENT_BOOTSTRAP_RECEIVE_BUFFER_SIZE, 1048576);

        sendBufferSize = Util.getIntProperty(
                properties, Constants.CLIENT_BOOTSTRAP_SEND_BUFFER_SIZE, 1048576);

        socketTimeout = Util.getIntProperty(properties, Constants.CLIENT_BOOTSTRAP_SO_TIMEOUT, 15);

        keepAlive = Util.getBooleanProperty(
                properties, Constants.CLIENT_BOOTSTRAP_KEEPALIVE, true);

        socketReuse = Util.getBooleanProperty(
                properties, Constants.CLIENT_BOOTSTRAP_SO_REUSE, false);

        String logValue = "{}:{}";
        LOG.debug(logValue, Constants.CLIENT_BOOTSTRAP_TCP_NO_DELY , tcpNoDelay);
        LOG.debug(logValue, Constants.CLIENT_BOOTSTRAP_CONNECT_TIME_OUT, connectTimeOut);
        LOG.debug(logValue, Constants.CLIENT_BOOTSTRAP_RECEIVE_BUFFER_SIZE, receiveBufferSize);
        LOG.debug(logValue, Constants.CLIENT_BOOTSTRAP_SEND_BUFFER_SIZE, sendBufferSize);
        LOG.debug(logValue, Constants.CLIENT_BOOTSTRAP_SO_TIMEOUT, socketTimeout);
        LOG.debug(logValue, Constants.CLIENT_BOOTSTRAP_KEEPALIVE, keepAlive);
        LOG.debug(logValue, Constants.CLIENT_BOOTSTRAP_SO_REUSE, socketReuse);
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
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
}
