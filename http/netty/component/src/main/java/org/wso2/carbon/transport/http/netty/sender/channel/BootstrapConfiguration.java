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


import org.wso2.carbon.transport.http.netty.common.Constants;

import java.util.Map;

/**
 * A class represents client bootstrap configurations.
 */
public class BootstrapConfiguration {

    private static BootstrapConfiguration bootstrapConfig;

    private boolean tcpNoDelay = true;

    private int connectTimeOut = 15000;

    private int reciveBufferSize = 1048576;

    private int sendBufferSize = 1048576;

    private boolean keepAlive = true;

    private boolean socketReuse = false;

    private int socketTimeout = 15;


    private BootstrapConfiguration(Map<String, String> parameters) {

        if (parameters != null) {
            tcpNoDelay = parameters.get(Constants.CLINET_BOOTSTRAP_TCP_NO_DELY) == null ||
                         Boolean.parseBoolean(parameters.get(Constants.CLINET_BOOTSTRAP_TCP_NO_DELY));
            connectTimeOut = parameters.get(Constants.CLINET_BOOTSTRAP_CONNECT_TIME_OUT) != null ?
                             Integer.parseInt(parameters.get(Constants.CLINET_BOOTSTRAP_CONNECT_TIME_OUT)) : 15000;
            reciveBufferSize = parameters.get(Constants.CLINET_BOOTSTRAP_RECEIVE_BUFFER_SIZE) != null ?
                               Integer.parseInt
                                          (parameters.get(Constants.CLINET_BOOTSTRAP_RECEIVE_BUFFER_SIZE)) : 1048576;
            sendBufferSize = parameters.get(Constants.CLINET_BOOTSTRAP_SEND_BUFFER_SIZE) != null ?
                             Integer.parseInt(parameters.get(Constants.CLINET_BOOTSTRAP_SEND_BUFFER_SIZE)) : 1048576;
            socketTimeout = parameters.get(Constants.CLINET_BOOTSTRAP_SO_TIMEOUT) != null ?
                             Integer.parseInt(parameters.get(Constants.CLINET_BOOTSTRAP_SO_TIMEOUT)) : 15;
            keepAlive = parameters.get(Constants.CLINET_BOOTSTRAP_KEEPALIVE) == null ||
                        Boolean.parseBoolean(parameters.get(Constants.CLINET_BOOTSTRAP_KEEPALIVE));
            socketReuse = Boolean.parseBoolean(parameters.get(Constants.CLINET_BOOTSTRAP_SO_REUSE));

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

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public static BootstrapConfiguration getInstance() {
        return bootstrapConfig;
    }

    public static void createBootStrapConfiguration(Map<String, String> parameters) {
        bootstrapConfig = new BootstrapConfiguration(parameters);

    }


}
