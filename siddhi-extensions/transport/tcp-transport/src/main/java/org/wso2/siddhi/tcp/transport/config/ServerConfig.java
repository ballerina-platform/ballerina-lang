/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.siddhi.tcp.transport.config;

import org.wso2.siddhi.tcp.transport.utils.Constant;

public class ServerConfig {
    private int receiverThreads = Constant.DEFAULT_RECEIVER_THREADS;
    private int workerThreads = Constant.DEFAULT_WORKER_THREADS;
    private int port = Constant.DEFAULT_PORT;
    private String host = Constant.DEFAULT_HOST;

    public int getReceiverThreads() {
        return receiverThreads;
    }

    public void setReceiverThreads(int receiverThreads) {
        this.receiverThreads = receiverThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
