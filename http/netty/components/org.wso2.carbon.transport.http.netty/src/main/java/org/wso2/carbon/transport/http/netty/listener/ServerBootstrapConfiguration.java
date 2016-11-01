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

package org.wso2.carbon.transport.http.netty.listener;

import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;

import java.util.Set;

/**
 * A class represents Server Bootstrap configurations.
 */
public class ServerBootstrapConfiguration {

    private static ServerBootstrapConfiguration bootstrapConfig;

    private boolean tcpNoDelay = true;

    private int connectTimeOut = 15000;

    private int reciveBufferSize = 1048576;

    private int sendBufferSize = 1048576;

    private boolean keepAlive = true;

    private boolean socketReuse = false;

    private int soBackLog = 100;

    private int socketTimeOut = 15;

    private ServerBootstrapConfiguration(Set<TransportProperty> properties) {

        if (properties != null) {
            properties.forEach(parameter -> {
                if (Constants.SERVER_BOOTSTRAP_CONNECT_TIME_OUT.equals(parameter.getName())) {
                    connectTimeOut = (Integer) (parameter.getValue());
                } else if (Constants.SERVER_BOOTSTRAP_KEEPALIVE.equals(parameter.getName())) {
                    keepAlive = (Boolean) (parameter.getValue());
                } else if (Constants.SERVER_BOOTSTRAP_RECEIVE_BUFFER_SIZE.equals(parameter.getName())) {
                    reciveBufferSize = (Integer) (parameter.getValue());
                } else if (Constants.SERVER_BOOTSTRAP_SEND_BUFFER_SIZE.equals(parameter.getName())) {
                    sendBufferSize = (Integer) (parameter.getValue());
                } else if (Constants.SERVER_BOOTSTRAP_TCP_NO_DELY.equals(parameter.getName())) {
                    tcpNoDelay = (Boolean) (parameter.getValue());
                } else if (Constants.SERVER_BOOTSTRAP_SO_REUSE.equals(parameter.getName())) {
                    socketReuse = (Boolean) (parameter.getValue());
                } else if (Constants.SERVER_BOOTSTRAP_SO_BACKLOG.equals(parameter.getName())) {
                    soBackLog = (Integer) (parameter.getValue());
                } else if (Constants.SERVER_BOOTSTRAP_SO_TIMEOUT.equals(parameter.getName())) {
                    socketTimeOut = (Integer) (parameter.getValue());
                }
            });
        }
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

    public int getSoBackLog() {
        return soBackLog;
    }

    public static ServerBootstrapConfiguration getInstance() {
        return bootstrapConfig;
    }

    public int getSoTimeOut() {
        return socketTimeOut;
    }

    /**
     * configure transport level properties such as socket timeouts, tcp no delay
     *
     * @param properties
     */
    public static void createBootStrapConfiguration(Set<TransportProperty> properties) {
        bootstrapConfig = new ServerBootstrapConfiguration(properties);

    }

}
