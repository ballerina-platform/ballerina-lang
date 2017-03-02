/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.input.transport.tcp;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SourceCallback;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;

@Extension(
        name = "tcp",
        namespace = "inputtransport",
        description = ""
)
public class TcpInputTransport extends InputTransport {
    static String HOST = "host";
    static String PORT = "port";
    static String WORKER_THREADS = "workerThreads";
    static String RECEIVER_THREADS = "receiverThreads";

    private ServerConfig serverConfig;

    @Override
    public void init(SourceCallback sourceCallback, OptionHolder optionHolder) {
        serverConfig = new ServerConfig();
        serverConfig.setHost(optionHolder.validateAndGetStaticValue(HOST, serverConfig.getHost()));
        serverConfig.setPort(Integer.parseInt(optionHolder.validateAndGetStaticValue(PORT,
                Integer.toString(serverConfig.getPort()))));
        serverConfig.setReceiverThreads(Integer.parseInt(optionHolder.validateAndGetStaticValue(RECEIVER_THREADS,
                Integer.toString(serverConfig.getReceiverThreads()))));
        serverConfig.setWorkerThreads(Integer.parseInt(optionHolder.validateAndGetStaticValue(WORKER_THREADS,
                Integer.toString(serverConfig.getWorkerThreads()))));
    }

    @Override
    public void connect() throws ConnectionUnavailableException {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {

    }
}